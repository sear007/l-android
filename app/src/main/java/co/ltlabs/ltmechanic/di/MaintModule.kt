package co.ltlabs.ltmechanic.di

import co.ltlabs.ltmechanic.BuildConfig
import co.ltlabs.ltmechanic.constant.AppConfig
import co.ltlabs.ltmechanic.network.ApiMaint
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
class MaintModule {

    @Provides
    @Named(MAINT_MODULE_RETROFIT)
    fun provideRetrofitInstance(
        gson: Gson,
        @Named(MAINT_MODULE_OKHTTP)
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
                ?: BASE_URL) + "/msv/tickets/api/"
        return Retrofit.Builder()
            .client(okHttpBuilder.build())
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    @Named(MAINT_MODULE_OKHTTP)
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
    fun provideMaintApi(
        @Named(MAINT_MODULE_RETROFIT)
        retrofit: Retrofit
    ) = retrofit.create(ApiMaint::class.java)

    companion object {

        private const val BASE_URL = "https://ltm-feature.ltlabs.co"
        private const val MAINT_MODULE_RETROFIT = "MAINT_MODULE_RETROFIT"
        private const val MAINT_MODULE_OKHTTP = "MAINT_MODULE_OKHTTP"

    }

}