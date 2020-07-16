package p2pdops.dopsender.zshare_helpers

import android.net.Uri
import p2pdops.dopsender.local_connection.MessagesRunnable


const val PLUSES =
    "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"

fun MessagesRunnable.messagePeer(type: String, payload: String? = null): Boolean {
    val data = "$type:${Uri.encode(payload)}"
    write("$PLUSES$data~$PLUSES".toByteArray())
    return true
}