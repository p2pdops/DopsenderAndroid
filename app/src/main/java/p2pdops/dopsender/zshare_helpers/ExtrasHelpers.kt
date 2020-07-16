package p2pdops.dopsender.zshare_helpers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_option.view.*
import p2pdops.dopsender.*
import p2pdops.dopsender.modals.*
import p2pdops.dopsender.utils.*
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

fun Activity.getDopsenderFolder(): File {
    val dopsenderFolder =
        File(getExternalFilesDir(null)?.canonicalPath + "/Dopsender/")

    if (!(dopsenderFolder.exists()))
        if (dopsenderFolder.mkdirs())
            Log.d("folderFormalities", "created ${dopsenderFolder.canonicalPath}")

    return dopsenderFolder
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

class SendOptionHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val textView: TextView = itemView.send_option
}

class SendOptionsAdapter(private val c: Activity) : RecyclerView.Adapter<SendOptionHolder>() {

    companion object {
        private const val TAG = "SendOptionsUtils"
        private val colors = arrayOf(
            R.color.c_doc,
            R.color.c_apps,
            R.color.c_img,
            R.color.c_vid,
            R.color.c_aud,
            R.color.c_com
        )

        private val maps = arrayOf(
            "Documents", "Apps",
            "Images",
            "Videos",
            "Audios",
            "Compressed"
        )

        private val activities = arrayOf(
            DocsSelectorActivity::class.java to RESULT_CODE_INPUT_DOCS,
            AppsSelectorActivity::class.java to RESULT_CODE_INPUT_APPS,
            ImagesSelectorActivity::class.java to RESULT_CODE_INPUT_IMAGES,
            VideosSelectorActivity::class.java to RESULT_CODE_INPUT_VIDEOS,
            AudiosSelectorActivity::class.java to RESULT_CODE_INPUT_AUDIOS,
            CompressedSelectorActivity::class.java to RESULT_CODE_INPUT_COMPRESSED
        )

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SendOptionHolder =
        SendOptionHolder(
            LayoutInflater.from(c)
                .inflate(R.layout.item_option, parent, false)
        )

    override fun getItemCount(): Int = maps.size

    override fun onBindViewHolder(holder: SendOptionHolder, position: Int) {
        holder.textView.text = maps[position]
        val color = c.resources.getColor(colors[position])
        holder.textView.setTextColor(color)
        holder.textView.background.setTint(color)
        holder.itemView.setOnClickListener {
            Log.d(TAG, "option click: ")
            c.startActivityForResult(
                Intent(c, activities[position].first),
                activities[position].second
            )
        }
    }

}