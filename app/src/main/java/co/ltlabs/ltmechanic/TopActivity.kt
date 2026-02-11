package co.ltlabs.ltmechanic

import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import co.ltlabs.ltmechanic.constant.AppConfig
import co.ltlabs.ltmechanic.domain.WifiNetwork
import co.ltlabs.ltmechanic.service.*
import co.ltlabs.ltmechanic.util.ConnectionUtil
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.coroutines.*
import timber.log.Timber
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

abstract class TopActivity : DaggerAppCompatActivity() {
    private var snackbar: Snackbar? = null
    private var register = false //TO Check if receiver already register?
    private var isPing = false
    private var isMobileDataEnabled = false
    private val topScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @Inject
    lateinit var wifiReceiver: WifiConnectivityReceiver

    @Inject
    lateinit var wifiScannerReceiver: WifiScannerReceiver

    //Each view response by themselve
    abstract fun onWifiStateDisabled(isMobileDataEnabled: Boolean)
    abstract fun onWifiStateEnabling()
    abstract fun onWifiStateDisabling()
    abstract fun onWifiStateEnabled(isDataEnabled: Boolean)
    abstract fun onConnectionPoor()
    abstract fun onConnectionStrong()
    abstract fun onNoConnection()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WifiScannerReceiver.connectionListener = object : ConnectivityListener {
            override fun onConnectionPoor() {
                Timber.d(">>>> Connection is poor, scanning strongest wifi")
                this@TopActivity.onConnectionPoor()
            }

            override fun onConnectionStrong() {
                this@TopActivity.onConnectionStrong()
            }

            @SuppressLint("MissingPermission")
            override fun scanStrongestWifi(currentSSID: String) {
                Timber.d(">>> Scanning strongest wifi...")
                try {
                    val wifiManager =
                        applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                    val wifiNetworks = mutableListOf<WifiNetwork>()
                    val wifiList: List<ScanResult> = wifiManager.scanResults
                    Timber.d(">>> Scan Result : ${wifiList.size}")
                    for (scanResult in wifiList) {
                        val level = WifiManager.calculateSignalLevel(scanResult.level, 5)
                        if (wifiManager.configuredNetworks.any { it.SSID.replace("\"", "") == scanResult.SSID }) {
                            wifiNetworks.add(WifiNetwork(level, scanResult.SSID))
                        }
                    }

                    Timber.d(">>> Wifi Network : ${wifiNetworks.size}")
                    wifiNetworks.forEachIndexed { index, wifiNet ->
                        if (wifiNet.ssid == currentSSID) {
                            topScope.launch {
                                if (index < wifiNetworks.size) {
                                    wifiNetworks.removeAt(index)
                                }
                            }
                        }
                    }

                    topScope.launch {
                        wifiNetworks.sortByDescending { it.level }
                        loop@ for (wifiNetwork in wifiNetworks) {
                            Timber.d(">>> Connecting to ${wifiNetwork.ssid}")
                            if (wifiNetwork.level > 1) {
                                val connected = connectToWifi(wifiManager, wifiNetwork.ssid)
                                Timber.d(">>>> Connected")
                                if (connected) {
                                    break@loop
                                }
                            }
                        }
                    }

                } catch (e: Exception) {
                    Timber.e(">>> Wifi scanning problem ${e.localizedMessage}")
                }
            }
        }

        WifiConnectivityReceiver.wifiListener = object : WifiListener {
            override fun onNoConnection() {
                this@TopActivity.onNoConnection()
            }

            override fun onMobileDataEnabled(isEnabled: Boolean) {
                Timber.d(">>>> mobile data enabled : $isEnabled")
                isMobileDataEnabled = isEnabled
                if (isEnabled) onWifiStateEnabled()
            }

            override fun onWifiStateDisabled() {
                Timber.d(">>> Wifi Disable on ${this@TopActivity}")
                this@TopActivity.onWifiStateDisabled(isMobileDataEnabled)
            }

            override fun onWifiStateDisabling() {
                this@TopActivity.onWifiStateDisabling()
            }

            override fun onWifiStateEnabled() {
                if (!isPing) {
                    isPing = true
                    Handler().postDelayed({
                        runBlocking {
                            Timber.d(">>> Ping server ... ")
                            val isPingSuccess = pingServer(AppConfig.PING_URL)
                            ConnectionUtil.setInternetConnected(isPingSuccess)
                            if (isPingSuccess) {
                                Timber.d(">>> Wifi enabled : Connected to ${AppConfig.PING_URL} ")
                                this@TopActivity.onWifiStateEnabled(true)
                            } else {
                                Timber.d(">>> Wifi enabled : Unable to connect to ${AppConfig.PING_URL}")
                                this@TopActivity.onWifiStateEnabled(false)
                            }
                            isPing = false
                        }
                    },3000)
                }
            }

            override fun onWifiStateEnabling() {
                this@TopActivity.onWifiStateEnabling()
            }
        }

    }

    fun setupReceiver() {
        if (!register) {
            Timber.e(">>> Register receiver for ${this@TopActivity}")
            val intentFilter = IntentFilter()
            intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
            application.registerReceiver(
                wifiReceiver,
                intentFilter
            )
            application.registerReceiver(
                wifiScannerReceiver,
                IntentFilter(WifiManager.RSSI_CHANGED_ACTION)
            )
            register = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //unregister receiver
        try {
            Timber.e(">>>> Unregister for ${this@TopActivity}")
            unregisterReceiver(wifiReceiver)
            unregisterReceiver(wifiScannerReceiver)
        } catch (e: java.lang.Exception) {
            Timber.e(e.localizedMessage)
        }
    }

    private fun connectToWifi(wifiManager: WifiManager, ssid: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val builder = WifiNetworkSuggestion.Builder().setSsid(ssid)
            val suggestion = builder.build()
            val list = arrayListOf(suggestion)
            val status = wifiManager.addNetworkSuggestions(list)

            return status == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS
        } else {
            if (!wifiManager.isWifiEnabled) {
                wifiManager.isWifiEnabled = true
            }
            val conf = WifiConfiguration()
            conf.SSID = String.format("\"%s\"", ssid)
            val netId = wifiManager.addNetwork(conf)
            wifiManager.disconnect()
            wifiManager.enableNetwork(netId, true)
            wifiManager.reconnect()
            return wifiManager.connectionInfo.ssid == ssid
        }
    }

    //Replace showing message with popup
//    fun showLongSnackBar(message: String, color: Int) {
//        if (snackbar != null) hideSnackBar()
//        snackbar =
//            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_INDEFINITE)
//        val sView = snackbar!!.view
//        sView.setBackgroundColor(color)
//        val textView =
//            sView.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
//        textView.setTextColor(Color.WHITE)
//        snackbar!!.show()
//    }
//
//    fun showSnackBar(message: String) {
//        if (snackbar != null) hideSnackBar()
//        snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
//        val sView = snackbar!!.view
//        sView.setBackgroundColor(Color.BLUE)
//        val textView =
//            sView.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
//        textView.setTextColor(Color.WHITE)
//        snackbar!!.show()
//    }

//    fun hideSnackBar() {
//        if (snackbar != null && snackbar!!.isShown) {
//            snackbar!!.dismiss()
//        }
//    }

    /**
     * Ping server with url provided
     */
    private suspend fun pingServer(url: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.setRequestProperty("User-Agent", "ConnectionTest")
                connection.setRequestProperty("Connection", "close")
                connection.connectTimeout = 60000 // configurable
                connection.connect()
                connection.responseCode == 200
            } catch (e: Exception) {
                Timber.e(">>>> ${e.localizedMessage}")
                false
            }
        }
    }


    /**
     * Check network is connected or not ***
     */
    fun isNetworkConnected(): Boolean {
        val connectivityManager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    fun hasNetworkAvailable(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val service = Context.CONNECTIVITY_SERVICE
            val manager = context.getSystemService(service) as ConnectivityManager?
            val n = manager?.activeNetwork
            if (n != null) {
                val nc = manager.getNetworkCapabilities(n)
                //It will check for both wifi and cellular network
                return nc!!.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI
                )
            }
            return false
        } else {
            val service = Context.CONNECTIVITY_SERVICE
            val manager = context.getSystemService(service) as ConnectivityManager?
            val network = manager?.activeNetworkInfo
            return (network != null)
        }
    }

}