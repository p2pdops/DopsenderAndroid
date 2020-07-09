package p2pdops.dopsender.send_helpers

import android.util.Log
import com.google.gson.Gson
import p2pdops.dopsender.SenderActivity
import p2pdops.dopsender.SenderActivity.Companion.TAG
import p2pdops.dopsender.modals.AppData
import p2pdops.dopsender.modals.FileData
import p2pdops.dopsender.modals.FileType
import java.io.File

fun SenderActivity.addFileToQue(file: File, fileType: FileType) {
    Log.d(TAG, "operateMessageAction: $file")
    if (queuedList.isEmpty()) {
        Log.d(TAG, "addFileToQue: que empty adding $file")
        queuedList.add(
            FileData(
                fileType,
                if (fileType == FileType.Apps) (file as AppData).appName + if (file.appName.endsWith(
                        ".apk"
                    )
                ) "" else ".apk" else file.name,
                file.absolutePath,
                file.length()
            )
        )
        Log.d(TAG, "addFileToQue: sending file")
        sendFileToPeer(queuedList[0])
    } else {
        Log.d(TAG, "addFileToQue: que not empty adding $file")
        queuedList.add(
            FileData(
                fileType,
                if (fileType == FileType.Apps) (file as AppData).appName + if (file.appName.endsWith(
                        ".apk"
                    )
                ) "" else ".apk" else file.name,
                file.absolutePath,
                file.length()
            )
        )
    }
}

fun SenderActivity.sendNextFileIfExist() {
    if (queuedList.size > 1) {
        queuedList.remove(queuedList[0])
        Log.d(TAG, "sendNextFileIfExist: sending data")
        sendFileToPeer(queuedList[0])
    } else if (queuedList.isNotEmpty()) {
        Log.d(TAG, "sendNextFileIfExist: que ended no files left")
        queuedList.remove(queuedList[0])
    } else {
        Log.d(TAG, "sendNextFileIfExist: que ended no files left")
    }
}

fun SenderActivity.sendFileToPeer(fileData: FileData) {
    messagePeer("TYPE_SENDING_FILE", fileData.type.name, Gson().toJson(fileData))
}
