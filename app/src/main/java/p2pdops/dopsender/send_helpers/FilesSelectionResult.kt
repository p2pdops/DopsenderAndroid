package p2pdops.dopsender.send_helpers

import android.app.Activity
import android.content.Intent
import android.util.Log
import p2pdops.dopsender.SenderActivity
import p2pdops.dopsender.SenderActivity.Companion.TAG
import p2pdops.dopsender.modals.*
import p2pdops.dopsender.utils.*
import p2pdops.dopsender.utils.Constants.Companion.APPS
import p2pdops.dopsender.utils.Constants.Companion.FILES
import java.io.File
import java.lang.Exception

fun SenderActivity.handActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
) {
    if (resultCode == Activity.RESULT_OK) {


        if (requestCode == RESULT_CODE_INPUT_APPS) {
            val apps = data?.getSerializableExtra(APPS) as ArrayList<AppData>
            for (app in apps) {
                Log.d(TAG, "handActivityResult: ${app.appName}")
                addSendMessageItem(
                    ConnSendFileItem(
                        type = ConnectionMessageType.SEND_FILE,
                        timestamp = System.currentTimeMillis(),
                        fileType = FileType.Apps,
                        fileName = app.appName + ".apk",
                        filePath = app.canonicalPath,
                        status = ConnFileStatusTypes.WAITING,
                        extraData = app.appPackageName
                    )
                ).also {
                    addFileToQue(app, FileType.Apps)
                }
            }
        } else if (
            requestCode == RESULT_CODE_INPUT_DOCS ||
            requestCode == RESULT_CODE_INPUT_IMAGES ||
            requestCode == RESULT_CODE_INPUT_VIDEOS ||
            requestCode == RESULT_CODE_INPUT_AUDIOS ||
            requestCode == RESULT_CODE_INPUT_COMPRESSED
        ) {
            val files = data?.getSerializableExtra(FILES) as ArrayList<File>
            for (file in files) {

                val type = when (requestCode) {
                    RESULT_CODE_INPUT_DOCS -> FileType.Documents
                    RESULT_CODE_INPUT_IMAGES -> FileType.Images
                    RESULT_CODE_INPUT_VIDEOS -> FileType.Videos
                    RESULT_CODE_INPUT_AUDIOS -> FileType.Audios
                    RESULT_CODE_INPUT_COMPRESSED -> FileType.COMPRESSED
                    else -> {
                        throw Exception("Impossible!")
                    }
                }
                addSendMessageItem(
                    ConnSendFileItem(
                        type = ConnectionMessageType.SEND_FILE,
                        timestamp = System.currentTimeMillis(),
                        fileType = type,
                        fileName = file.name,
                        filePath = file.canonicalPath,
                        status = ConnFileStatusTypes.WAITING
                    )
                ).also {
                    addFileToQue(file, type)
                }
            }

        }
    }
}