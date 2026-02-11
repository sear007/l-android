package co.ltlabs.ltmechanic.di

import android.app.Application
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.constant.AppConfig
import co.ltlabs.ltmechanic.constant.AppConfig.GLOBAL_BASE_URL
import co.ltlabs.ltmechanic.constant.AppConfig.GLOBAL_URL
import co.ltlabs.ltmechanic.constant.type.AccessType
import co.ltlabs.ltmechanic.constant.type.COStatusType
import co.ltlabs.ltmechanic.constant.type.IsCriticalMachineType
import co.ltlabs.ltmechanic.constant.type.MaintType
import co.ltlabs.ltmechanic.network.ApiCompanyLogin
import co.ltlabs.ltmechanic.network.ApiGlobal
import co.ltlabs.ltmechanic.network.AuthTokenInterceptor
import co.ltlabs.ltmechanic.network.auth.Reference2Api
import co.ltlabs.ltmechanic.network.main.FileApi
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.util.dialog.LoadingIndicator
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module(
    includes = [
        ReceiverModule::class,
        HelperModule::class,
        COModule::class,
        MaintModule::class
    ]
)
class AppModule {

    @Provides
    fun provideLoadingIndicator(): LoadingIndicator = LoadingIndicator()

    companion object {

        const val MACHINE = "machine"
        const val MACHINE_OKHTTP = "machineOkHttp"
        const val LINE = "line"
        const val LINE_OKHTTP = "lineOkHttp"
        const val REFERENCE = "reference"
        const val REFERENCE_OKHTTP = "referenceOkHttp"
        const val REFERENCE2 = "reference2"
        const val TICKET = "ticket"
        const val TICKET_OKHTTP = "ticketOkHttp"
        const val FILE = "file"
        const val FILE_OKHTTP = "fileOkHttp"
        const val AUTH = "auth"
        const val AUTH_OKHTTP = "authOkHttp"
        const val AUTH2 = "auth2"
        const val GLOBAL = "global"
        const val GLOBAL_OKHTTP = "globalhttp"
        const val COMPANY_LOGIN_NAME = "companylogin"

        var scheme: String = "http"
            set(url) {
                field = url.split("://")[0]
            }

        var host: String = API_HOSTNAME

        @Singleton
        @Provides
        fun provideMoshiInstance(): Moshi =
            Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

        @Singleton
        @Provides
        @Named(MACHINE)
        fun provideMachineRetrofitInstance(
            @Named(MACHINE_OKHTTP)
            httpClient: OkHttpClient.Builder,
            gson: Gson
        ): Retrofit {
            val okHttpBuilder = httpClient.build().newBuilder()
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            okHttpBuilder.addInterceptor(httpLoggingInterceptor)
            okHttpBuilder.addInterceptor(AuthTokenInterceptor())

            return Retrofit.Builder()
                .client(okHttpBuilder.build())
                .baseUrl(API_BASE_URL_MACHINES)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .build()
        }


        @Singleton
        @Provides
        @Named(LINE)
        fun provideLineRetrofitInstance(
            gson: Gson,
            @Named(LINE_OKHTTP)
            httpClient: OkHttpClient.Builder,
        ): Retrofit {

            val okHttpBuilder = httpClient.build().newBuilder()
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            okHttpBuilder.addInterceptor(httpLoggingInterceptor)
            okHttpBuilder.addInterceptor(AuthTokenInterceptor())

            return Retrofit.Builder()
                .client(okHttpBuilder.build())
                .baseUrl(API_BASE_URL_LINES)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .build()
        }

        @Singleton
        @Provides
        @Named(REFERENCE)
        fun provideReferenceRetrofitInstance(
            @Named(REFERENCE_OKHTTP)
            httpClient: OkHttpClient.Builder,
            gson: Gson
        ): Retrofit {

            val okHttpBuilder = httpClient.build().newBuilder()
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            okHttpBuilder.addInterceptor(httpLoggingInterceptor)
            okHttpBuilder.addInterceptor(AuthTokenInterceptor())

            return Retrofit.Builder()
                .client(okHttpBuilder.build())
                .baseUrl(API_BASE_URL_REFERENCES)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .build()
        }

        @Singleton
        @Provides
        @Named(REFERENCE2)
        fun provideReference2RetrofitInstance(
            moshi: Moshi,
            @Named(REFERENCE_OKHTTP)
            httpClient: OkHttpClient.Builder
        ): Retrofit {

            val okHttpBuilder = httpClient.build().newBuilder()
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            okHttpBuilder.addInterceptor(httpLoggingInterceptor)
            okHttpBuilder.addInterceptor(AuthTokenInterceptor())

            return Retrofit.Builder()
                .client(okHttpBuilder.build())
                .baseUrl(API_BASE_URL_REFERENCES)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .build()
        }

        @Singleton
        @Provides
        @Named(TICKET)
        fun provideTicketRetrofitInstance(
            @Named(TICKET_OKHTTP)
            httpClient: OkHttpClient.Builder,
            gson: Gson
        ): Retrofit {

            val okHttpBuilder = httpClient.build().newBuilder()
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            okHttpBuilder.addInterceptor(httpLoggingInterceptor)
            okHttpBuilder.addInterceptor(AuthTokenInterceptor())

            return Retrofit.Builder()
                .baseUrl(API_BASE_URL_TICKETS)
                .client(
                    okHttpBuilder
                        .connectTimeout(120, TimeUnit.SECONDS)
                        .readTimeout(120, TimeUnit.SECONDS)
                        .writeTimeout(120, TimeUnit.SECONDS)
                        .build()
                )
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .build()
        }

        @Singleton
        @Provides
        @Named(FILE)
        fun provideFileRetrofitInstance(
            moshi: Moshi,
            @Named(FILE_OKHTTP)
            httpClient: OkHttpClient.Builder
        ): Retrofit {
            val okHttpBuilder = httpClient.build().newBuilder()
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            okHttpBuilder.addInterceptor(httpLoggingInterceptor)
            okHttpBuilder.addInterceptor(AuthTokenInterceptor())


            return Retrofit.Builder()
                .client(
                    okHttpBuilder
                        .connectTimeout(60, TimeUnit.SECONDS)
                        .readTimeout(60, TimeUnit.SECONDS)
                        .writeTimeout(60, TimeUnit.SECONDS)
                        .build()
                )
                .baseUrl(API_BASE_URL_FILES)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .build()
        }

        @Singleton
        @Provides
        @Named(AUTH)
        fun provideAuthRetrofitInstance(
            moshi: Moshi,
            @Named(AUTH_OKHTTP)
            httpClient: OkHttpClient.Builder
        ): Retrofit {

            val okHttpBuilder = httpClient.build().newBuilder()
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            okHttpBuilder.addInterceptor(httpLoggingInterceptor)

            return Retrofit.Builder()
                .client(okHttpBuilder.build())
                .baseUrl(API_BASE_URL_AUTH)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .build()
        }

        @Singleton
        @Provides
        @Named(AUTH2)
        fun provideAuth2RetrofitInstance(
            @Named(AUTH_OKHTTP)
            httpClient: OkHttpClient.Builder,
            gson: Gson
        ): Retrofit {

            val okHttpBuilder = httpClient.build().newBuilder()
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            okHttpBuilder.addInterceptor(httpLoggingInterceptor)

            return Retrofit.Builder()
                .client(okHttpBuilder.build())
                .baseUrl(API_BASE_URL_AUTH)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .build()
        }

        @Singleton
        @Provides
        @Named(MACHINE_OKHTTP)
        fun provideMachinesOkHttpClientInstance(
            header: Interceptor
        ): OkHttpClient.Builder =
            OkHttpClient.Builder()
                .addInterceptor(header)
                .addInterceptor {
                    val request = it.request()

                    val newUrl: HttpUrl?
                    newUrl = when {
                        scheme != null && host != null -> {
                            if (ENV_TYPE == "STG_PROD") {
                                request.url().newBuilder()
                                    .scheme(scheme)
                                    .host(host)
                                    .build()
                            } else {
                                request.url().newBuilder()
                                    .scheme(scheme)
                                    .host(host)
                                    .port(API_PORT_MACHINES)
                                    .build()
                            }
                        }
                        else -> {
                            request.url()
                                .newBuilder()
                                .build()
                        }

                    }
                    it.proceed(
                        request.newBuilder()
                            .url(newUrl)
                            .build()
                    )

                }

        @Singleton
        @Provides
        @Named(REFERENCE_OKHTTP)
        fun provideReferencesOkHttpClientInstance(
            header: Interceptor
        ): OkHttpClient.Builder =
            OkHttpClient.Builder()
                .addInterceptor(header)
                .addInterceptor {
                    val request = it.request()

                    val newUrl: HttpUrl?
                    newUrl = when {
                        scheme != null && host != null -> {
                            if (ENV_TYPE == "STG_PROD") {
                                request.url().newBuilder()
                                    .scheme(scheme)
                                    .host(host)
                                    .build()
                            } else {
                                request.url().newBuilder()
                                    .scheme(scheme)
                                    .host(host)
                                    .port(API_PORT_REFERENCES)
                                    .build()
                            }
                        }
                        else -> {
                            request.url()
                                .newBuilder()
                                .build()
                        }

                    }
                    it.proceed(
                        request.newBuilder()
                            .url(newUrl)
                            .build()
                    )

                }

        @Singleton
        @Provides
        @Named(LINE_OKHTTP)
        fun provideLinesOkHttpClientInstance(
            header: Interceptor
        ): OkHttpClient.Builder =
            OkHttpClient.Builder()
                .addInterceptor(header)
                .addInterceptor {
                    val request = it.request()

                    val newUrl: HttpUrl?
                    newUrl = when {
                        scheme != null && host != null -> {
                            if (ENV_TYPE == "STG_PROD") {
                                request.url().newBuilder()
                                    .scheme(scheme)
                                    .host(host)
                                    .build()
                            } else {
                                request.url().newBuilder()
                                    .scheme(scheme)
                                    .host(host)
                                    .port(API_PORT_LINES)
                                    .build()
                            }
                        }
                        else -> {
                            request.url()
                                .newBuilder()
                                .build()
                        }

                    }
                    it.proceed(
                        request.newBuilder()
                            .url(newUrl)
                            .build()
                    )

                }

        @Singleton
        @Provides
        @Named(TICKET_OKHTTP)
        fun provideTicketsOkHttpClientInstance(
            header: Interceptor
        ): OkHttpClient.Builder =
            OkHttpClient.Builder()
                .addInterceptor(header)
                .addInterceptor {
                    val request = it.request()

                    val newUrl: HttpUrl?
                    newUrl = when {
                        scheme != null && host != null -> {
                            if (ENV_TYPE == "STG_PROD") {
                                request.url().newBuilder()
                                    .scheme(scheme)
                                    .host(host)
                                    .build()
                            } else {
                                request.url().newBuilder()
                                    .scheme(scheme)
                                    .host(host)
                                    .port(API_PORT_TICKETS)
                                    .build()
                            }
                        }
                        else -> {
                            request.url()
                                .newBuilder()
                                .build()
                        }

                    }
                    it.proceed(
                        request.newBuilder()
                            .url(newUrl)
                            .build()
                    )

                }

        @Singleton
        @Provides
        @Named(FILE_OKHTTP)
        fun provideFilesOkHttpClientInstance(): OkHttpClient.Builder =
            OkHttpClient.Builder()
                .addInterceptor {
                    val request = it.request()

                    val newUrl: HttpUrl?
                    newUrl = when {
                        scheme != null && host != null -> {
                            if (ENV_TYPE == "STG_PROD") {
                                request.url().newBuilder()
                                    .scheme(scheme)
                                    .host(
                                        if (ENV_TYPE == "DEV") {
                                            API_FILES_DEV_HOSTNAME
                                        } else {
                                            host
                                        }
                                    )
                                    .build()
                            } else {
                                request.url().newBuilder()
                                    .scheme(scheme)
                                    .host(
                                        if (ENV_TYPE == "DEV") {
                                            API_FILES_DEV_HOSTNAME
                                        } else {
                                            host
                                        }
                                    )
                                    .port(API_PORT_FILES)
                                    .build()
                            }
                        }
                        else -> {
                            request.url()
                                .newBuilder()
                                .build()
                        }

                    }
                    it.proceed(
                        request.newBuilder()
                            .url(newUrl)
                            .build()
                    )

                }

        @Singleton
        @Provides
        @Named(AUTH_OKHTTP)
        fun provideAuthOkHttpClientInstance(
            header: Interceptor
        ): OkHttpClient.Builder =
            OkHttpClient.Builder()
                .addInterceptor(header)
                .addInterceptor {
                    val request = it.request()

                    val newUrl: HttpUrl?
                    newUrl = when {
                        scheme != null && host != null -> {
                            if (ENV_TYPE == "STG_PROD") {
                                request.url().newBuilder()
                                    .scheme(scheme)
                                    .host(host)
                                    .build()
                            } else {
                                request.url().newBuilder()
                                    .scheme(scheme)
                                    .host(host)
//                                    .port(API_PORT_AUTH)
                                    .build()
                            }
                        }
                        else -> {
                            request.url()
                                .newBuilder()
                                .build()
                        }

                    }
                    it.proceed(
                        request.newBuilder()
                            .url(newUrl)
                            .build()
                    )

                }


        @Singleton
        @Provides
        fun provideRequestOptions(): RequestOptions =
            RequestOptions
                .placeholderOf(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)

        @Singleton
        @Provides
        fun provideGlideInstance(
            application: Application,
            requestOptions: RequestOptions
        ): RequestManager =
            Glide.with(application)
                .setDefaultRequestOptions(requestOptions)

        @Singleton
        @Provides
        fun provideLanguageJsonObject() =
            JSONObject()

        @Singleton
        @Provides
        fun provideFileApi(@Named(FILE) retrofit: Retrofit): FileApi =
            retrofit.create(FileApi::class.java)

        @Singleton
        @Provides
        @Named(GLOBAL_OKHTTP)
        fun provideOkHttpClient(
            header: Interceptor
        ): OkHttpClient {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

            val okHttp = OkHttpClient().newBuilder()
            okHttp.addInterceptor(httpLoggingInterceptor)
            okHttp.addInterceptor(AuthTokenInterceptor())
            okHttp.addInterceptor(header)
            okHttp.callTimeout(40, TimeUnit.SECONDS)
            okHttp.connectTimeout(5, TimeUnit.MINUTES)
            okHttp.readTimeout(5, TimeUnit.MINUTES)
            okHttp.writeTimeout(30, TimeUnit.SECONDS)
            okHttp.build()

            return okHttp.build()
        }

        @Provides
        @Named(GLOBAL)
        fun provideGlobalRetrofit(
            @Named(GLOBAL_OKHTTP)
            okHttpClient: OkHttpClient,
            gson: Gson
        ): Retrofit {
            val url = GLOBAL_BASE_URL.ifEmpty { "https://globaladmin-sg.ltlabs.co/msv/global-admin/api/v1/" }
            return Retrofit.Builder()
                .baseUrl(url)
                .client(okHttpClient)
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }

        @Provides
        fun provideGlobalRepository(
            @Named(GLOBAL)
            retrofit: Retrofit
        ): ApiGlobal = retrofit.create(ApiGlobal::class.java)

        @Singleton
        @Provides
        @Named(COMPANY_LOGIN_NAME)
        fun provideCompanyLoginRetrofit(
            @Named(GLOBAL_OKHTTP)
            okHttpClient: OkHttpClient,
            gson: Gson
        ): Retrofit {

            return Retrofit.Builder()
                .baseUrl(GLOBAL_URL)
                .client(okHttpClient)
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }

        @Singleton
        @Provides
        fun provideCompanyLogin(@Named(COMPANY_LOGIN_NAME) retrofit: Retrofit): ApiCompanyLogin =
            retrofit.create(ApiCompanyLogin::class.java)

        @Singleton
        @Provides
        @Named("for_main")
        fun provideReference2Api(@Named("reference2") retrofit: Retrofit): Reference2Api =
            retrofit.create(Reference2Api::class.java)

        @Provides
        @Singleton
        fun provideGSon(): Gson {
            val accessTypeAdapter = JsonDeserializer { json, _, _ ->
                AccessType.convertCodeToType(json.asString)
            }
            val coTypeAdapter = JsonDeserializer { json, _, _ ->
                COStatusType.fromStringToType(json.asString)
            }
            val maintTypeAdapter = JsonDeserializer { json, _, _ ->
                MaintType.fromStringToType(json.asString)
            }
            val isCritical = JsonDeserializer { json, _, _ ->
                IsCriticalMachineType.fromCodeToType(json.asInt)
            }

            val dateTypeAdapter: JsonDeserializer<Date?> = JsonDeserializer { json, _, _ ->
                var serverFormat: SimpleDateFormat
                try {
                    serverFormat = SimpleDateFormat(DateUtil.SERVER_DATE_TIME_FORMAT, Locale.getDefault())
                    serverFormat.parse(json.asString)
                } catch (e: Exception) {
                    serverFormat = SimpleDateFormat(DateUtil.SERVER_DATE_TIME_FORMAT_UTC_0, Locale.getDefault())
                    serverFormat.parse(json.asString)
                }
            }

            return GsonBuilder()
                .registerTypeAdapter(Date::class.java, dateTypeAdapter)
                .registerTypeAdapter(AccessType::class.java, accessTypeAdapter)
                .registerTypeAdapter(COStatusType::class.java, coTypeAdapter)
                .registerTypeAdapter(MaintType::class.java, maintTypeAdapter)
                .registerTypeAdapter(IsCriticalMachineType::class.java, isCritical)
                .setLenient()
                .create()
        }

        @Provides
        @Singleton
        fun provideHeaderInterceptor(): Interceptor {
            return Interceptor(fun(chain: Interceptor.Chain): Response {
                val token = SharePrefUtil.getString(AppConfig.SP_USER_TOKEN, "")
                val request = chain.request()
                    .newBuilder()
                    .header("Authorization", "Bearer $token")
                return chain.proceed(request.build())
            })
        }

    }
}