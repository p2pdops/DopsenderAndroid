package p2pdops.dopsender

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender.SendIntentException
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pGroup
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.*
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.ac_sender_connected_lay.*
import kotlinx.android.synthetic.main.activity_sender.*
import p2pdops.dopsender.adapters.WifiDeviceData
import p2pdops.dopsender.adapters.WifiDevicesAdapter
import p2pdops.dopsender.local_connection.*
import p2pdops.dopsender.modals.ConnSendFileItem
import p2pdops.dopsender.modals.ConnectionItem
import p2pdops.dopsender.modals.FileData
import p2pdops.dopsender.send_helpers.*
import p2pdops.dopsender.services.ForegroundFileService
import p2pdops.dopsender.services.ForegroundFileService.Companion.FILE_PATH
import p2pdops.dopsender.services.ForegroundFileService.Companion.FROM_FILE_SERVER
import p2pdops.dopsender.services.ForegroundFileService.Companion.PROCESS_ETA
import p2pdops.dopsender.services.ForegroundFileService.Companion.PROCESS_PERCENTAGE
import p2pdops.dopsender.services.ForegroundFileService.Companion.TRANSFER_FILE_CANCELLED
import p2pdops.dopsender.services.ForegroundFileService.Companion.TRANSFER_FILE_COMPLETED
import p2pdops.dopsender.services.ForegroundFileService.Companion.TRANSFER_FILE_PROCESS
import p2pdops.dopsender.services.ForegroundFileService.Companion.TRANSFER_FILE_STARTED
import p2pdops.dopsender.services.ForegroundFileService.Companion.TYPE
import p2pdops.dopsender.utils.*
import p2pdops.dopsender.utils.wifi.WifiExtraHelper
import java.io.IOException
import java.net.InetAddress

class SenderActivity : AppCompatActivity(), OnCompleteListener<LocationSettingsResponse>,
    WifiP2pManager.ConnectionInfoListener, Handler.Callback {

    companion object {
        const val TAG = "SenderActivity"
        private const val REQUEST_CHECK_SETTINGS = 204
        private const val UPDATE_UI_CONNECTED = 11
        private const val UPDATE_UI_DIS_CONNECTED = 12

    }

    private var forceRefresh: Boolean = false
    internal val handler: Handler = Handler(this)

    lateinit var devicesAdapter: WifiDevicesAdapter

    var peerAddress: InetAddress? = null

    internal var connMessagesAdapter: ConnectionMessageAdapter? = null

    internal var messagesRunnable: MessagesRunnable? = null

    var manager: WifiP2pManager? = null

    var channel: WifiP2pManager.Channel? = null

    internal var wifiBroadCastReceiver: WiFiP2PBroadcastReceiver? = null

    private val wifiDevices = ArrayList<WifiDeviceData>()

    private var isGrpOwner: Boolean? = null

    internal var socketThread: Thread? = null

    val connMessagesList = ArrayList<ConnectionItem>()

    val queuedList = ArrayList<FileData>()

    var isSending: Boolean = false
    var isReceiving: Boolean = false

    var currPos = 0

    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            intent.getStringExtra(TYPE)?.let { type ->
                Log.d("receiver", "Got message: $type ")
                when (type) {
                    TRANSFER_FILE_STARTED -> {
                        val filePath = intent.getStringExtra(FILE_PATH)!!
                        Log.d(TAG, "onReceive: filePath: $filePath")
                        Log.d(TAG, "onReceive: fileQue: $connMessagesList")
                        setSendingFilePath(filePath)
                        isSending = true
                        showSendProcessBar()
                        currPos =
                            connMessagesList.indexOfFirst { (it is ConnSendFileItem && it.filePath == filePath) }
                        if (currPos != -1)
                            updateItemToSending(currPos)
                    }
                    TRANSFER_FILE_PROCESS -> {
                        val percentage = intent.getIntExtra(PROCESS_PERCENTAGE, 0)
                        val eta = intent.getLongExtra(PROCESS_ETA, 60000)
                        updateSendFileStatus(eta, percentage)
                    }
                    TRANSFER_FILE_COMPLETED,
                    TRANSFER_FILE_CANCELLED -> {
                        val filePath = intent.getStringExtra(FILE_PATH)!!
                        Log.d(TAG, "onReceive: filePath: $filePath")
                        Log.d(TAG, "onReceive: fileQue: $connMessagesList")
                        currPos =
                            connMessagesList.indexOfFirst { (it is ConnSendFileItem && it.filePath == filePath) }
                        if (currPos != -1)
                            updateItemToSent(currPos)
                        isSending = false
                        hideSendProcessBar()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_sender)


        myDp.setImageResource(getLocalDpRes())
        myInfo.text = "${getLocalName()}\n${Build.MODEL}"


        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mMessageReceiver, IntentFilter(FROM_FILE_SERVER))

        startService(Intent(this, ForegroundFileService::class.java))


        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close)
        supportActionBar?.title = "Searching..."

        _ConnectedLay.hide()

        handler.post {
            MobileAds.initialize(this)
            val adLoader = AdLoader.Builder(this, getString(R.string.sender_ad_id))
                .forUnifiedNativeAd { unifiedNativeAd ->
                    val styles = NativeTemplateStyle.Builder()
                        .withMainBackgroundColor(
                            ColorDrawable(Color.parseColor("#ffffff"))
                        )
                        .build()

                    native_ad.setStyles(styles)
                    native_ad.setNativeAd(unifiedNativeAd)
                    native_ad.bulge()
                }
                .build()

            adLoader.loadAd(AdRequest.Builder().build())
        }


        closeSheet(discoverDevicesSheet)

        setupSendOptions(sendOptionsRecycler)

        devicesAdapter = WifiDevicesAdapter(this, wifiDevices)
        loadDevicesList(devicesRecycler, devicesAdapter)

        startProcess()

        discoverRefreshFab.setOnClickListener {
            this.forceRefresh = true
            discoverRefreshFab.shrink()
            startProcess()
        }
    }

    internal fun startProcess() {


        handler.postDelayed({
            discoverRefreshFab.bulge()
        }, 12000)

        WifiExtraHelper.openWifi(this)

        val locationRequest = LocationRequest.create()

        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val task = LocationServices.getSettingsClient(this)
            .checkLocationSettings(
                LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest).setAlwaysShow(true).build()
            )
        task.addOnCompleteListener(this)
    }


    @SuppressLint("MissingPermission")
    private fun initializeWifiManager() {
        rippleBackground.startRippleAnimation()
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)

        this.manager = (getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager)
        this.channel = manager!!.initialize(
            this, Looper.getMainLooper()
        ) { }

        manager?.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                //onSuccess
                manager?.stopPeerDiscovery(channel, null)
            }

            override fun onFailure(reason: Int) {
                if (reason == 1) {
                    setDeviceUnSupported()
                    startActivity(Intent(this@SenderActivity, NoSupportedActivity::class.java))
                    finish()
                }
                manager?.stopPeerDiscovery(channel, null)
            }
        })
        this.manager?.requestConnectionInfo(channel, this)
        if (!forceRefresh) {
            wifiBroadCastReceiver = WiFiP2PBroadcastReceiver(manager!!, channel!!, this)
            registerReceiver(wifiBroadCastReceiver, intentFilter)
        }
        startPresenceService()
    }

    override
    fun onComplete(task: Task<LocationSettingsResponse>) {
        try {
            task.getResult(ApiException::class.java)
            Log.d(TAG, "onComplete: already enabled")
            initializeWifiManager()
        } catch (exception: ApiException) {
            when (exception.statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                    val resolvable = exception as ResolvableApiException
                    resolvable.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
                } catch (sendEx: SendIntentException) {
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

    @SuppressLint("MissingPermission")
    private fun startPresenceService() {
        manager!!.requestGroupInfo(channel) { wifiP2pGroup: WifiP2pGroup? ->

            if (wifiP2pGroup == null) {
                Log.d(TAG, "startPresenceService: no grp starting presence")
                startMyPresenceService()
            } else manager!!.removeGroup(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.d(TAG, "startPresenceService: grp rm success: starting presence")
                    startMyPresenceService()
                }

                override fun onFailure(i: Int) {
                    Log.d(TAG, "onFailure: remove group failed")
                    startProcess()
                }
            })

        }
    }

    override
    fun onConnectionInfoAvailable(info: WifiP2pInfo?) {
        Log.d(TAG, "onConnectionInfoAvailable: $info")

        val wifiInfo = info ?: return

        if (!wifiInfo.groupFormed) {
            updateUi(UPDATE_UI_DIS_CONNECTED)
            return
        }

        updateUi(UPDATE_UI_CONNECTED)

        isGrpOwner = wifiInfo.isGroupOwner
        if (isGrpOwner!!) {
            Log.d(TAG, "onConnectionInfoAvailable: Connected as group owner")
            try {
                Log.d(TAG, "onConnectionInfoAvailable: socketThread:" + (socketThread != null))
                socketThread = GroupOwnerSocketThread(handler)
                socketThread?.start()

                //set Group Owner ip address
                Log.d(TAG, "onConnectionInfoAvailable: ${wifiInfo.groupOwnerAddress.hostAddress}")

            } catch (e: IOException) {
                Log.e(TAG, "onConnectionInfoAvailable: failed to create a server thread: ", e)
                return
            }
        } else {
            Log.d(TAG, "Connected as peer")
            socketThread = ClientSocketThread(handler, wifiInfo.groupOwnerAddress)
            socketThread!!.start()
        }

    }

    private fun updateUi(code: Int) {
        when (code) {
            UPDATE_UI_CONNECTED -> {

                waiting.slideUp()
                tapAnimation.slideUp()
                if (!getSendSelectHelperShown()) {

                    TapTargetView.showFor(this,
                        TapTarget.forView(
                            sendOptionsRecycler,
                            "Select files here!",
                            "Click on this menu to select different kinds of files."
                        )
                            .outerCircleColor(R.color.pureWhite) // Specify a color for the outer circle
                            .outerCircleAlpha(0.98f) // Specify the alpha amount for the outer circle
                            .targetCircleColor(R.color.unPureWhite) // Specify a color for the target circle
                            .titleTextSize(20) // Specify the size (in sp) of the title text
                            .titleTextColor(R.color.pureBlack) // Specify the color of the title text
                            .descriptionTextSize(16) // Specify the size (in sp) of the description text
                            .descriptionTextColor(R.color.pureBlack) // Specify the color of the description text
                            .textColor(R.color.black80) // Specify a color for both the title and description text
                            .textTypeface(Typeface.SANS_SERIF) // Specify a typeface for the text
                            .dimColor(R.color.unPureWhite) // If set, will dim behind the view with 30% opacity of the given color
                            .drawShadow(true) // Whether to draw a drop shadow or not
                            .cancelable(true) // Whether tapping outside the outer circle dismisses the view
                            .tintTarget(true) // Whether to tint the target view's color
                            .transparentTarget(true) // Specify whether the target is transparent (displays the content underneath)
                            .targetRadius(15),  // Specify the target radius (in dp)
                        object : TapTargetView.Listener() {
                            override fun onTargetClick(view: TapTargetView?) {
                                super.onTargetClick(view)
                                Toast.makeText(
                                    this@SenderActivity,
                                    "Ready to go!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                    setSendSelectHelperShown()
                }

                rippleBackground.stopRippleAnimation()
                closeSheet(discoverDevicesSheet)
                _disConnectedLay.shrink()
                _ConnectedLay.bulge()
                setupMessagesRecycler()
                initBars()
            }
            UPDATE_UI_DIS_CONNECTED -> {
                rippleBackground.startRippleAnimation()
                _disConnectedLay.bulge()
                _ConnectedLay.shrink()
            }
            else -> {

            }
        }
    }

    override
    fun onDestroy() {
        super.onDestroy()
        disconnect()
    }

    override
    fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (this.peerAddress == null) onBackPressed()
            else
                AlertDialog
                    .Builder(this@SenderActivity)
                    .setTitle("Disconnect connection?")
                    .setMessage("Click yes to disconnect now")
                    .setPositiveButton(
                        "Cancel"
                    ) { dialog, _ -> dialog.dismiss() }
                    .setNegativeButton("Yes") { _, _ ->
                        disconnect()
                    }.show()
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    override
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK || isLocationProviderEnabled()) {
                Log.d(TAG, "onActivityResult: enabled")
                initializeWifiManager()
            } else {
                Log.e(TAG, "onActivityResult: denied")
            }
        }

        handActivityResult(requestCode, resultCode, data)
    }


    override
    fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            WConfiguration.FIRST_SHAKE_HAND -> {
                val obj = msg.obj
                messagesRunnable = obj as MessagesRunnable
                messagePeer("INITIAL_SHAKE_HAND", "", "")
                if (socketThread is GroupOwnerSocketThread) {
                    this.peerAddress = (socketThread as GroupOwnerSocketThread).ipAddress
                } else if (socketThread is ClientSocketThread) {
                    this.peerAddress = (socketThread as ClientSocketThread).groupOwnerAddress
                }
            }

            WConfiguration.HANDLE_TRY_FORCE_DISCONNECT -> {
                disconnect(false)
            }
            // received a msg, handle it
            WConfiguration.HANDLE_OBTAINED_MESSAGE -> {

                val readBuf = msg.obj as ByteArray
                var str = String(readBuf, 0, msg.arg1).replace("+", "")
                Log.d(TAG, "handleMessage: $str")
                val bool = str.contains('~')
                if (bool) str = str.substring(0, str.lastIndexOf('~'))
                Log.d(TAG, "handleMessage: $str")
                val action = str.split(":")
                Log.d(TAG, "handleMessage: $action")

                val type = action[0]
                val subType = action[1]
                val payload = action[2]
                val data = Uri.decode(payload)
                Log.d(TAG, "handleMessage: $type $subType $payload")

                operateMessageAction(type, subType, data)
            }
            WConfiguration.HANDLE_TRY_RESTART_SOCKET -> {
                if (isGrpOwner!!) {
                    try {
                        socketThread = GroupOwnerSocketThread(handler)
                        socketThread!!.start()
                    } catch (e: Exception) {
                        Log.d(TAG, "handleMessage: restarting socket connection failed")
                    }
                } else {
                    socketThread = ClientSocketThread(handler, groupOwnerAddress = peerAddress!!)
                    socketThread!!.start()
                }
            }
        }
        return true
    }

}