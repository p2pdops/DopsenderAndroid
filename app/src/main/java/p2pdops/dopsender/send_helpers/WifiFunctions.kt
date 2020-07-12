package p2pdops.dopsender.send_helpers

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import android.os.Build
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_sender.*

import p2pdops.dopsender.SenderActivity
import p2pdops.dopsender.SenderActivity.Companion.TAG
import p2pdops.dopsender.adapters.WifiDeviceData
import p2pdops.dopsender.local_connection.ClientSocketThread
import p2pdops.dopsender.local_connection.GroupOwnerSocketThread
import p2pdops.dopsender.services.ForegroundFileService
import p2pdops.dopsender.utils.getLocalDpKey
import p2pdops.dopsender.utils.getLocalName
import p2pdops.dopsender.utils.wifi.WifiConstants

@SuppressLint("MissingPermission")
fun SenderActivity.startMyPresenceService() {
    val record: MutableMap<String, String> = HashMap()
    record[WifiConstants.PROP_USER_NAME] = getLocalName()
    record[WifiConstants.PROP_USER_DP] = getLocalDpKey()
    record[WifiConstants.PROP_DEVICE_NAME] = Build.MODEL

    val service = WifiP2pDnsSdServiceInfo.newInstance(
        WifiConstants.SERVICE_INSTANCE, WifiConstants.SERVICE_REG_TYPE, record
    )

    manager!!.clearLocalServices(channel, object : WifiP2pManager.ActionListener {
        override fun onSuccess() {
            manager!!.addLocalService(channel, service, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.d(TAG, "addLocalService: onSuccess : ")
                    handler.postDelayed({
                        startDnsSdPresenceListeners()
                    }, 1500)
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

fun SenderActivity.startDnsSdPresenceListeners() {
    manager!!.setDnsSdResponseListeners(channel!!,
        { instanceName: String, _: String?, _: WifiP2pDevice ->
            if (instanceName.equals(WifiConstants.SERVICE_INSTANCE, ignoreCase = true))
                Log.d(TAG, "onServiceAvailable: $instanceName:")

        }, { _: String?, record: Map<String?, String?>, device: WifiP2pDevice ->

            Log.d(TAG, "${device.deviceName} is")

            val newRecord = WifiDeviceData(
                macAddress = device.deviceAddress,
                name = record[WifiConstants.PROP_USER_NAME]
                    ?: error("Please report.. failed to get user name error! "),
                dpKey = record[WifiConstants.PROP_USER_DP] ?: error("Dp error"),
                deviceName = record[WifiConstants.PROP_DEVICE_NAME] ?: error("Device name error")
            )

            Log.d(TAG, "discoverLocalServices: discovered : $newRecord")

            openSheet(discoverDevicesSheet)

            devicesAdapter.addDevice(newRecord)

//            Toast.makeText(this as Context, "$newRecord", Toast.LENGTH_LONG).show()
        }
    ).also {
        clearAndStartPresenceServiceRequests()
    }
}

@SuppressLint("MissingPermission")
fun SenderActivity.clearAndStartPresenceServiceRequests() {
    val serviceRequest: WifiP2pDnsSdServiceRequest = WifiP2pDnsSdServiceRequest.newInstance()
    manager!!.clearServiceRequests(channel, object : WifiP2pManager.ActionListener {
        override fun onSuccess() {
            manager!!.addServiceRequest(channel, serviceRequest,
                object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        Log.d(TAG, "addServiceRequest: onSuccess")
                        manager!!.discoverServices(channel,
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
fun SenderActivity.forceConnect(macAddress: String) {
    val config = WifiP2pConfig()
    config.deviceAddress = macAddress
    config.wps.setup = WpsInfo.PBC

    this.manager!!.connect(this.channel, config, object : WifiP2pManager.ActionListener {
        override fun onSuccess() {
            Log.d(TAG, "forceConnect: connected to : $macAddress ")
        }

        override fun onFailure(reason: Int) {
            Log.d(TAG, "forceConnect: onFailure: $reason")
            startProcess()
        }
    })
}

@SuppressLint("MissingPermission")
fun SenderActivity.disconnect(inform: Boolean = true) {
    if (wifiBroadCastReceiver != null) {
        try {
            unregisterReceiver(wifiBroadCastReceiver)
        } catch (e: Exception) {
            Log.d(TAG, "disconnect: unregister error: $e")
        }
    }

    handler.post {
        manager!!.clearLocalServices(channel, null)
        manager!!.clearServiceRequests(channel, null)
        if (socketThread is GroupOwnerSocketThread)
            (socketThread as GroupOwnerSocketThread).closeSocketAndKillThisThread()
        else if (socketThread is ClientSocketThread)
            (socketThread as ClientSocketThread).closeSocketAndKillThisThread()
    }.apply {
        if (inform) {
            messagePeer("DISCONNECT", "", "")
            Log.d(TAG, "disconnect: informed : $inform")
        }
        manager?.removeGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Toast.makeText(
                    applicationContext,
                    "Successfully disconnected",
                    Toast.LENGTH_LONG
                ).show()
                stopService(Intent(this@disconnect, ForegroundFileService::class.java))
                finish()
            }

            override fun onFailure(reason: Int) {
                Log.e(TAG, "onFailure: failed to disconnect $reason")
                stopService(Intent(this@disconnect, ForegroundFileService::class.java))
                finish()
            }
        })
    }
}

fun SenderActivity.isLocationProviderEnabled(): Boolean {
    val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
}