package co.ltlabs.ltmechanic.viewmodels.language

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.ltlabs.ltmechanic.constant.AppConfig
import co.ltlabs.ltmechanic.database.DatabaseAuthDetails
import co.ltlabs.ltmechanic.database.DatabaseLanguage
import co.ltlabs.ltmechanic.database.getDatabase
import co.ltlabs.ltmechanic.domain.Language
import co.ltlabs.ltmechanic.network.ApiGlobal
import co.ltlabs.ltmechanic.network.main.FileApi
import co.ltlabs.ltmechanic.network.main.dto.asLanguageDomainModel
import co.ltlabs.ltmechanic.repository.LtMechDatabaseRepository
import co.ltlabs.ltmechanic.util.FileUtil
import co.ltlabs.ltmechanic.util.LanguageUtil
import co.ltlabs.ltmechanic.util.SettingsUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.lang.Exception
import javax.inject.Inject

private const val TAG = "LanguageSetingViewModel";

class LanguageSettingViewModel @Inject constructor(
    private val apiGlobal: ApiGlobal,
    private val fileApi: FileApi,
    private var languageJsonObject: JSONObject,
    application: Application
) : AndroidViewModel(application) {
    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _languages = MutableLiveData<List<Language>>()
    val languages: LiveData<List<Language>>
        get() = _languages

    private val _translation = MutableLiveData<Boolean>()
    val translation: LiveData<Boolean>
        get() = _translation

    private val database = getDatabase(application)
    private val ltMechDatabaseRepository = LtMechDatabaseRepository(database)

    val languageFromDatabase = ltMechDatabaseRepository.language
    val mfgLinesFromDatabase = ltMechDatabaseRepository.mfgLines

    private val _transLationFileLoaded = MutableLiveData<Boolean>()
    val transLationFileLoaded: LiveData<Boolean>
        get() = _transLationFileLoaded

    fun getLanguages() {

        viewModelScope.launch {

            val getLanguageDeferred = fileApi.getLanguagesAsync()

            try {

                val result = getLanguageDeferred.await()

                if (result.success) {

                    _languages.value = result.language.asLanguageDomainModel()

                } else {
                    _languages.value = null
                }

            } catch (t: Throwable) {
                Log.e(TAG, "getLanguages: ", t)
                _languages.value = null
            }
        }
    }

    fun getTranslations(context: Context, language: String, selectedFactory: String) {
        Log.d(TAG, "getTranslations: language: $language")
        if (language != "en") {
            viewModelScope.launch {

                val params = mutableMapOf<String, String>()
                params["companyCode"] = AppConfig.COMPANY_CODE
                params["product"] = AppConfig.APP_NAME
                params["language"] = language
                params["version"] = "new"
                params["compact"] = "1"

                val getTranslationsDeferred = apiGlobal.getTranslationsAsync(params)

                try {

                    val result = getTranslationsDeferred.await()

                    val file = File("${context.getExternalFilesDir(null)}${File.separator}language.json")


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
                        val languages = it.replace("\"data\":{", "").replace("{", "").replace("}", "").split(",")

                        languages.forEach { language ->
                            val splitStr = language.split(":")
                            val key = splitStr[0].replace("\"", "")
                            val value = splitStr[1].replace("\"", "").replace("\\r", "")
                            languageJsonObject.put(key.toLowerCase().trim(), value)
                            LanguageUtil.languageJsonObject.put(key.toLowerCase().trim(), value)
                        }

                    }

                    Log.d(TAG, "getTranslations: languageJsonObject: $languageJsonObject")
                    Log.d(TAG, "getTranslations: languageJsonObject size: ${languageJsonObject.length()}")




                } catch (t: Throwable) {
                    Log.e(TAG, "getLanguages: ", t)
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

    fun getTranslationsOld(context: Context, language: String, selectedFactory: String) {
        Log.d(TAG, "getTranslations: language: $language")
        if (language != "en") {
            viewModelScope.launch {

                val getTranslationsDeferred = fileApi.getTranslationsAsync(language, selectedFactory, "new")

                try {

                    val result = getTranslationsDeferred.await()

                    val file = File("${context.getExternalFilesDir(null)}${File.separator}language.json")


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
                        val languages = it.replace("\"data\":{", "").replace("{", "").replace("}", "").split(",")

                        languages.forEach { language ->
                            val splitStr = language.split(":")
                            val key = splitStr[0].replace("\"", "")
                            val value = splitStr[1].replace("\"", "").replace("\\r", "")
                            languageJsonObject.put(key.toLowerCase().trim(), value)
                            LanguageUtil.languageJsonObject.put(key.toLowerCase().trim(), value)
                        }

                    }

                    Log.d(TAG, "getTranslations: languageJsonObject: $languageJsonObject")
                    Log.d(TAG, "getTranslations: languageJsonObject size: ${languageJsonObject.length()}")




                } catch (t: Throwable) {
                    Log.e(TAG, "getLanguages: ", t)
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

    fun loadTranslationFile(context: Context) {
        Log.d(TAG, "loadTranslationFile: ")
        try {

            File("${context.getExternalFilesDir(null)}${File.separator}language.json").forEachLine {
                val languages = it.replace("{", "").replace("}", "").split(",")

                languages.forEach { language ->
                    val splitStr = language.split(":")
                    val key = splitStr[0].replace("\"", "")
                    val value = splitStr[1].replace("\"", "").replace("\\r", "")
                    languageJsonObject.put(key.toLowerCase().trim(), value)
                    LanguageUtil.languageJsonObject.put(key.toLowerCase().trim(), value)
                }

//                languageJsonObject.put(key, value)


            }

            LanguageUtil.languageSelected.value = true

            _transLationFileLoaded.value = true


            Log.d(TAG, "getLanguageFile: LanguageUtil.languageJsonObject length:  ${languageJsonObject.length()}")

        } catch (e: Exception) {

            _transLationFileLoaded.value = false
            Log.e(TAG, "loadTranslationFile: ", e)
        }
    }

    fun insertLanguageDatabase(language: Array<DatabaseLanguage>) {
        viewModelScope.launch {
            ltMechDatabaseRepository.insertLanguage(language)
        }
    }

    fun languagesComplete() {
        _languages.value = null
    }

    fun insertToAuthDetailsDatabase(authDetails: Array<DatabaseAuthDetails>) {
        viewModelScope.launch {
            ltMechDatabaseRepository.insertAuthDetails(authDetails)
        }
    }
}