package p2pdops.dopsender.send_helpers

import android.app.Activity
import android.content.Intent
import android.util.Log
import p2pdops.dopsender.SenderActivity
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
                addSendMessageItem(
                    ConnSendFileItem(
                        type = ConnectionMessageType.SEND_FILE,
                        timestamp = System.currentTimeMillis(),
                        fileType = FileType.APK,
                        fileName = app.appName,
                        filePath = app.absolutePath,
                        status = ConnFileStatusTypes.WAITING,
                        extraData = app.appPackageName
                    )
                )
                addFileToQue(app, FileType.APK)

            }
        } else {
            val files = data?.getSerializableExtra(FILES) as ArrayList<File>
            for (file in files) {

                val type = when (requestCode) {
                    RESULT_CODE_INPUT_DOCS -> FileType.DOC
                    RESULT_CODE_INPUT_IMAGES -> FileType.IMAGE
                    RESULT_CODE_INPUT_VIDEOS -> FileType.VIDEO
                    RESULT_CODE_INPUT_AUDIO -> FileType.AUDIO
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
                        filePath = file.absolutePath,
                        status = ConnFileStatusTypes.WAITING
                    )
                ).also {
                    addFileToQue(file, type)
                }
            }

        }
    }
}