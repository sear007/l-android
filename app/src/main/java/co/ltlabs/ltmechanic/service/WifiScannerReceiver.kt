package co.ltlabs.ltmechanic.service

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import co.ltlabs.ltmechanic.domain.WifiNetwork
import timber.log.Timber

class WifiScannerReceiver : BroadcastReceiver() {
    companion object {
        var connectionListener: ConnectivityListener? = null
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            val wifiManager = context!!.getSystemService(Context.WIFI_SERVICE) as WifiManager
            wifiManager.startScan()
            val rssi = wifiManager.connectionInfo.rssi
            val currentSSID = wifiManager.connectionInfo.ssid.replace("\"", "")
            val level = WifiManager.calculateSignalLevel(rssi, 5)
            Timber.d(">>> Network Level : $level")
            if (level > 0) {
                when (level) {
                    4 -> {
                        Timber.d(">>> Connection Strong $level")
                        connectionListener?.onConnectionStrong()
                    }
                    3 -> {
                        Timber.d(">>> Connection Moderate $level")
                        connectionListener?.onConnectionStrong()
                    }
                    else -> {
                        Timber.d(">>> Connection Poor $level")
                        connectionListener?.onConnectionPoor()
                        //scanStrongestWifi(wifiManager)
                        connectionListener?.scanStrongestWifi(currentSSID)
                    }
                }
            } else {
               // scanStrongestWifi(wifiManager)
                connectionListener?.scanStrongestWifi(currentSSID)
            }
        } catch (e: java.lang.Exception) {
            Timber.e(e.localizedMessage)
        }
    }

    //Before using must check permission location
    //FINELOCATION, CORS LOCATION
//    @SuppressLint("MissingPermission")
//    fun scanStrongestWifi(wifiManager: WifiManager) {
//        Timber.d(">>> Start scanning")
//        val wifiNetworks = mutableListOf<WifiNetwork>()
//        // scan networks
//        val wifiList: List<ScanResult> = wifiManager.scanResults
//
//        for (scanResult in wifiList) {
//            val level = WifiManager.calculateSignalLevel(scanResult.level, 5)
//            if (wifiManager.configuredNetworks.any {
//                    it.SSID.replace("\"", "") == scanResult.SSID
//                }) {
//                wifiNetworks.add(WifiNetwork(level, scanResult.SSID))
//            }
//        }
//
//        wifiNetworks.sortByDescending { it.level }
//        Timber.d(">>> Wifi network = ${wifiNetworks.size}")
//        loop@ for (wifiNetwork in wifiNetworks) {
//            if (wifiNetwork.level > 1) {
//                val connected = connectToWifi(wifiNetwork.ssid, wifiManager)
//                if (connected) {
//                    break@loop
//                }
//            }
//        }
//    }
//
//
//    @SuppressLint("MissingPermission")
//    private fun connectToWifi(networkSSID: String, wifiManager: WifiManager): Boolean {
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            val builder = WifiNetworkSuggestion.Builder().setSsid(networkSSID)
//            val suggestion = builder.build()
//            val list = arrayListOf(suggestion)
//
//            val status = wifiManager.addNetworkSuggestions(list)
//            Timber.d(">>> Connecting to wifi [${networkSSID}] : ${status == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS}")
//            status == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS
//
//        } else {
//            if (!wifiManager.isWifiEnabled) {
//                wifiManager.isWifiEnabled = true
//            }
//
//            val conf = WifiConfiguration()
//            conf.SSID = String.format("\"%s\"", networkSSID)
//
//            val netId = wifiManager.addNetwork(conf)
//            wifiManager.disconnect()
//            wifiManager.enableNetwork(netId, true)
//            wifiManager.reconnect()
//            Timber.d(">>> Connecting to wifi [$networkSSID] : ${wifiManager.connectionInfo.ssid == networkSSID}")
//            wifiManager.connectionInfo.ssid == networkSSID
//        }
//    }

}