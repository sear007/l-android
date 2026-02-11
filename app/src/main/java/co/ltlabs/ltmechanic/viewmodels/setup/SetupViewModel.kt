package co.ltlabs.ltmechanic.viewmodels.setup

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import co.ltlabs.ltmechanic.constant.AppConfig.BASE_URL
import co.ltlabs.ltmechanic.constant.AppConfig.COMPANY_LOGIN
import co.ltlabs.ltmechanic.database.DatabaseLanguage
import co.ltlabs.ltmechanic.database.getDatabase
import co.ltlabs.ltmechanic.domain.AppConfigRequest
import co.ltlabs.ltmechanic.domain.CompanyInfoResponse
import co.ltlabs.ltmechanic.network.ApiCompanyLogin
import co.ltlabs.ltmechanic.network.ApiGlobal
import co.ltlabs.ltmechanic.network.auth.AuthApi
import co.ltlabs.ltmechanic.network.main.FileApi
import co.ltlabs.ltmechanic.repository.GlobalLoginRepository
import co.ltlabs.ltmechanic.repository.LtMechDatabaseRepository
import co.ltlabs.ltmechanic.util.*
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import javax.inject.Inject

private const val TAG = "SetupViewModel"

class SetupViewModel @Inject constructor(
        private val apiCompanyLogin: ApiCompanyLogin,
        private val fileApi: FileApi,
        private val languageJsonObject: JSONObject,
        application: Application) : AndroidViewModel(application) {

    private val database = getDatabase(application)
    private val ltMechDatabaseRepository = LtMechDatabaseRepository(database)

    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _status = MutableLiveData<ApiStatus>()
    val status: LiveData<ApiStatus>
        get() = _status

    private val _setupStatus = MutableLiveData<SetupStatus>()
    val setupStatus: LiveData<SetupStatus>
        get() = _setupStatus

    private val _languageStatus = MutableLiveData<LanguageStatus>()
    val languageStatus: LiveData<LanguageStatus>
        get() = _languageStatus

    private val _connected = MutableLiveData<Boolean>()
    val connected: LiveData<Boolean>
        get() = _connected

    val companyLoginStatus: MutableLiveData<ResponseUtil<CompanyInfoResponse>> = MutableLiveData()

    fun loginCompany(appConfig: AppConfigRequest) {

        companyLoginStatus.postValue(ResponseUtil.Loading())

        viewModelScope.launch {
            try {
                val loginCompanyDeferred = apiCompanyLogin.loginCompanyAsync(appConfig)
                val response = loginCompanyDeferred.await()
                companyLoginStatus.postValue(ResponseUtil.Success(response))

            } catch (e: HttpException) {
                companyLoginStatus.postValue(ResponseUtil.Error(e.message(), e))
            } catch (e: Exception) {
                companyLoginStatus.postValue(ResponseUtil.Error(e.message.toString(), e))
            }
        }
    }

    fun createConfigDirAndFile(context: Context) {

        viewModelScope.launch {

            val folderName = "LTMechanicConfig"
            val fileName = "config.txt"
            try {
                val folder = File(context.getExternalFilesDir(null), folderName)
                val file = File(folder.absolutePath, fileName)

                if (!folder.exists()) {
                    folder.mkdirs()
                }

                if (folder.exists()) {
                    file.createNewFile()
                    file.writeText("")

                    file.appendText("LANGUAGE:en \n")
//                    file.appendText("HOSTNAME:$API_SCHEME://$API_HOSTNAME \n")
                    println("==== baseURl FILE: $BASE_URL")
                    file.appendText("HOSTNAME:$BASE_URL \n")
                    file.appendText("HAS_NFC:false \n")
                    file.appendText("HAS_BARCODE:false \n")
                    file.appendText("TRANSLATION_VERSION:0 \n")
                    file.appendText("FACTORY:ltlabs \n")

                }
            } catch (e: Exception) {
                Log.e(TAG, "createConfigDirAndFile: ", e)
            }
        }
    }

    fun saveSettings(language: String) {

        viewModelScope.launch {

            _status.value = ApiStatus.LOADING

            try {

                _status.value = ApiStatus.DONE
                _setupStatus.value = SetupStatus.SUCCESS

            } catch (e: Exception) {
                Log.e(TAG, "saveSettings: ", e)
                _status.value = ApiStatus.ERROR
                _setupStatus.value = SetupStatus.FAILED
            }

        }

    }

    fun getLanguageFile(context: Context) {


        viewModelScope.launch {

            _status.value = ApiStatus.LOADING

            try {
                val getLanguageFileDeferred = fileApi.getLanguageFileAsync()
                val result = getLanguageFileDeferred.await()

                val writtenToDisk = FileUtil.writeResponseBodyToDisk(result, context)

                Log.d(TAG, "getLanguageFile: writtenToDisk: $writtenToDisk")

                Log.d(TAG, "getLanguageFile: result file name: ${result}")

                var index = 0


                val startTime = System.currentTimeMillis()
                Log.d(TAG, "getLanguageFile: startTime: $startTime")

                File("${context.getExternalFilesDir(null)}${File.separator}language.json").forEachLine {
                    val cleanStr = it.replace("\"data\":{", "").replace("{", "").replace("}", "")
                    val splitStr = cleanStr.split(":")
                    val key = splitStr[0].replace("\"", "")
                    val value = splitStr[1].replace("\"", "")
//                    translations.add(DatabaseTranslation(translationKey = key, translationValue = value))

                    languageJsonObject.put(key.toLowerCase().trim(), value)
                    LanguageUtil.languageJsonObject.put(key.toLowerCase().trim(), value)

                }

                val endTime = System.currentTimeMillis()
                Log.d(TAG, "getLanguageFile: endTime: $endTime")
                val elapsedTime = System.currentTimeMillis() - startTime

                Log.d(TAG, "getLanguageFile: elapsed time: ${elapsedTime } ms")

                Log.d(TAG, "getLanguageFile: LanguageUtil.languageJsonObject length:  ${languageJsonObject.length()}")


                _status.value = ApiStatus.DONE
                _languageStatus.value = LanguageStatus.SUCCESS
            } catch (t: Throwable) {
                Log.e(TAG, "getLanguageFile: ", t)

                _status.value = ApiStatus.ERROR
                _languageStatus.value = LanguageStatus.FAILED
            }
        }

    }

    fun testConnection(context: Context, language: String, endpoint: String, hasNFC: Boolean, hasBarcode: Boolean) {

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()


        val url = if (!endpoint.contains("$API_SCHEME://")) {
            if (ENV_TYPE == "STG_PROD") {
                "$API_SCHEME://$endpoint"
            } else {
                "$API_SCHEME://$endpoint:$API_PORT_AUTH"
            }
        } else {
            if (ENV_TYPE == "STG_PROD") {
                endpoint
            } else {
                "$endpoint:$API_PORT_AUTH"
            }
        }

        val okHttpBuilder = OkHttpClient.Builder().build().newBuilder()
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        okHttpBuilder.addInterceptor(httpLoggingInterceptor)

        val retrofit = Retrofit.Builder()
            .client(okHttpBuilder.build())
            .baseUrl("$url/")
            .addConverterFactory(MoshiConverterFactory.create(moshi).withNullSerialization())
//                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()

        val authApi = retrofit.create(AuthApi::class.java)

        viewModelScope.launch {

            _status.value = ApiStatus.LOADING

            //val loginDeferred = authApi.loginAsync(LoginRequest("test", "test"))

            try {

                //loginDeferred.await()

                _status.value = ApiStatus.LOADING

                val folderName = "LTMechanicConfig"
                val fileName = "config.txt"
                var content = StringBuilder()

                try {

                    val file = File("${context.getExternalFilesDir(null)}${File.separator}$folderName${File.separator}$fileName")
                    file.forEachLine { line ->

                        val split = line.split(":")
                        val key = split[0]

                        when {

                            line.contains("LANGUAGE") -> {

                                content.append("$key:$language\n")
                            }

                            line.contains("HOSTNAME") -> {
                                content.append("$key:$endpoint\n")
                            }

                            line.contains("HAS_NFC") -> {
                                content.append("$key:$hasNFC\n")
                            }

                            line.contains("HAS_BARCODE") -> {
                                content.append("$key:$hasBarcode\n")
                            }

                            line.contains("TRANSLATION_VERSION") -> {
                                content.append("$key:${LanguageUtil.translationVersion}\n")
                            }

                            line.contains("FACTORY") -> {
                                content.append("$key:${LanguageUtil.selectedFactory}\n")
                            }

                        }

                        file.writeText("")
                        file.writeText(content.toString())

                    }

                    insertLanguageDatabase(
                        arrayOf(
                            DatabaseLanguage(
                                selectedLanguage = language
                            )
                        )
                    )


                    _status.value = ApiStatus.DONE
//                    _setupStatus.value = SetupStatus.SUCCESS

                } catch (e: Exception) {
                    Log.e(TAG, "saveSettings: ", e)
                    _status.value = ApiStatus.ERROR
//                    _setupStatus.value = SetupStatus.FAILED
                }

                _connected.value = true

                _status.value = ApiStatus.DONE

            } catch (e: Exception) {
                Log.e(TAG, "testConnection: ", e)
                _status.value = ApiStatus.ERROR
                _connected.value = false
            }
        }


    }

    fun languageStatusComplete() {
        _languageStatus.value = null
    }

    fun insertLanguageDatabase(language: Array<DatabaseLanguage>) {
        viewModelScope.launch {
            ltMechDatabaseRepository.insertLanguage(language)
        }
    }

    fun connectedComplete() {
        _connected.value = false
        _connected.value = null
    }
}