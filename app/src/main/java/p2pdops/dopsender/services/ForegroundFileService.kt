package p2pdops.dopsender.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MIN
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import p2pdops.dopsender.R
import p2pdops.dopsender.SenderActivity
import p2pdops.dopsender.interfaces.FileTransferStatusListener
import p2pdops.dopsender.services.FileHttpServer
import java.lang.Exception
import kotlin.math.roundToInt


class ForegroundFileService : Service(), FileTransferStatusListener {


    companion object {
        private const val TAG = "ForegroundFileService"

        const val FROM_FILE_SERVER = "FROM_FILE_SERVER"

        const val TYPE = "TYPE"
        const val FILE_NAME = "FILE_NAME"

        const val TRANSFER_FILE_STARTED = "TRANSFER_FILE_STARTED"
        const val TRANSFER_FILE_COMPLETED = "TRANSFER_FILE_COMPLETED"
        const val TRANSFER_FILE_CANCELLED = "TRANSFER_FILE_CANCELLED"
        const val TRANSFER_FILE_PROCESS = "TRANSFER_FILE_PROCESS"

        const val PROCESS_PERCENTAGE = "PROCESS_PERCENTAGE"
        const val PROCESS_ETA = "PROCESS_ETA"
        const val PROCESS_SPEED = "PROCESS_SPEED"

    }

    private fun startForeground() {
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("my_service", "My Background Service")
            } else {
                // If earlier version channel ID is not used
                // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                ""
            }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        val notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(101, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    override fun onCreate() {
        super.onCreate()

        startForeground()

        val server = FileHttpServer(null, 9090, this)

        try {
            server.start()
            Log.d(TAG, "onCreate: server started ${server.isAlive} ${server.listeningPort}")
        } catch (e: Exception) {
            Log.e(TAG, "onCreate: failed to start server", e)
        }

    }

    override fun onBind(intent: Intent?): IBinder? {


        return null
    }

    override fun onBytesTransferProgress(
        ip: String?,
        fileName: String?,
        totalSize: Long,
        timeLeft: Long,
        speed: String?,
        currentSize: Long,
        percentageUploaded: Int
    ) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(
            Intent(FROM_FILE_SERVER).putExtra(TYPE, TRANSFER_FILE_PROCESS)
                .putExtra(PROCESS_PERCENTAGE, percentageUploaded)
                .putExtra(PROCESS_ETA, timeLeft)
        )

    }

    override fun onBytesTransferCompleted(ip: String?, fileName: String?) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(
            Intent(FROM_FILE_SERVER).putExtra(TYPE, TRANSFER_FILE_COMPLETED)
                .putExtra(FILE_NAME, fileName)
        )
    }

    override fun onBytesTransferStarted(ip: String?, fileName: String?) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(
            Intent(FROM_FILE_SERVER).putExtra(TYPE, TRANSFER_FILE_STARTED)
                .putExtra(FILE_NAME, fileName)
        )
    }

    override fun onBytesTransferCancelled(ip: String?, error: String?, fileName: String?) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(
            Intent(FROM_FILE_SERVER).putExtra(TYPE, TRANSFER_FILE_CANCELLED)
                .putExtra(FILE_NAME, fileName)
        )
    }

}