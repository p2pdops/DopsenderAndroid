package p2pdops.dopsender.zshare_helpers

import android.annotation.SuppressLint
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import android.util.Log
import p2pdops.dopsender.ShareActivity
import p2pdops.dopsender.ShareActivity.Companion.TAG
import p2pdops.dopsender.adapters.WifiDeviceData
import p2pdops.dopsender.utils.wifi.WifiConstants.PROP_DEVICE_NAME
import p2pdops.dopsender.utils.wifi.WifiConstants.PROP_USER_DP
import p2pdops.dopsender.utils.wifi.WifiConstants.PROP_USER_NAME
import p2pdops.dopsender.utils.wifi.WifiConstants.SERVICE_INSTANCE
import p2pdops.dopsender.utils.wifi.WifiConstants.SERVICE_REG_TYPE

@SuppressLint("MissingPermission")
fun ShareActivity.addDnsSdLocalService(callBack: () -> Unit) {
    val service = WifiP2pDnsSdServiceInfo.newInstance(
        SERVICE_INSTANCE,
        SERVICE_REG_TYPE,
        myWifiPresenceRecord()
    )
    manager.clearLocalServices(channel, object : WifiP2pManager.ActionListener {
        override fun onSuccess() {

            manager.addLocalService(channel, service, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.d(TAG, "addLocalService: onSuccess : ")
                    callBack()
                }

                override fun onFailure(error: Int) {
                    Log.d(TAG, "addLocalService: onFailure : $error")
                }
            })
        }

        override fun onFailure(i: Int) {
            Log.d(TAG, "clearLocalServices: onFailure: $i")
        }
    })
}

fun ShareActivity.addPresenceListeners(callBack: (record: WifiDeviceData) -> Unit) {
    manager.setDnsSdResponseListeners(
        channel,
        { instanceName: String, _: String?, _: WifiP2pDevice ->
            if (instanceName.equals(SERVICE_INSTANCE, ignoreCase = true))
                Log.d(TAG, "onServiceAvailable: $instanceName:")

        }, { _: String?, record: Map<String?, String?>, device: WifiP2pDevice ->

            Log.d(TAG, "${device.deviceName} is")

            val newRecord = WifiDeviceData(
                macAddress = device.deviceAddress,
                name = record.getValue(PROP_USER_NAME)!!,
                dpKey = record.getValue(PROP_USER_DP)!!,
                deviceName = record.getValue(PROP_DEVICE_NAME)!!
            )

            Log.d(TAG, "discoverLocalServices: discovered : $newRecord")

            callBack(newRecord)

        }
    ).also {
        startPresenceServiceRequests()
    }
}

@SuppressLint("MissingPermission")
fun ShareActivity.startPresenceServiceRequests() {
    val serviceRequest: WifiP2pDnsSdServiceRequest = WifiP2pDnsSdServiceRequest.newInstance()
    manager.clearServiceRequests(channel, object : WifiP2pManager.ActionListener {
        override fun onSuccess() {
            manager.addServiceRequest(channel, serviceRequest,
                object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        manager.discoverServices(channel,
                            object : WifiP2pManager.ActionListener {
                                override fun onSuccess() {
                                    Log.d(TAG, "discoverServices: success")
                                }

                                override fun onFailure(reason: Int) {
                                    Log.d(TAG, "discoverServices: success $reason")
                                }
                            })
                    }

                    override fun onFailure(reason: Int) {
                        Log.d(TAG, "addServiceRequest: onFailure $reason")
                    }
                })
        }

        override fun onFailure(i: Int) {
            Log.d(TAG, "clearServiceRequests: onFailure :$i")
        }
    })
}

@SuppressLint("MissingPermission")
fun ShareActivity.connectToMacAddress(macAddress: String, callBack: (reason: Int) -> Unit) =
    manager.connect(channel, configFromMacAddress(macAddress), object : WifiP2pManager.ActionListener {
        override fun onSuccess() {
            callBack(-1)
            Log.d(TAG, "connectToMacAddress: success : $macAddress")
        }

        override fun onFailure(reason: Int) {
            Log.d(TAG, "connectToMacAddress: onFailure: $reason")
            callBack(reason)
        }
    })
