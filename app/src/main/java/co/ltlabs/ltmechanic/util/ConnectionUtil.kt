package co.ltlabs.ltmechanic.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import co.ltlabs.ltmechanic.constant.AppConfig.BASE_URL
import co.ltlabs.ltmechanic.constant.AppConfig.PING_URL
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

private const val TAG = "ConnectionUtil";

class ConnectionUtil {

    companion object {

        private val _internetConnected = MutableLiveData<Boolean>()
        val internetConnected: LiveData<Boolean>
            get() = _internetConnected

        fun setInternetConnected(connected: Boolean) {
            _internetConnected.value = connected
        }

        fun setInternetConnectedComplete() {
            _internetConnected.value = null
        }

        private fun hasNetworkAvailable(context: Context): Boolean {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val service = Context.CONNECTIVITY_SERVICE
                val manager = context.getSystemService(service) as ConnectivityManager?
                val n = manager?.activeNetwork
                if (n != null) {
                    val nc = manager.getNetworkCapabilities(n)
                    //It will check for both wifi and cellular network
                    return nc!!.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(
                        NetworkCapabilities.TRANSPORT_WIFI)
                }
                return false
            } else {
                val service = Context.CONNECTIVITY_SERVICE
                val manager = context.getSystemService(service) as ConnectivityManager?
                val network = manager?.activeNetworkInfo
                return (network != null)
            }
        }

        fun hasInternetConnected(context: Context): Boolean {
            return if (hasNetworkAvailable(context)) {
                try {
                    val connection = URL(PING_URL).openConnection() as HttpURLConnection
                    connection.setRequestProperty("User-Agent", "ConnectionTest-LT_PING")
                    connection.setRequestProperty("Connection", "close")
                    connection.connectTimeout = 60000 // configurable
                    connection.connect()
                    Log.d(TAG, "hasInternetConnected: has internet connection!")
                    (connection.responseCode == 200)
                } catch (e: IOException) {
                    Log.d(TAG, "hasInternetConnected: no internet connection!")
                    Log.e(TAG, "hasInternetConnected: ", e)
                    false
                }
            } else {
                false
            }
        }
    }
}