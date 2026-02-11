package co.ltlabs.ltmechanic.network

import co.ltlabs.ltmechanic.constant.AppConfig
import co.ltlabs.ltmechanic.di.AppModule
import co.ltlabs.ltmechanic.util.AuthUtil
import co.ltlabs.ltmechanic.util.SharePrefUtil
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber


class AuthTokenInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val refreshToken =
            SharePrefUtil.getString(AppConfig.SP_USER_REFRESH_TOKEN, "empty") ?: "empty"

        return if (response.code() == 403) {
            val body = response.body()?.string()
            val data = Gson().fromJson(body, JsonObject::class.java)
            val error = data.get("error")?.asJsonObject

            //Check status
            if (error != null) {
                val errorName = error.get("name")?.asString
                if (errorName == "TokenExpiredError" || errorName == "JsonWebTokenError") {
                    if (AppConfig.IS_REFRESHING_TOKEN == 0) {
                        AppConfig.IS_REFRESHING_TOKEN = 1
                        Timber.e(">>>> Expired token on = ${chain.request().url()}")
                        Timber.e(">>>> Refreshing Token...")

                        response.close()
                        runBlocking {
                            Timber.e(">>> Refresh : $refreshToken")
                            val deferred = authApi().refreshTokenAsync(refreshToken ?: "")
                            val result = deferred.await()
                            try {
                                val token =
                                    result.asJsonObject?.get("accessToken")?.asString
                                val refresh =
                                    result.asJsonObject?.get("refreshToken")?.asString

                                //Save to preference
                                token?.let {
                                    AuthUtil.token = it
                                    SharePrefUtil.set(AppConfig.SP_USER_TOKEN, token)
                                }
                                refresh?.let {
                                    SharePrefUtil.set(
                                        AppConfig.SP_USER_REFRESH_TOKEN,
                                        refresh
                                    )
                                }
                                Timber.e(">>>> Proceed token : ${token?.split(".")?.get(2)}")

                                val request = chain.request()
                                val newReq = request.newBuilder()
                                    .header("Authorization", "Bearer $token")
                                    .build()
                                delay(1500)
                                AppConfig.IS_REFRESHING_TOKEN = 0
                                chain.proceed(newReq)
                            } catch (e: Exception) {
                                Timber.e(">>> Error : ${e.localizedMessage}")
                                AppConfig.IS_REFRESHING_TOKEN = 0
                                chain.proceed(requestBuilder(chain))
                            }
                        }
                    } else {
                        //Problem can't revoke 403 requests after refresh token is done, so keep it in queue for 10s
                        runBlocking {
                            delay(10000)
                            val token = SharePrefUtil.getString(AppConfig.SP_USER_TOKEN, "")
                            val request = chain.request()
                            val newReq = request.newBuilder()
                                .header("Authorization", "Bearer $token")
                                .build()
                            chain.proceed(newReq)
                        }
                    }
                } else {
                    chain.proceed(requestBuilder(chain))
                }
            } else {
                chain.proceed(requestBuilder(chain))
            }
        } else {
            response
        }
    }

    private fun requestBuilder(chain: Interceptor.Chain): Request {
        //We cannot use original response due to we use .string() body already,
        // Solution is to recreate new request
        val newRequest = chain.request().newBuilder()
        return newRequest.build()
    }


    private fun authApi(): ApiGlobal {
        val okHttpBuilder = OkHttpClient.Builder().build().newBuilder()
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        okHttpBuilder.addInterceptor(httpLoggingInterceptor)
        val baseUrl = SharePrefUtil.getString(
            AppConfig.SP_GLOBAL_BASE_URL,
            AppConfig.GLOBAL_BASE_URL
        ) ?: AppConfig.GLOBAL_BASE_URL
        val retrofit = Retrofit.Builder()
            .client(okHttpBuilder.build())
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()

        return retrofit.create(ApiGlobal::class.java)
    }

    private fun getHttpClient(): OkHttpClient.Builder {
        return OkHttpClient.Builder()
            .addInterceptor {
                val request = it.request()
                val newUrl: HttpUrl? = request.url().newBuilder()
                    .scheme(AppModule.scheme)
                    .host(AppModule.host)
                    .build()
                it.proceed(request.newBuilder().url(newUrl).build())
            }
    }
}