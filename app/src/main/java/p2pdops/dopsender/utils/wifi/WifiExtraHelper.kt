package p2pdops.dopsender.utils.wifi

import android.content.Context
import android.net.wifi.WifiManager


object WifiExtraHelper {


    fun openWifi(context: Context) {
        val wifiManager = context.applicationContext
            .getSystemService(Context.WIFI_SERVICE) as WifiManager

        if (!wifiManager.isWifiEnabled) wifiManager.setWifiEnabled(true)

    }

    fun closeWifi(context: Context) {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        wifiManager.isWifiEnabled = false
    }
}