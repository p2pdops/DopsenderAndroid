package p2pdops.dopsender.local_connection

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo

import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener

import android.os.Parcelable
import android.util.Log

class WiFiP2PBroadcastReceiver(
    private val manager: WifiP2pManager,
    private val channel: WifiP2pManager.Channel,
    private val connectionInfoListener: ConnectionInfoListener
) : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != null) {
            when (action) {
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    manager.requestConnectionInfo(channel, connectionInfoListener)
                }
                WifiManager.WIFI_STATE_CHANGED_ACTION -> {

                }
            }
        }
    }

    private val connectionListener =
        ConnectionInfoListener { info ->
            //val params: WritableMap = mapper.mapWiFiP2PInfoToReactEntity(info)
//             Utils.sendEvent(reactContext, "WIFI_P2P:CONNECTION_INFO_UPDATED", params)
        }

    companion object {
        private const val TAG = "WiFiP2PBroadcastReceive"
    }
}
