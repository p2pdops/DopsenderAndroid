package p2pdops.dopsender.modals

import android.graphics.drawable.Drawable
import java.io.File

enum class FileType {
    Documents,
    Apps,
    Images,
    Videos,
    Audios,
    Compressed
}

enum class ConnFileStatusTypes {
    WAITING, LOADING, LOADED
}

enum class ConnectionMessageType {
    SEND_FILE,
    RECEIVE_FILE,
}

sealed class ConnectionItem(open val type: ConnectionMessageType, open val timestamp: Long)

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
    val fileSize: Long
)

data class AppData(
    val appName: String,
    val appPackageName: String,
    val appFilePath: String,
    var iconDrawable: Drawable? = null
) : File(appFilePath)
