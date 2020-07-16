package p2pdops.dopsender.zshare_helpers

import android.net.Uri
import android.util.Log

fun ByteArray.processReceivedMessage(callback: (type: String, payload: String) -> Unit) {
    var str = toString(Charsets.UTF_8).replace("+", "")
    Log.d("processReceivedMessage", "original: $str")
    val bool = str.contains('~')
    if (bool) str = str.substring(0, str.indexOf('~'))
    Log.d("processReceivedMessage", "processed: $str")
    val action = str.split(":")
    val type = action[0]
    val data = action[1]
    val payload = Uri.decode(data)
    Log.d("processReceivedMessage", "type: $type")
    Log.d("processReceivedMessage", "payload: $payload")
    callback(type, payload)
}
