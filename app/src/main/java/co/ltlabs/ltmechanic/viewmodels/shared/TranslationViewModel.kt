package co.ltlabs.ltmechanic.viewmodels.shared

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.ltlabs.ltmechanic.constant.AppConfig
import co.ltlabs.ltmechanic.domain.Language
import co.ltlabs.ltmechanic.network.ApiGlobal
import co.ltlabs.ltmechanic.network.main.FileApi
import co.ltlabs.ltmechanic.network.main.dto.asLanguageDomainModel
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

private const val TAG = "TranslationViewModel"

class TranslationViewModel @Inject constructor(
    private val apiGlobal: ApiGlobal,
    private val fileApi: FileApi,
    private val languageJsonObject: JSONObject
) : ViewModel() {

    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _languages = MutableLiveData<List<Language>>()
    val languages: LiveData<List<Language>>
        get() = _languages

    private val _translation = MutableLiveData<Boolean>()
    val translation: LiveData<Boolean>
        get() = _translation

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

    fun getTranslations(context: Context, language: String, factory: String) {
        LanguageUtil.selectedFactory = factory
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

                    val writtenToDisk = FileUtil.writeResponseBodyToDisk(result, context)

                    val startTime = System.currentTimeMillis()

                    File("${context.getExternalFilesDir(null)}${File.separator}language.json").forEachLine {
                        val languages = it.replace("\"data\":{", "")
                            .replace("{", "")
                            .replace("}", "")
                            .split(",")

                        val version = JSONObject(it)["version"]
                        LanguageUtil.translationVersion = version.toString().toInt()
                        val languageStr = "{${it.replace("{", "").replace("}", "").replace("\"new\":true,\"version\":$version,\"data\":", "")}}"
                        val newLanguages = languageStr.split(":").toTypedArray()

                        val newLanguageObject = JSONObject(languageStr)

                        newLanguageObject.keys().forEach { key ->
                            Log.d(TAG, "key: $key, value: ${newLanguageObject["$key"]}")
                            val keyVal = key.toString().replace("\"", "")
                            val value = newLanguageObject["$key"].toString().replace("\"", "").replace("\\r", "")
                            languageJsonObject.put(keyVal.toLowerCase().trim(), value)
                            LanguageUtil.languageJsonObject.put(keyVal.toLowerCase().trim(), value)
                        }

                    }

                    _translation.value = true

                } catch (t: Throwable) {
                    Log.e(TAG, "getLanguages: ", t)
                    _languages.value = null
                }
            }
        } else {
            SettingsUtil.language = "en"
            val file = File("${context.getExternalFilesDir(null)}${File.separator}language.json")
            if(!file.exists()) {
                file.createNewFile()
            }
        }

        _translation.value = true
    }

    fun getTranslationsOld(context: Context, language: String, factory: String) {
        LanguageUtil.selectedFactory = factory
        if (language != "en") {
            viewModelScope.launch {

                val getTranslationsDeferred = fileApi.getTranslationsAsync(language, factory, "new")

                try {

                    val result = getTranslationsDeferred.await()

                    val writtenToDisk = FileUtil.writeResponseBodyToDisk(result, context)

                    val startTime = System.currentTimeMillis()

                    File("${context.getExternalFilesDir(null)}${File.separator}language.json").forEachLine {
                        val languages = it.replace("\"data\":{", "")
                            .replace("{", "")
                            .replace("}", "")
                            .split(",")

                        val version = JSONObject(it)["version"]
                        LanguageUtil.translationVersion = version.toString().toInt()
                        val languageStr = "{${it.replace("{", "").replace("}", "").replace("\"new\":true,\"version\":$version,\"data\":", "")}}"
                        val newLanguages = languageStr.split(":").toTypedArray()

                        val newLanguageObject = JSONObject(languageStr)

                        newLanguageObject.keys().forEach { key ->
                            Log.d(TAG, "key: $key, value: ${newLanguageObject["$key"]}")
                            val keyVal = key.toString().replace("\"", "")
                            val value = newLanguageObject["$key"].toString().replace("\"", "").replace("\\r", "")
                            languageJsonObject.put(keyVal.toLowerCase().trim(), value)
                            LanguageUtil.languageJsonObject.put(keyVal.toLowerCase().trim(), value)
                        }

                    }

                    _translation.value = true

                } catch (t: Throwable) {
                    Log.e(TAG, "getLanguages: ", t)
                    _languages.value = null
                }
            }
        } else {
            SettingsUtil.language = "en"
            val file = File("${context.getExternalFilesDir(null)}${File.separator}language.json")
            if(!file.exists()) {
                file.createNewFile()
            }
        }

        _translation.value = true
    }

    fun loadTranslationFile(context: Context) {
        try {

            File("${context.getExternalFilesDir(null)}${File.separator}language.json").forEachLine {
                val cleanStr = it.replace("{", "").replace("}", "")
                val splitStr = cleanStr.split(":")
                val key = splitStr[0].replace("\"", "")
                val value = splitStr[1].replace("\"", "")
//                    translations.add(DatabaseTranslation(translationKey = key, translationValue = value))

                languageJsonObject.put(key, value)
                LanguageUtil.languageJsonObject.put(key, value)
                _transLationFileLoaded.value = true
            }


            Log.d(TAG, "getLanguageFile: LanguageUtil.languageJsonObject length:  ${languageJsonObject.length()}")

        } catch (e: Exception) {

            _transLationFileLoaded.value = false
            Log.e(TAG, "loadTranslationFile: ", e)
        }
    }

    fun languagesComplete() {
        _languages.value = null
    }
}