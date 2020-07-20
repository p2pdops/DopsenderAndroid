package p2pdops.dopsender.zshare_helpers

import android.app.Activity
import android.content.Context
import android.content.IntentFilter
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import p2pdops.dopsender.modals.*
import p2pdops.dopsender.selectors.SendOptionsAdapter
import p2pdops.dopsender.utils.getDopsenderFolder
import p2pdops.dopsender.utils.getLocalDpKey
import p2pdops.dopsender.utils.getLocalName
import p2pdops.dopsender.utils.wifi.WifiConstants
import p2pdops.dopsender.utils.wifi.WifiConstants.FILE_SERVER_PORT
import java.io.File

fun Activity.getDownloadFile(fileType: FileType, fileName: String): File {

    val file = File(getDopsenderFolder().canonicalPath + '/' + fileType + '/' + fileName)

    file.parentFile?.let {
        if ((it.exists() || it.mkdirs())) file.createNewFile()
    }.also {
        return file
    }
}

fun getDownloadUrl(peerAddress: String, remoteFilePath: String) =
    "http://$peerAddress:$FILE_SERVER_PORT$remoteFilePath"

fun getReceiveItemFromFileData(fileData: FileData) = ConnReceiveFileItem(
    ConnectionMessageType.RECEIVE_FILE,
    System.currentTimeMillis(),
    fileName = fileData.fileName,
    status = ConnFileStatusTypes.LOADING,
    filePath = fileData.filePath,
    fileType = fileData.type,
    fileSize = fileData.fileSize
)

fun getSendItemFromFileAndType(file: File, type: FileType) = ConnSendFileItem(
    type = ConnectionMessageType.SEND_FILE,
    timestamp = System.currentTimeMillis(),
    fileType = type,
    fileName = file.name,
    filePath = file.canonicalPath,
    status = ConnFileStatusTypes.WAITING
)

fun getSendItemFromAppData(app: AppData) = ConnSendFileItem(
    type = ConnectionMessageType.SEND_FILE,
    timestamp = System.currentTimeMillis(),
    fileType = FileType.Apps,
    fileName = app.appName + ".apk",
    filePath = app.canonicalPath,
    status = ConnFileStatusTypes.WAITING,
    extraData = app.appPackageName
)

fun Activity.isLocationProviderEnabled(): Boolean {
    val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
}

fun configFromMacAddress(macAddress: String): WifiP2pConfig {
    val config = WifiP2pConfig()
    config.deviceAddress = macAddress
    config.wps.setup = WpsInfo.PBC
    return config
}

fun Activity.setupSendOptions(optionsRecyclerView: RecyclerView) {
    optionsRecyclerView.setHasFixedSize(true)
    optionsRecyclerView.layoutManager =
        LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    optionsRecyclerView.adapter = SendOptionsAdapter(this)
}

fun wifiP2pInfoReceiverIntentFilter(): IntentFilter {
    val intentFilter = IntentFilter()
    intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
    intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
    intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
    intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
    intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    return intentFilter
}

fun Activity.myWifiPresenceRecord(): MutableMap<String, String> {
    val record: MutableMap<String, String> = HashMap()
    record[WifiConstants.PROP_USER_NAME] = getLocalName()
    record[WifiConstants.PROP_USER_DP] = getLocalDpKey()
    record[WifiConstants.PROP_DEVICE_NAME] = Build.MODEL
    return record
}