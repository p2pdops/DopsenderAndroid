package p2pdops.dopsender

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.net.wifi.p2p.WifiP2pGroup
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.*
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.ac_sender_connected_lay.*
import kotlinx.android.synthetic.main.activity_share.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import p2pdops.dopsender.ShareService.LocalBinder
import p2pdops.dopsender.adapters.WifiDeviceData
import p2pdops.dopsender.adapters.WifiDevicesAdapter
import p2pdops.dopsender.downloader.DownloadResult
import p2pdops.dopsender.downloader.downloadFile
import p2pdops.dopsender.interfaces.ShareActivityImpl
import p2pdops.dopsender.local_connection.WiFiP2PBroadcastReceiver
import p2pdops.dopsender.modals.ConnReceiveFileItem
import p2pdops.dopsender.modals.ConnSendFileItem
import p2pdops.dopsender.modals.ConnectionItem
import p2pdops.dopsender.utils.*
import p2pdops.dopsender.utils.wifi.WifiConstants.WIFI_P2P_DATA
import p2pdops.dopsender.utils.wifi.WifiExtraHelper
import p2pdops.dopsender.zshare_helpers.*

@SuppressLint("MissingPermission")
class ShareActivity : AppCompatActivity(), OnCompleteListener<LocationSettingsResponse>,
    WifiP2pManager.ConnectionInfoListener, WifiP2pManager.GroupInfoListener, ShareActivityImpl {

    companion object {
        const val TAG = "ShareActivity"
        val handler = Handler(Looper.getMainLooper())
        private const val REQUEST_CHECK_SETTINGS = 321
    }

    private var forceRefresh: Boolean = false
    private var devicesAdapter: WifiDevicesAdapter? = null
    private val wifiDevices = ArrayList<WifiDeviceData>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)

        // toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close)
        supportActionBar?.title = "Searching..."

        // ad
        loadAd(native_ad)

        // initial ui
        myDp.setImageResource(getLocalDpRes())
        myInfo.text = getLocalName()

        closeSheet(discoverDevicesSheet)
        setupSendOptions(sendOptionsRecycler)

        // devices list
        devicesAdapter = WifiDevicesAdapter(this, wifiDevices)
        devicesRecycler.setHasFixedSize(true)
        devicesRecycler.layoutManager = LinearLayoutManager(this)
        devicesRecycler.adapter = devicesAdapter

        // start task
        initShareActivity()
        discoverRefreshFab.setOnClickListener {
            this.forceRefresh = true
            discoverRefreshFab.shrink()
            initShareActivity()
        }
    }

    private fun initShareActivity() {
        Handler(Looper.getMainLooper()).postDelayed({ discoverRefreshFab.bulge() }, 12000)
        WifiExtraHelper.openWifi(this)
        rippleBackground.startRippleAnimation()
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val task = LocationServices.getSettingsClient(this).checkLocationSettings(
            LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
                .setAlwaysShow(true).build()
        )
        task.addOnCompleteListener(this)
    }

    override fun onComplete(task: Task<LocationSettingsResponse>) {
        try {
            task.getResult(ApiException::class.java)
            Log.d(TAG, "onComplete: already enabled")
            initializeWifiManager()
        } catch (exception: ApiException) {
            when (exception.statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                    val resolvable = exception as ResolvableApiException
                    resolvable.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.e(TAG, "Failed to show dialog", sendEx)
                } catch (classCast: ClassCastException) {
                    Log.d(TAG, "onComplete: ClassCastException Internal error")
                }

                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    Log.d(TAG, "onComplete: ClassCastException Settings change unavailable")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK || isLocationProviderEnabled()) {
                Log.d(TAG, "onActivityResult: enabled")
                initializeWifiManager()
            } else {
                Log.e(TAG, "onActivityResult: denied")
            }
        } else {
            handActivityResult(requestCode, resultCode, data)
        }
    }

    internal lateinit var manager: WifiP2pManager
    internal lateinit var channel: WifiP2pManager.Channel
    private lateinit var wifiBroadCastReceiver: WiFiP2PBroadcastReceiver
    private fun initializeWifiManager() {
        manager = (getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager)
        channel = manager.initialize(this, Looper.getMainLooper(), null)
        manager.requestConnectionInfo(channel, this)
        manager.requestGroupInfo(channel, this)
        if (!forceRefresh) {
            wifiBroadCastReceiver = WiFiP2PBroadcastReceiver(manager, channel, this)
            registerReceiver(wifiBroadCastReceiver, wifiP2pInfoReceiverIntentFilter())
        }
    }

    /* trigger from  {@link ShareActivity.initializeWifiManager}*/
    override fun onGroupInfoAvailable(groupInfo: WifiP2pGroup?) {
        groupInfo?.let {
            Log.d(
                TAG,
                "onGroupInfoAvailable: Grp Already exist"
            )  // not good!!
        } ?: let {
            Log.d(TAG, "onGroupInfoAvailable: group is null, starting my wifip2p presence")
            addDnsSdLocalService {
                addPresenceListeners { record ->
                    openSheet(discoverDevicesSheet)
                    devicesAdapter?.addDevice(record)
                }
            }
        }
    }

    override
    fun onConnectionInfoAvailable(wifiInfo: WifiP2pInfo?) {
        wifiInfo?.let { info ->
            if (info.groupFormed) {
                // handles when grp formed
                Log.d(TAG, "onConnectionInfoAvailable: $wifiInfo")
                val shareIntent = Intent(
                    this@ShareActivity,
                    ShareService::class.java
                ).putExtra(WIFI_P2P_DATA, info)
                startService(shareIntent)
                bindService(shareIntent, mConnection, Context.BIND_AUTO_CREATE)
            } else {
                isConnected = false
                allConnectionMessages.clear()
                supportActionBar?.title = "Searching..."
                Log.d(TAG, "onConnectionInfoAvailable: it's disconnection!")
                rippleBackground.startRippleAnimation()
                _disConnectedLay.bulge()
                _ConnectedLay.shrink()
            }
            0
        }
    }

    var mService: ShareService? = null
    private var mBound: Boolean = false

    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            className: ComponentName,
            service: IBinder
        ) {
            mService = (service as LocalBinder).service
            mService?.activity = this@ShareActivity
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(wifiBroadCastReceiver)
        mService?.disconnect().also {
            manager.removeGroup(channel, null)

            try {
                unbindService(mConnection)
                unregisterReceiver(wifiBroadCastReceiver)
            } catch (e: Exception) {
                Log.d(TAG, "disconnect: unregister error: $e")
            }
            handler.post {
                manager.clearLocalServices(channel, null)
                manager.clearServiceRequests(channel, null)
            }.apply {
                manager.removeGroup(channel, object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        Toast.makeText(
                            applicationContext, "Disconnected", Toast.LENGTH_LONG
                        ).show()
                        stopService(Intent(this@ShareActivity, ShareService::class.java))
                        finish()
                    }

                    override fun onFailure(reason: Int) {
                        Log.e(TAG, "onFailure: failed to disconnect $reason")
                        stopService(Intent(this@ShareActivity, ShareService::class.java))
                        finish()
                    }
                })
            }
        }
    }

    var isSending = false
    var isReceiving = false
    var connMessagesAdapter: ConnectionMessageAdapter? = null
    val allConnectionMessages = ArrayList<ConnectionItem>()

    private var isConnected = false
    override fun onSocketsConnected() {
        isConnected = true
        supportActionBar?.title = "Connected, Share Files..."
        Log.d(TAG, "onSocketsConnected: ")
        rippleBackground.stopRippleAnimation()
        closeSheet(discoverDevicesSheet)
        _disConnectedLay.shrink()
        setupMessagesRecycler()
        _ConnectedLay.bulge()
        initBars()
    }

    override fun onAddedSenderItem(item: ConnSendFileItem) {
        addSendMessageItem(item)
    }

    private var receivingPosition = 0
    override fun onAddedReceiverItem(downloadUrl: String, receiveItem: ConnReceiveFileItem) {
        Log.d(TAG, "onAddedReceiverItem: $downloadUrl $receiveItem")
        val fileToDownload = getDownloadFile(receiveItem.fileType, receiveItem.fileName)
        CoroutineScope(Dispatchers.IO).launch {
            downloadFile(fileToDownload, downloadUrl).collect {
                withContext(Dispatchers.Main) {
                    when (it) {
                        is DownloadResult.Started -> {
                            Log.d(TAG, "onAddedReceiverItem: Started")
                            receivingPosition = addReceiveMessageItem(item = receiveItem)
                        }
                        is DownloadResult.DownloadStarted -> {
                            Log.d(TAG, "onAddedReceiverItem: Started")
                            setReceivingFileName(receiveItem.fileName)
                            showReceiveProcessBar()
                            updateItemToReceiving(receivingPosition)
                            isReceiving = true
                        }
                        is DownloadResult.Success -> {
                            isReceiving = false
                            updateItemToReceived(receivingPosition, fileToDownload.canonicalPath)
                            mService?.requestNextFile()
                            hideReceiveProcessBar()
                        }
                        is DownloadResult.Error -> {

                        }
                        is DownloadResult.Progress -> {
                            updateReceiveFileStatus(it.percentage, it.eta)
                        }
                    }
                }
            }
        }
    }

    override fun onSendingItemStart(item: ConnSendFileItem) {
        Handler(Looper.getMainLooper()).post {
            setSendingFilePath(item.fileName)
            isSending = true
            showSendProcessBar()
            val currPos =
                allConnectionMessages.indexOfFirst { (it is ConnSendFileItem && it.filePath == item.filePath) }
            if (currPos != -1)
                updateItemToSending(currPos)
        }
    }

    override fun onSendingItemProgress(percentage: Int, eta: Long) {
        Handler(Looper.getMainLooper()).post {
            updateSendFileStatus(eta, percentage)
        }
    }

    override fun onSendingItemSuccess(item: ConnSendFileItem) {
        Handler(Looper.getMainLooper()).post {

            val filePath = item.filePath
            Log.d(TAG, "onReceive: filePath: $filePath")
            val currPos =
                allConnectionMessages.indexOfFirst { (it is ConnSendFileItem && it.filePath == filePath) }
            if (currPos != -1)
                updateItemToSent(currPos)
            isSending = false
            hideSendProcessBar()
        }
    }

    override fun handlePeerDisconnected() {
        manager.requestConnectionInfo(channel, this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (!isConnected) onBackPressed()
            else AlertDialog
                .Builder(this@ShareActivity)
                .setTitle("Disconnect connection?")
                .setMessage("Click yes to disconnect now")
                .setPositiveButton(
                    "Cancel"
                ) { dialog, _ -> dialog.dismiss() }
                .setNegativeButton("Yes") { _, _ ->
                    finish()
                }.show()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}