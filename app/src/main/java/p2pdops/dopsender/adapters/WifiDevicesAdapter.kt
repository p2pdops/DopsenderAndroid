package p2pdops.dopsender.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_wifi_device.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import p2pdops.dopsender.R
import p2pdops.dopsender.ShareActivity
import p2pdops.dopsender.utils.bulge
import p2pdops.dopsender.utils.getDpForKey
import p2pdops.dopsender.utils.shrink
import p2pdops.dopsender.zshare_helpers.connectToMacAddress


data class WifiDeviceData(
    var name: String,
    var deviceName: String,
    var macAddress: String,
    var dpKey: String
)

class WifiDevicesHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

class WifiDevicesAdapter(
    private val c: ShareActivity,
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
        holder.itemView.userDp.setImageResource(getDpForKey(wifiDevice.dpKey))
        holder.itemView.userName.text = wifiDevice.name
        holder.itemView.deviceName.text = wifiDevice.deviceName
        var retry = 0
        holder.itemView.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                delay(500)
                retry++
                c.connectToMacAddress(wifiDevice.macAddress) {
                    if (retry++ == 2 && it != -1) {
                        holder.itemView.connectingLottie.shrink()
                        Toast.makeText(c, "Please retry!", Toast.LENGTH_SHORT).show()
                    } else {
                        c.connectToMacAddress(wifiDevice.macAddress) {
                            if (retry++ == 2 && it != -1) {
                                holder.itemView.connectingLottie.shrink()
                                Toast.makeText(c, "Please retry!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
            holder.itemView.connectingLottie.bulge()
        }
    }

}