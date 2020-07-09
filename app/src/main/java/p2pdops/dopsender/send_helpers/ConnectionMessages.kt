package p2pdops.dopsender.send_helpers

import android.app.AlertDialog
import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import p2pdops.dopsender.SenderActivity
import p2pdops.dopsender.SenderActivity.Companion.TAG
import p2pdops.dopsender.local_connection.GroupOwnerSocketThread

import p2pdops.dopsender.modals.*
import java.lang.Exception

import java.net.InetAddress


const val PLUSES =
    "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"

fun SenderActivity.messagePeer(type: String, subType: String, payload: String): Boolean {
    Log.d(TAG, "messagePeer: ")
    if (messagesRunnable != null) {
        val ipAddress: InetAddress
        val data = "$type:$subType:${Uri.encode(payload)}"
        handler.postDelayed({
            if (socketThread is GroupOwnerSocketThread) {
                Log.d(TAG, "messagePeer: i'm owner : sending data $data")
                messagesRunnable?.write("$PLUSES${data}~$PLUSES".toByteArray())
            } else {
                Log.d(TAG, "messagePeer: i'm client: sending data $data")
                messagesRunnable?.write("$PLUSES${data}~$PLUSES".toByteArray())
            }
        }, 500)
    }
    return true
}

fun SenderActivity.operateMessageAction(type: String, subType: String, payload: String) {
    Log.d(TAG, "operateMessageAction: $type")

    when (type) {
        "DISCONNECTED" -> {
            AlertDialog.Builder(this).setTitle("Disconnected!")
                .setMessage("The other device has been disconnected, so connection closed.")
                .setNegativeButton(
                    "Ok"
                ) { dialog, _ ->
                    dialog.dismiss()
                    disconnect()
                }.show()
        }
        "TYPE_SENDING_FILE" -> {
            try {
                val fileData = Gson().fromJson(payload, FileData::class.java)
                Log.d(TAG, "operateMessageAction: $fileData")
                addReceiveMessageItem(
                    ConnReceiveFileItem(
                        type = ConnectionMessageType.RECEIVE_FILE,
                        timestamp = System.currentTimeMillis(),
                        fileName = fileData.fileName,
                        status = ConnFileStatusTypes.WAITING,
                        filePath = fileData.filePath,
                        fileType = fileData.type,
                        fileSize = fileData.fileSize
                    )
                ).apply {
                    val data =
                        ReceiveFileDownloadTask(this, this@operateMessageAction, fileData).execute()
                    Log.d(TAG, "operateMessageAction: returned data: $data")
                }
                Log.d(TAG, "${connMessagesList.size}")
            } catch (e: Exception) {
                Log.e(TAG, "operateMessageAction: error when msg : $payload")
                Log.e(TAG, "operateMessageAction: error", e);
            }

        }
        "REQ_NEXT_FILE" -> {
            sendNextFileIfExist()
        }
    }
}