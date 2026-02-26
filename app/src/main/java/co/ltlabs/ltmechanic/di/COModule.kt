package co.ltlabs.ltmechanic.di

import co.ltlabs.ltmechanic.BuildConfig
import co.ltlabs.ltmechanic.constant.AppConfig
import co.ltlabs.ltmechanic.network.ApiCO
import co.ltlabs.ltmechanic.util.SharePrefUtil
import com.google.gson.Gson
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
class COModule {

    @Provides
    @Named(CO_MODULE_RETROFIT)
    fun provideRetrofitInstance(
        gson: Gson,
        @Named(CO_MODULE_OKHTTP)
        httpClient: OkHttpClient
    ): Retrofit {

        val okHttpBuilder = httpClient.newBuilder().build().newBuilder()
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level =
            if (BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        okHttpBuilder.addInterceptor(httpLoggingInterceptor)
        val baseUrl =
            (SharePrefUtil.getString(AppConfig.SP_COMPANY_BASE_URL, AppConfig.BASE_URL)
                ?: BASE_URL) + "/msv/machines/api/"
        return Retrofit.Builder()
            .client(okHttpBuilder.build())
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    @Named(CO_MODULE_OKHTTP)
    fun provideHttpClient(
        logging: HttpLoggingInterceptor,
        header: Interceptor
    ): OkHttpClient {
        return OkHttpClient().newBuilder()
            .addInterceptor(header)
            .addInterceptor(logging)
            .retryOnConnectionFailure(true)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideLoggingHttp(): HttpLoggingInterceptor {
        val logging = HttpLoggingInterceptor()
        if (BuildConfig.DEBUG) logging.level = HttpLoggingInterceptor.Level.BODY
        return logging
    }

    @Provides
    fun provideCOApi(
        @Named(CO_MODULE_RETROFIT)
        retrofit: Retrofit
    ) = retrofit.create(ApiCO::class.java)

    companion object {

        private const val BASE_URL = "https://ltm-feature.ltlabs.co"
        private const val CO_MODULE_RETROFIT = "CO_MODULE_RETROFIT"
        private const val CO_MODULE_OKHTTP = "CO_MODULE_OKHTTP"

    }

}