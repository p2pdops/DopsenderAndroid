package p2pdops.dopsender.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_wifi_device.view.*
import p2pdops.dopsender.R
import p2pdops.dopsender.SenderActivity
import p2pdops.dopsender.send_helpers.forceConnect
import p2pdops.dopsender.utils.bulge


data class WifiDeviceData(
    var name: String,
    var macAddress: String
)

class WifiDevicesHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

class WifiDevicesAdapter(
    private val c: SenderActivity,
    private var wifiDevices: ArrayList<WifiDeviceData>
) :
    RecyclerView.Adapter<WifiDevicesHolder>() {
    companion object {
        private const val TAG = "WifiDevicesAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WifiDevicesHolder {
        return WifiDevicesHolder(
            LayoutInflater.from(c).inflate(R.layout.item_wifi_device, parent, false)
        )
    }

    fun addDevice(deviceData: WifiDeviceData) {
        Log.d(TAG, "addDevice: ")
        if (!this.wifiDevices.contains(deviceData)) {
            wifiDevices.add(deviceData)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return wifiDevices.size
    }

    override fun onBindViewHolder(holder: WifiDevicesHolder, position: Int) {
        val wifiDevice = wifiDevices[position]
        holder.itemView.deviceName.text = wifiDevice.name
        holder.itemView.deviceAddress.text = wifiDevice.macAddress
        holder.itemView.setOnClickListener() {
            c.forceConnect(wifiDevice.macAddress)
            holder.itemView.connectingLottie.bulge()
        }
    }

}