package p2pdops.dopsender.zshare_helpers

import com.google.gson.Gson
import p2pdops.dopsender.modals.ConnSendFileItem
import p2pdops.dopsender.modals.FileData
import java.io.File

fun ConnSendFileItem.toJson(): String =
    Gson().toJson(FileData(fileType, fileName, filePath, File(filePath).length()))