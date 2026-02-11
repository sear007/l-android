package co.ltlabs.ltmechanic.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import co.ltlabs.ltmechanic.constant.AppConfig
import co.ltlabs.ltmechanic.domain.request.DeviceTokenRequest
import co.ltlabs.ltmechanic.network.ApiGlobal
import co.ltlabs.ltmechanic.util.SharePrefUtil
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import dagger.android.AndroidInjection
import kotlinx.coroutines.*
import me.pushy.sdk.Pushy
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject


class DeviceTokenService : Service() {

    @Inject
    lateinit var apiGlobal: ApiGlobal

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        generateToken(intent)
        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun generateToken(intent: Intent?) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val key = withContext(Dispatchers.Default) {
                    Pushy.register(applicationContext)
                }
                registerDeviceToken(key)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun registerDeviceToken(token: String) {
        val deviceId: String = Settings.Secure.getString(
            this.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        apiGlobal.deviceRegister(DeviceTokenRequest(
            uuid = deviceId,
            token = token,
            region = null
        ))
            .enqueue(object : Callback<Any> {
                override fun onResponse(
                    call: Call<Any>,
                    response: Response<Any>
                ) {
                    SharePrefUtil.set(AppConfig.DEVICE_TOKEN, token)
                    call.cancel()
                    stopSelf()
                }

                override fun onFailure(call: Call<Any>, t: Throwable) {
                    call.cancel()
                    stopSelf()
                }
            })
    }
}