package co.ltlabs.ltmechanic.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.SystemClock
import android.telephony.TelephonyManager
import android.util.Log
import co.ltlabs.ltmechanic.util.ConnectionUtil
import kotlinx.coroutines.delay
import timber.log.Timber
import java.lang.reflect.Method

interface WifiListener{
    fun onWifiStateDisabled()
    fun onWifiStateDisabling()
    fun onWifiStateEnabled()
    fun onWifiStateEnabling()
    fun onNoConnection()
    fun onMobileDataEnabled(isEnabled: Boolean)
}
class WifiConnectivityReceiver : BroadcastReceiver() {

    companion object{
        var wifiListener : WifiListener? = null
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            val manager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (manager.activeNetworkInfo != null){
                if(manager.activeNetworkInfo.type == ConnectivityManager.TYPE_MOBILE){
                    val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                        wifiListener?.onMobileDataEnabled(telephonyManager.isDataEnabled)
                    } else {
                        wifiListener?.onMobileDataEnabled(isMobileDataEnabled(context)?:false)
                    }
                }

                if(manager.activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI){
                    val wifi = context?.getSystemService(Context.WIFI_SERVICE) as WifiManager
                    when (wifi.wifiState) {
                        WifiManager.WIFI_STATE_DISABLED -> {
                            ConnectionUtil.setInternetConnected(false)
                            wifiListener?.onWifiStateDisabled()
                        }
                        WifiManager.WIFI_STATE_DISABLING-> {
                            wifiListener?.onWifiStateDisabling()
                        }
                        WifiManager.WIFI_STATE_ENABLING -> {
                            wifiListener?.onWifiStateEnabling()
                        }
                        WifiManager.WIFI_STATE_ENABLED -> {
                            wifiListener?.onWifiStateEnabled()
                        }
                    }
                }
            }else{
                wifiListener?.onNoConnection()
            }
        }catch (e:Exception){
            wifiListener?.onNoConnection()
        }
    }

    private fun isMobileDataEnabled(context: Context?): Boolean? {
        val connectivityService = context?.getSystemService(Context.CONNECTIVITY_SERVICE)
        val cm = connectivityService as ConnectivityManager
        return try {
            val c = Class.forName(cm.javaClass.name)
            val m: Method = c.getDeclaredMethod("getMobileDataEnabled")
            m.isAccessible = true
            m.invoke(cm) as Boolean
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}