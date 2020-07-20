package p2pdops.dopsender

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.wifi.p2p.WifiP2pInfo
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import p2pdops.dopsender.downloader.DownloadResult
import p2pdops.dopsender.interfaces.FileTransferStatusListener
import p2pdops.dopsender.interfaces.ShareActivityImpl
import p2pdops.dopsender.local_connection.ClientSocketThread
import p2pdops.dopsender.local_connection.GroupOwnerSocketThread
import p2pdops.dopsender.local_connection.MessagesRunnable
import p2pdops.dopsender.local_connection.WConfiguration.HANDLE_RECEIVED_MESSAGE
import p2pdops.dopsender.local_connection.WConfiguration.SOCKETS_CONNECTED
import p2pdops.dopsender.modals.ConnSendFileItem
import p2pdops.dopsender.modals.FileData
import p2pdops.dopsender.services.FileHttpServer
import p2pdops.dopsender.utils.wifi.WifiConstants.FILE_SERVER_PORT
import p2pdops.dopsender.utils.wifi.WifiConstants.WIFI_P2P_DATA
import p2pdops.dopsender.zshare_helpers.*
import java.io.IOException
import java.net.InetAddress
import java.util.*

class ShareService : Service(), Handler.Callback, FileTransferStatusListener {

    private var wifiP2pDataNull: Boolean = false
    private val handler = Handler(Looper.getMainLooper(), this)
    private val mBinder: IBinder = LocalBinder(this)

    companion object {
        private const val TAG = "ShareService"
    }

    class LocalBinder(val service: ShareService) : Binder()

    private var server: FileHttpServer? = null
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: ")
        startForeground()
        server = FileHttpServer(null, FILE_SERVER_PORT, this)

        try {
            server?.start()
            Log.d(TAG, "onCreate: server started ${server?.isAlive} ${server?.listeningPort}")
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "onCreate: failed to start server", e)
        }
    }

    private var groupOwnerSocketThread: GroupOwnerSocketThread? = null

    private var clientSocketThread: ClientSocketThread? = null

    private var isGroupOwner = false

    var activity: ShareActivityImpl? = null
        set(value) {
            field = value
            if (wifiP2pDataNull) {
                activity?.onInitError()
                stopSelf()
            }
        }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val wifiInfo = intent?.getParcelableExtra<WifiP2pInfo>(WIFI_P2P_DATA)
        if (wifiInfo != null) {
            isGroupOwner = wifiInfo.isGroupOwner
            if (isGroupOwner) {
                try {
                    groupOwnerSocketThread = GroupOwnerSocketThread(handler)
                    groupOwnerSocketThread?.start()
                    Log.d(TAG, "as owner : socketThread: $groupOwnerSocketThread")
                } catch (e: IOException) {
                    Log.e(TAG, "cannot create server: ", e)
                }
            } else {
                Log.d(TAG, "Connected as peer")
                clientSocketThread = ClientSocketThread(handler, wifiInfo.groupOwnerAddress)
                clientSocketThread?.start()
            }
        } else {
            wifiP2pDataNull = true
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return mBinder
    }

    private fun startForeground() {
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChannel()
        else ""

        val notificationBuilder = NotificationCompat.Builder(this, channelId)

        val notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(101, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(): String {
        val chan = NotificationChannel(
            "dopsender_service", "File sharing service",
            NotificationManager.IMPORTANCE_NONE
        )
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)

        return "dopsender_service"

    }

    private var peerAddress: InetAddress? = null

    private var messagesRunnable: MessagesRunnable? = null

    /*
     * VERY IMPORTANT method
     * @param handleMessage is message obtained from Messages Runnable
     */
    override fun handleMessage(handleMessage: Message): Boolean {
        Log.d(TAG, "handleMessage: $handleMessage")

        if (handleMessage.what == -2) {
            activity?.onInitError()
        }

        val obj = handleMessage.obj

        when (handleMessage.what) {
            SOCKETS_CONNECTED -> {
                messagesRunnable = obj as MessagesRunnable
                peerAddress = if (isGroupOwner) {
                    groupOwnerSocketThread?.ipAddress
                } else {
                    clientSocketThread?.groupOwnerAddress
                }.also {
                    activity?.onSocketsConnected()
                }
            }
            HANDLE_RECEIVED_MESSAGE -> {
                (obj as ByteArray).processReceivedMessage { type, payload ->
                    when (type) {
                        TYPE_RECEIVE_FILE_DATA -> {
                            val fileData = Gson().fromJson(payload, FileData::class.java)
                            activity?.onAddedReceiverItem(
                                getDownloadUrl(peerAddress!!.hostAddress, fileData.filePath),
                                getReceiveItemFromFileData(fileData)
                            )
                        }
                        TYPE_REQUEST_NEXT_FILE -> {
                            onRequestedNextFile()
                        }
                        TYPE_REQUEST_DISCONNECT -> {
                            activity?.handlePeerDisconnected()
                        }

                    }
                }
            }
        }

        return true
    }


    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: called")
    }

    fun disconnect() {
        Log.d(TAG, "disconnect: called")
        messagesRunnable?.messagePeer(TYPE_REQUEST_DISCONNECT).also {
            groupOwnerSocketThread?.closeSocketAndKillThisThread()
            clientSocketThread?.closeSocketAndKillThisThread()
            server?.stop()
            stopSelf()
        }
    }

    /*
     * SENDER METHODS
     */
    private val sendItemsQueue: Queue<ConnSendFileItem> = ArrayDeque<ConnSendFileItem>()
    fun addSendFileToQue(item: ConnSendFileItem) {
        if (sendItemsQueue.isEmpty()) {
            sendItemsQueue.add(item)
            messagesRunnable?.messagePeer(TYPE_RECEIVE_FILE_DATA, sendItemsQueue.element().toJson())
        } else {
            sendItemsQueue.add(item)
        }.also {
            activity?.onAddedSenderItem(item)
        }
    }

    private fun onRequestedNextFile() {
        sendItemsQueue.poll()
        sendItemsQueue.peek()?.let {
            messagesRunnable?.messagePeer(TYPE_RECEIVE_FILE_DATA, it.toJson())
        }
    }


    /*
     * RECEIVER METHODS
     */
    fun requestNextFile() {
        messagesRunnable?.messagePeer(TYPE_REQUEST_NEXT_FILE)
    }

    override fun onUploadProgress(result: DownloadResult) {

        when (result) {
            is DownloadResult.Started -> {
                Log.d(ShareActivity.TAG, "onUploadProgress: Started")
            }
            is DownloadResult.DownloadStarted -> {
                Log.d(ShareActivity.TAG, "onUploadProgress: upload started")
                activity?.onSendingItemStart(sendItemsQueue.element())
            }
            is DownloadResult.Success -> {
                activity?.onSendingItemSuccess(sendItemsQueue.element())
            }
            is DownloadResult.Progress -> {
                activity?.onSendingItemProgress(result.percentage, result.eta)
            }
            is DownloadResult.Error -> {

            }
        }
    }
}
