package p2pdops.dopsender.modals

import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import java.io.File

data class FolderInfo(val name: String, val path: String, val lastModified: Long)

enum class ConnectionMessageType {
    INIT,
    END,
    SEND_FILE,
    RECEIVE_FILE,
}

enum class FileType {
    DOC,
    APK,
    IMAGE,
    VIDEO,
    AUDIO,
    COMPRESSED
}

enum class ConnFileStatusTypes {
    WAITING, LOADING, LOADED
}

sealed class ConnectionItem(open val type: ConnectionMessageType, open val timestamp: Long)

data class ConnStatus(override val type: ConnectionMessageType, override val timestamp: Long) :
    ConnectionItem(type, timestamp)


data class  ConnSendFileItem(
    override val type: ConnectionMessageType,
    override val timestamp: Long,
    val fileType: FileType,
    val fileName: String,
    val filePath: String,
    var status: ConnFileStatusTypes,
    var extraData: String? = null
) : ConnectionItem(type, timestamp)

data class ConnReceiveFileItem(
    override val type: ConnectionMessageType,
    override val timestamp: Long,
    val fileType: FileType,
    val fileName: String,
    var filePath: String,
    var status: ConnFileStatusTypes,
    val fileSize: Long
) : ConnectionItem(type, timestamp)

data class FileData(
    val type: FileType,
    val fileName: String,
    var filePath: String,
    val fileSize: Long,
    val extraData: String? = null
)

data class AppData(
    val appName: String,
    val appPackageName: String,
    val appFilePath: String,
    var iconDrawable: Drawable? = null
) : File(appFilePath)
