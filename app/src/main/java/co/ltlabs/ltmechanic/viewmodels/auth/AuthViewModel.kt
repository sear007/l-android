package co.ltlabs.ltmechanic.viewmodels.auth

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import co.ltlabs.ltmechanic.constant.AppConfig.APP_NAME
import co.ltlabs.ltmechanic.constant.AppConfig.COMPANY_CODE
import co.ltlabs.ltmechanic.constant.AppConfig.SELECTED_FACTORY
import co.ltlabs.ltmechanic.database.DatabaseAuthDetails
import co.ltlabs.ltmechanic.database.DatabaseLanguage
import co.ltlabs.ltmechanic.database.getDatabase
import co.ltlabs.ltmechanic.di.AppModule
import co.ltlabs.ltmechanic.domain.*
import co.ltlabs.ltmechanic.network.ApiGlobal
import co.ltlabs.ltmechanic.network.LoginRequest
import co.ltlabs.ltmechanic.network.auth.AuthApi
import co.ltlabs.ltmechanic.network.auth.Reference2Api
import co.ltlabs.ltmechanic.network.auth.dto.asLoginAccessDomain
import co.ltlabs.ltmechanic.network.main.FileApi
import co.ltlabs.ltmechanic.repository.GlobalLoginRepository
import co.ltlabs.ltmechanic.repository.LtMechDatabaseRepository
import co.ltlabs.ltmechanic.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.HttpUrl
import org.json.JSONObject
import retrofit2.HttpException
import timber.log.Timber
import java.io.File
import javax.inject.Inject

private const val TAG = "AuthViewModel";

class AuthViewModel @Inject constructor(
    private val apiGlobal: ApiGlobal,
    private val authApi: AuthApi,
    private val languageJsonObject: JSONObject,
    private val referenceApi: Reference2Api,
    private val fileApi: FileApi,
    application: Application
) : AndroidViewModel(application) {

    private val repository = GlobalLoginRepository(apiGlobal)

    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val database = getDatabase(application)
    private val ltMechDatabaseRepository = LtMechDatabaseRepository(database)

    val loginDetailsFromDatabase = ltMechDatabaseRepository.authDetails

    val languageFromDatabase = ltMechDatabaseRepository.language

    private val _languages = MutableLiveData<List<Language>>()
    val languages: LiveData<List<Language>>
        get() = _languages

    private val _authDetails = MutableLiveData<AuthDetails>()
    val authDetails: LiveData<AuthDetails>
        get() = _authDetails

    private val _status = MutableLiveData<ApiStatus>()
    val status: LiveData<ApiStatus>
        get() = _status

    private val _loginStatus = MutableLiveData<LoginStatus>()
    val loginStatus: LiveData<LoginStatus>
        get() = _loginStatus

    private val _transLationFileLoaded = MutableLiveData<Boolean>()
    val transLationFileLoaded: LiveData<Boolean>
        get() = _transLationFileLoaded

    private val _callStatus = MutableLiveData<ApiCallStatus>()
    val callStatus: LiveData<ApiCallStatus>
        get() = _callStatus

    private val _translation = MutableLiveData<Boolean>()
    val translation: LiveData<Boolean>
        get() = _translation

//    private val _appStoreStatus = MutableLiveData<AppStoreInfo>()
//    val appStoreStatus: LiveData<AppStoreInfo>
//        get() = _appStoreStatus
//
//    fun getAppInfo() {
//        viewModelScope.launch {
//            try {
//                val getAppInfoDeferred = referenceApi.getAppInfoAsync(AppStore(APP_NAME))
//                val response = getAppInfoDeferred.await()
//                if (response.isNotEmpty()) {
//                    _appStoreStatus.value = response[0]
//                } else {
//                    _appStoreStatus.value = null
//                }
//
//            } catch (t: Throwable) {
//                _appStoreStatus.value = null
//                Log.e(TAG, "getAppInfo: ${ t.localizedMessage}")
//            }
//        }
//    }

    val employeeLoginStatus: MutableLiveData<ResponseUtil<EmployeeResponse>> = MutableLiveData()

    fun loginEmployee(employee: Employee) {
        employeeLoginStatus.postValue(ResponseUtil.Loading())
        viewModelScope.launch {
            val loginEmployeeDeferred = repository.loginEmployeeAsync(employee)
            try {
                val response = loginEmployeeDeferred.await()
                employeeLoginStatus.postValue(ResponseUtil.Success(response))
                _authDetails.value = AuthDetails(
                    response.accessToken,
                    response.role,
                    true,
                    employee.username
                )
                SELECTED_FACTORY = response.factory
            } catch (e: HttpException) {
                employeeLoginStatus.postValue(ResponseUtil.Error(e.message(), e))
            } catch (e: Exception) {
                employeeLoginStatus.postValue(ResponseUtil.Error(e.message.toString(), e))
            }
        }
    }

    fun loginEmployeeWithRfidAsync(rfidRequest: RfidRequest) {
        employeeLoginStatus.postValue(ResponseUtil.Loading())
        viewModelScope.launch {
            val loginEmployeeDeferred = repository.loginEmployeeWithRfidAsync(rfidRequest)
            try {
                val response = loginEmployeeDeferred.await()
                employeeLoginStatus.postValue(ResponseUtil.Success(response))
                _authDetails.value = AuthDetails(
                    response.accessToken,
                    response.role,
                    true,
                    response.username
                )
                SELECTED_FACTORY = response.factory
            } catch (e: HttpException) {
                employeeLoginStatus.postValue(ResponseUtil.Error(e.message(), e))
            } catch (e: Exception) {
                employeeLoginStatus.postValue(ResponseUtil.Error(e.message.toString(), e))
            }
        }
    }

//    fun checkTokenValidity(token: String) {
//
//        viewModelScope.launch {
//
//            val getNonLineAreasDeferred = referenceApi.getNonLineAreasAsync("Bearer $token")
////            val getNonLineAreasDeferred = referenceApi.getNonLineAreasAsync(
////                "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjcsImZhY3RvcnlJZCI6MSwiaWF0IjoxNTkzMTMzODgxLCJleHAiOjE1OTMyMjAyODF9.XJT6EE4autU_olM5hYCajiZiZdWz-Hx2Acy_i_X4YU4"
////            )
//
//            try {
//
//                val result = getNonLineAreasDeferred.await()
//                _callStatus.value = ApiCallStatus.SUCESS
//
//            } catch (t: Throwable) {
//
//
//                if (t is HttpException) {
//
//                    val error = t.response()?.errorBody()?.string()
//                    if (error?.contains("Unauthorized")!!) {
//                        _callStatus.value = ApiCallStatus.UNAUTHORIZED
//                    } else {
//                        val gson = Gson()
//                        val errorObj: String? = error
//                        val errorJson = gson.fromJson(errorObj, Error2::class.java)
//
//                        Log.d(TAG, "checkTokenValidity: error: $error")
//                        Log.d(TAG, "checkTokenValidity: errorObj: $errorJson")
////                    Log.d(TAG, "checkInMachine: errorObj: $errorObj")
//
//
//                        if (errorJson.error?.message?.contains("expired") == true) {
//                            _callStatus.value = ApiCallStatus.TOKEN_EXPIRED
//                        } else {
//                            _callStatus.value = ApiCallStatus.SUCESS
//                        }
//                    }
//
//                } else {
//                    _callStatus.value = ApiCallStatus.SUCESS
//                }
//
//            }
//        }
//    }

    fun loadTranslationFile(context: Context) {
        try {

            //Comment By Socheat on 29/07/2021
            //Reason : App Auth Login not translate due to wrong parseing json logic
            //It wil always to to exception
            /* File("${context.getExternalFilesDir(null)}${File.separator}language.json").forEachLine {
                 val languages = it.replace("\"data\":{", "").replace("{", "").replace("}", "").split(",")


                 val version = JSONObject(it)["version"]
                 val languageStr = "{${it.replace("{", "").replace("}", "").replace("\"new\":true,\"version\":$version,\"data\":", "")}}"
                 val newLanguages = languageStr.split(":").toTypedArray()
                 val upperHalf = newLanguages.copyOfRange(0, (newLanguages.size + 1) / 2)
                 val lowerHalf = newLanguages.copyOfRange((newLanguages.size + 1) / 2, newLanguages.size)

 //                Log.d(TAG, "half: upperHalf: ${upperHalf.toMutableList()}")
 //                Log.d(TAG, "half: lowerHalf: ${lowerHalf.toMutableList()}")
 //                Log.d(TAG, "languageStr: $languageStr")
 //                Log.d(TAG, "JSONObject(it): ${JSONObject(languageStr)["Request send sucessfully"]}")

 //                languages.forEach { language ->
 //                    val splitStr = language.split(":")
 //                    val key = splitStr[0].replace("\"", "")
 //                    val value = splitStr[1].replace("\"", "").replace("\\r", "")
 //                    languageJsonObject.put(key.toLowerCase().trim(), value)
 //                }

                 val newLanguageObject = JSONObject(languageStr)

                 newLanguageObject.keys().forEach { key ->
                     Log.d(TAG, "key: $key, value: ${newLanguageObject["$key"]}")
                     val keyVal = key.toString().replace("\"", "")
                     val value = newLanguageObject["$key"].toString().replace("\"", "").replace("\\r", "")
                     languageJsonObject.put(keyVal.toLowerCase().trim(), value)
                     LanguageUtil.languageJsonObject.put(keyVal.toLowerCase().trim(), value)
                 }

 //                languageJsonObject = JSONObject(languageStr)
 //                LanguageUtil.languageJsonObject = JSONObject(languageStr)
 //                Log.d(TAG, "Showing All lines: ${LanguageUtil.languageJsonObject["Showing All lines"]}")
 //                Log.d(TAG, "Showing All lines: ${languageJsonObject["Showing All lines"]}")

 //                languageJsonObject.put(key, value)


             }*/

            val file = File("${context.getExternalFilesDir(null)}${File.separator}language.json")
            file.forEachLine {
                val regex = Regex("""\,(?=([^"]*"[^"]*")*[^"]*$)""")
                val languages = it.replace("{", "")
                    .replace("}", "")
                    .replace("\"data\":", "")
                    .split(regex)
                for (language in languages) {
                    try {
                        val splitStr = language.split(":")
                        val key = splitStr[0].replace("\"", "")
                        val value = splitStr[1].replace("\"", "").replace("\\r", "")
                        languageJsonObject.put(key.toLowerCase().trim(), value)
                        LanguageUtil.languageJsonObject.put(key.toLowerCase().trim(), value)
                    } catch (e: java.lang.Exception) {
                        _transLationFileLoaded.value = false
                        Timber.e(e.localizedMessage)
                    }
                }
            }

            _transLationFileLoaded.value = true
            Log.d(
                TAG,
                "getLanguageFile: LanguageUtil.languageJsonObject length:  ${languageJsonObject.length()}"
            )

        } catch (e: Exception) {

            _transLationFileLoaded.value = false
            Log.e(TAG, "loadTranslationFile: ", e)
        }
    }

    fun login(username: String, password: String) {

        viewModelScope.launch {

            val loginDeferred = authApi.loginAsync(LoginRequest(username, password))

            try {

                _status.value = ApiStatus.LOADING

                val result = loginDeferred.await()

                if (result.token != null) {
                    Log.d(TAG, "login: result: $result")

                    val encoded = Crypto.encryptAndEncode(password)
                    AuthUtil.password = password
                    AuthUtil.refresh_token = result.refresh_token ?: ""

                    _loginStatus.value = LoginStatus.SUCCESS

                    var role = result.role

                    result.access?.asLoginAccessDomain()?.let {
                        if (it.isNotEmpty()) {
                            role = it[0].role
                        }
                    }

                    _authDetails.value = AuthDetails(
                        result.token,
                        role,
                        true,
                        username
                    )

                    Log.d(TAG, "login: token: ${result.token}")
                } else {
                    if (result.error?.contains("Password does not match") == true) {
                        _loginStatus.value = LoginStatus.PASSWORD_INVALID
                    } else if (result.error?.contains("Account not found") == true) {
                        _loginStatus.value = LoginStatus.USERNAME_INVALID
                    } else {
                        _loginStatus.value = LoginStatus.FAILED
                        _authDetails.value = AuthDetails(
                            "",
                            "",
                            false,
                            ""
                        )
                        Log.d(TAG, "login: error: ${result.error}")
                    }
                }

                _status.value = ApiStatus.DONE

            } catch (t: Throwable) {

                Log.e(TAG, "login: ", t)

                //TEST
//                _authDetails.value = AuthDetails(
//                    "sample_token",
//                    "lineleader",
//                    true
//                )

                _loginStatus.value = LoginStatus.ERROR
                _status.value = ApiStatus.ERROR
            }

        }

    }

    fun loadConfigFile(context: Context) {

        viewModelScope.launch {

            val folderName = "LTMechanicConfig"
            val fileName = "config.txt"
            var content = StringBuilder()

            try {

                val file =
                    File("${context.getExternalFilesDir(null)}${File.separator}$folderName${File.separator}$fileName")
                file.forEachLine { line ->

                    val split = line.split(":")
                    val key = split[0]

                    when {

                        line.contains("LANGUAGE") -> {
                            SettingsUtil.language = split[1]
                        }

                        line.contains("HOSTNAME") -> {
                            SettingsUtil.hostname = if (split.size > 2) {
                                "${split[1]}:${split[2]}"
                            } else {
                                split[1]
                            }
                        }

                        line.contains("HAS_NFC") -> {
                            SettingsUtil.hasNFC = split[1].toBoolean()
                        }

                        line.contains("HAS_BARCODE") -> {
                            SettingsUtil.hasBarcode = split[1].toBoolean()
                        }

                        line.contains("TRANSLATION_VERSION") -> {
                            SettingsUtil.translationVersion = split[1].trim().toInt()
                        }

                        line.contains("FACTORY") -> {
                            SettingsUtil.factory = split[1]
                        }

                    }


                }

                getTranslations(context, SettingsUtil.language, SettingsUtil.factory)

                Log.d(TAG, "onCreate: hostname: ${SettingsUtil.hostname}")

//                AppModule.host = "xurpas-web101.ltlabs.co"
//                AppModule.scheme = "http://18.163.4.11"

                AppModule.host = HttpUrl.parse(SettingsUtil.hostname)?.host().toString()
                AppModule.scheme = SettingsUtil.hostname


            } catch (e: Exception) {
                Log.e(TAG, "saveSettings: ", e)
            }

        }

    }

    fun getTranslations(context: Context, language: String, selectedFactory: String) {
        Log.d(TAG, "getTranslations: language: $language")
        Log.d(TAG, "getTranslations: selectedFactory: $selectedFactory")
        if (language != "en") {
            viewModelScope.launch {

                val params = mutableMapOf<String, String>()
                params["companyCode"] = COMPANY_CODE
                params["product"] = APP_NAME
                params["language"] = language
                params["version"] = "new"
                params["compact"] = "1"

                val getTranslationsDeferred = apiGlobal.getTranslationsAsync(params)

                try {

                    val result = getTranslationsDeferred.await()

                    val file =
                        File("${context.getExternalFilesDir(null)}${File.separator}language.json")

                    Log.d(TAG, "getTranslations: file.exists(): ${file.exists()}")

                    FileUtil.writeResponseBodyToDisk(result, context)

                    val startTime = System.currentTimeMillis()
                    Log.d(TAG, "getLanguageFile: startTime: $startTime")

                    file.forEachLine {
                        val version = JSONObject(it)["version"].toString().toInt()

                        Log.d(
                            TAG,
                            "getTranslations: version from app: ${SettingsUtil.translationVersion}"
                        )
                        Log.d(TAG, "getTranslations: version from api: $version")

                        if (SettingsUtil.translationVersion != version) {
                            LanguageUtil.translationVersion = version
                            SettingsUtil.translationVersion = version
                            SettingsUtil.language = language
                            val languageStr =
                                "{${
                                    it.replace("{", "")
                                        .replace("}", "")
                                        .replace("\"new\":true,\"version\":$version,\"data\":", "")
                                }}"
                            val newLanguageObject = JSONObject(languageStr)

                            newLanguageObject.keys().forEach { key ->
                                Log.d(TAG, "key: $key, value: ${newLanguageObject["$key"]}")
                                val keyVal = key.toString().replace("\"", "")
                                val value = newLanguageObject["$key"].toString().replace("\"", "")
                                    .replace("\\r", "")
                                languageJsonObject.put(keyVal.toLowerCase().trim(), value)
                                LanguageUtil.languageJsonObject.put(
                                    keyVal.toLowerCase().trim(),
                                    value
                                )
                            }

                            updateConfigFile(context)
                        }
                        Log.d(TAG, "getTranslations: languageJsonObject: $languageJsonObject")
                        Log.d(
                            TAG,
                            "getTranslations: languageJsonObject size: ${languageJsonObject.length()}"
                        )
                    }

                    _translation.value = true

                } catch (t: Throwable) {
                    Log.e(TAG, "getLanguages: ", t)
                    _translation.value = false
                    _languages.value = null
                }
            }
        } else {
            SettingsUtil.language = "en"
        }
        insertLanguageDatabase(
            arrayOf(
                DatabaseLanguage(
                    selectedLanguage = language
                )
            )
        )

        _translation.value = true
    }

    fun getTranslationsOld(context: Context, language: String, selectedFactory: String) {
        Log.d(TAG, "getTranslations: language: $language")
        Log.d(TAG, "getTranslations: selectedFactory: $selectedFactory")
        if (language != "en") {
            viewModelScope.launch {

                val getTranslationsDeferred =
                    fileApi.getTranslationsAsync(language, selectedFactory, "new")

                try {

                    val result = getTranslationsDeferred.await()

                    val file =
                        File("${context.getExternalFilesDir(null)}${File.separator}language.json")


                    Log.d(TAG, "getTranslations: file.exists(): ${file.exists()}")
//                    if (file.exists()) {
//                        file.delete()

                    val writtenToDisk = FileUtil.writeResponseBodyToDisk(result, context)

//                        Log.d(TAG, "getLanguageFile: writtenToDisk: $writtenToDisk")
//
//                        Log.d(TAG, "getLanguageFile: result file name: ${result}")
//                    }


                    val startTime = System.currentTimeMillis()
                    Log.d(TAG, "getLanguageFile: startTime: $startTime")



                    file.forEachLine {
                        val languages =
                            it.replace("\"data\":{", "").replace("{", "").replace("}", "")
                                .split(",")

//                        languages.forEach { language ->
//                            val splitStr = language.split(":")
//                            val key = splitStr[0].replace("\"", "")
//                            val value = splitStr[1].replace("\"", "").replace("\\r", "")
//                            languageJsonObject.put(key.toLowerCase().trim(), value)
//                        }

                        val version = JSONObject(it)["version"].toString().toInt()
//                        val version = 6

                        Log.d(
                            TAG,
                            "getTranslations: version from app: ${SettingsUtil.translationVersion}"
                        )
                        Log.d(TAG, "getTranslations: version from api: $version")

                        if (SettingsUtil.translationVersion != version) {
                            LanguageUtil.translationVersion = version
                            SettingsUtil.translationVersion = version
                            SettingsUtil.language = language
                            val languageStr = "{${
                                it.replace("{", "").replace("}", "")
                                    .replace("\"new\":true,\"version\":$version,\"data\":", "")
                            }}"
//                            val newLanguages = languageStr.split(":").toTypedArray()
//                            val upperHalf = newLanguages.copyOfRange(0, (newLanguages.size + 1) / 2)
//                            val lowerHalf = newLanguages.copyOfRange((newLanguages.size + 1) / 2, newLanguages.size)

//                        languages.forEach { language ->
//                            val splitStr = language.split(":")
//                            val key = splitStr[0].replace("\"", "")
//                            val value = splitStr[1].replace("\"", "").replace("\\r", "")
//                            languageJsonObject.put(key.toLowerCase(), value)
//                        }

                            val newLanguageObject = JSONObject(languageStr)

                            newLanguageObject.keys().forEach { key ->
                                Log.d(TAG, "key: $key, value: ${newLanguageObject["$key"]}")
                                val keyVal = key.toString().replace("\"", "")
                                val value = newLanguageObject["$key"].toString().replace("\"", "")
                                    .replace("\\r", "")
                                languageJsonObject.put(keyVal.toLowerCase().trim(), value)
                                LanguageUtil.languageJsonObject.put(
                                    keyVal.toLowerCase().trim(),
                                    value
                                )
                            }

                            updateConfigFile(context)

                        }

                        Log.d(TAG, "getTranslations: languageJsonObject: $languageJsonObject")
                        Log.d(
                            TAG,
                            "getTranslations: languageJsonObject size: ${languageJsonObject.length()}"
                        )
                    }

                    _translation.value = true

                } catch (t: Throwable) {
                    Log.e(TAG, "getLanguages: ", t)
                    _translation.value = false
                    _languages.value = null
                }
            }
//        } else {
//            val file = File("${context.getExternalFilesDir(null)}${File.separator}language.json")
//            if (file.exists()) {
//                file.delete()
//
//                file.createNewFile()
//            }
//
//            languageJsonObject = JSONObject()
//
//            Log.d(TAG, "getTranslations: languageJsonObject length: ${languageJsonObject.length()}")
//
//            _translation.value = true
        } else {
            SettingsUtil.language = "en"
        }
        insertLanguageDatabase(
            arrayOf(
                DatabaseLanguage(
                    selectedLanguage = language
                )
            )
        )

        _translation.value = true

    }

    private fun updateConfigFile(context: Context) {

        Log.d(TAG, "updateConfigFile: updating config file...")

        val folderName = "LTMechanicConfig"
        val fileName = "config.txt"
        var content = StringBuilder()

        try {

            val file =
                File("${context.getExternalFilesDir(null)}${File.separator}$folderName${File.separator}$fileName")
            file.forEachLine { line ->

                val split = line.split(":")
                val key = split[0]

                when {

                    line.contains("LANGUAGE") -> {

                        content.append("$key:${SettingsUtil.language}\n")
                    }

                    line.contains("HOSTNAME") -> {
                        content.append("$key:${SettingsUtil.hostname}\n")
                    }

                    line.contains("HAS_NFC") -> {
                        content.append("$key:${SettingsUtil.hasNFC}\n")
                    }

                    line.contains("HAS_BARCODE") -> {
                        content.append("$key:${SettingsUtil.hasBarcode}\n")
                    }

                    line.contains("TRANSLATION_VERSION") -> {
                        content.append("$key:${SettingsUtil.translationVersion}\n")
                    }

                    line.contains("FACTORY") -> {
                        content.append("$key:${SettingsUtil.factory}\n")
                    }

                }

                file.writeText("")
                file.writeText(content.toString())

            }
        } catch (e: Exception) {
            Log.e(TAG, "updateConfigFile: ", e)
        }
    }

    fun insertLanguageDatabase(language: Array<DatabaseLanguage>) {
        viewModelScope.launch {
            ltMechDatabaseRepository.insertLanguage(language)
        }
    }


    fun insertToAuthDetailsDatabase(authDetails: Array<DatabaseAuthDetails>) {
        viewModelScope.launch {
            ltMechDatabaseRepository.insertAuthDetails(authDetails)
        }
    }

    fun authDetailsComplete() {
        _authDetails.value = null
    }

    fun callStatusComplete() {
        _callStatus.value = null
    }
}