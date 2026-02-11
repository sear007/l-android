package co.ltlabs.ltmechanic.util

import android.util.Base64
import android.util.Log
import java.nio.charset.Charset

private const val TAG = "JWTUtil";

class JWTUtil {

    companion object {

        fun decoded(jwtEncoded: String) {
            try {

                val split = jwtEncoded.split("\\.")
                Log.d(TAG, "decoded: Header: ${getJson(split[0])}")
                Log.d(TAG, "decoded: Body: ${getJson(split[1])}")
            } catch (e: Exception) {
                Log.e(TAG, "decoded: ", e)
            }
        }

        fun getJson(strEncoded: String): String {
            val decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE)
            return String(decodedBytes, Charset.forName("UTF-8"))
        }
    }
}