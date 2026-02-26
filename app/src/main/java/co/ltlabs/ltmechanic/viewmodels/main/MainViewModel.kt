package co.ltlabs.ltmechanic.viewmodels.main

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import co.ltlabs.ltmechanic.constant.AppConfig
import co.ltlabs.ltmechanic.database.*
import co.ltlabs.ltmechanic.domain.Translation
import co.ltlabs.ltmechanic.domain.request.LogoutRequest
import co.ltlabs.ltmechanic.network.ApiGlobal
import co.ltlabs.ltmechanic.network.AppStore
import co.ltlabs.ltmechanic.network.AppStoreInfo
import co.ltlabs.ltmechanic.network.auth.Reference2Api
import co.ltlabs.ltmechanic.network.main.FileApi
import co.ltlabs.ltmechanic.repository.LtMechDatabaseRepository
import co.ltlabs.ltmechanic.ui.main.Resource
import co.ltlabs.ltmechanic.util.AuthUtil
import co.ltlabs.ltmechanic.util.FileUtil
import co.ltlabs.ltmechanic.util.LanguageUtil
import co.ltlabs.ltmechanic.util.SharePrefUtil
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.json.JSONObject
import retrofit2.await
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Named

private const val TAG = "MainViewModel"

class MainViewModel @Inject constructor(
    application: Application,
    private val fileApi: FileApi,
    private val apiGlobal: ApiGlobal,
    private val languageJsonObject: JSONObject,
    @Named("for_main")
    private val referenceApi: Reference2Api
) : AndroidViewModel(application) {

    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val database = getDatabase(application)
    private val ltMechDatabaseRepository = LtMechDatabaseRepository(database)

    val languageFromDatabase = ltMechDatabaseRepository.language

    private val _hideToolbar = MutableLiveData<Boolean>()
    val hideToolbar: LiveData<Boolean>
        get() = _hideToolbar

    private val _transLationFileLoaded = MutableLiveData<Boolean>()
    val transLationFileLoaded: LiveData<Boolean>
        get() = _transLationFileLoaded

    val firebaseNotificationFromDatabase = ltMechDatabaseRepository.firebaseNotifications
    val mfgLinesFromDatabase = ltMechDatabaseRepository.mfgLines
    val mfgAreaFromDatabase = ltMechDatabaseRepository.mfgAreas
    val translationsFromDatabase = ltMechDatabaseRepository.translations
    val nfcFromDatabase = ltMechDatabaseRepository.nfc
    val nfcDeviceFromDatabase = ltMechDatabaseRepository.nfcDevice
    val notificationsFromDatebase = ltMechDatabaseRepository.notifications(AuthUtil.username)

    fun insertToFirebaseNotificationDatabase(token: String) {
        viewModelScope.launch {
            Log.d(TAG, "insertToFirebaseNotificationDatabase: token: $token")
            ltMechDatabaseRepository.insertFirebaseNotifications(
                arrayOf(
                    DatabaseFirebaseNotification(1, token)
                )
            )
        }
    }

    fun insertToNotificationDatabase(notifications: Array<DatabaseNotification>) {
        viewModelScope.launch {
            ltMechDatabaseRepository.insertNotifications(notifications, AuthUtil.username)
        }

    }

    fun deleteFromNotificationDatabase(notification: DatabaseNotification) {
        viewModelScope.launch {
            ltMechDatabaseRepository.deleteNotification(notification)
        }

    }

    fun insertToNfcDatabase(rfid: String, new: Boolean) {
        viewModelScope.launch {
            ltMechDatabaseRepository.insertNfc(DatabaseNFC(rfid = rfid, newRfid = new))
        }
    }

    fun insertToNfcDeviceDatabase(enabled: Boolean) {
        viewModelScope.launch {
            ltMechDatabaseRepository.insertNfcDevice(DatabaseNFCDevice(enabled = enabled))
        }
    }

    fun getTranslationByKey(key: String): LiveData<Translation> =
        ltMechDatabaseRepository.translationByKey(key)

    fun getLanguageFile(context: Context) {


        viewModelScope.launch {


            try {
                val getLanguageFileDeferred = fileApi.getLanguageFileAsync()
                val result = getLanguageFileDeferred.await()

                val writtenToDisk = FileUtil.writeResponseBodyToDisk(result, context)

                Log.d(TAG, "getLanguageFile: writtenToDisk: $writtenToDisk")

                Log.d(TAG, "getLanguageFile: result file name: ${result}")

                val startTime = System.currentTimeMillis()
                Log.d(TAG, "getLanguageFile: startTime: $startTime")

                File("${context.getExternalFilesDir(null)}${File.separator}language.json").forEachLine {
                    val cleanStr = it.replace("{", "").replace("}", "")
                    val splitStr = cleanStr.split(":")
                    val key = splitStr[0].replace("\"", "")
                    val value = splitStr[1].replace("\"", "")

                    languageJsonObject.put(key, value)
                    LanguageUtil.languageJsonObject.put(key, value)

                }

                val endTime = System.currentTimeMillis()
                Log.d(TAG, "getLanguageFile: endTime: $endTime")
                val elapsedTime = System.currentTimeMillis() - startTime

                Log.d(TAG, "getLanguageFile: elapsed time: ${elapsedTime} ms")

                Log.d(
                    TAG,
                    "getLanguageFile: LanguageUtil.languageJsonObject length:  ${languageJsonObject.length()}"
                )

//                ltMechDatabaseRepository.insertTranslations(translations.toTypedArray())
//
//                Log.d(TAG, "getLanguageFile: result: ${result.string()}")
//                Log.d(TAG, "getLanguageFile: result length: ${result.contentLength()}")
//
//                val jsonTest = JSONObject(StringBuilder(result.string()).toString())
//                Log.d(TAG, "getLanguageFile: jsonTest: ${jsonTest.length()}")
//                val json = Gson().fromJson("{\"-A\":\"this is just a test translation and should not be valid\"}", JSONObject::class.java)
////                Log.d(TAG, "getLanguageFile: json: ${json["-A"]}")
            } catch (t: Throwable) {
                Log.e(TAG, "getLanguageFile: ", t)
            }
        }

    }

    fun loadTranslationFile(context: Context) {
        try {
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
                        Timber.e(e.localizedMessage)
                        _transLationFileLoaded.value = false
                    }
                }
            }

            LanguageUtil.languageSelected.value = true
            _transLationFileLoaded.value = true

        } catch (e: Exception) {
            Timber.e(e.localizedMessage)
            _transLationFileLoaded.value = false
        }
    }

    fun setHideToolbar(hide: Boolean) {
        _hideToolbar.value = hide
    }

    fun insertToAuthDetailsDatabase(authDetails: Array<DatabaseAuthDetails>) {
        viewModelScope.launch {
            ltMechDatabaseRepository.insertAuthDetails(authDetails)
        }
    }

    private val _appStoreStatus = MutableLiveData<AppStoreInfo>()
    val appStoreStatus: LiveData<AppStoreInfo>
        get() = _appStoreStatus

    private val _logout: MutableSharedFlow<Resource<Any>> = MutableSharedFlow()
    val logout: SharedFlow<Resource<Any>> = _logout

    fun getAppInfo() {
        viewModelScope.launch {
            try {
                val getAppInfoDeferred = referenceApi.getAppInfoAsync(AppStore(AppConfig.APP_NAME))
                val response = getAppInfoDeferred.await()
                if (response.isNotEmpty()) {
                    _appStoreStatus.value = response[0]
                } else {
                    _appStoreStatus.value = null
                }

            } catch (t: Throwable) {
                _appStoreStatus.value = null
                Timber.e("getAppInfo: ${t.localizedMessage}")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _logout.emit(Resource.loading(null))
            try {
                val deviceToken = SharePrefUtil.getString(AppConfig.DEVICE_TOKEN, "")
                val token = AuthUtil.token
                val result = apiGlobal.logout(LogoutRequest(deviceToken, token)).await()
                withContext(Dispatchers.IO) {
                    database.clearAllTables()
                }
                _logout.emit(Resource.success(result))

            } catch (t: Throwable) {
                _logout.emit(Resource.error(t.localizedMessage, null))
            }
        }
    }

}