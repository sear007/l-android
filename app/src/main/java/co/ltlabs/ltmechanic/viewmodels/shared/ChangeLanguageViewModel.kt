package co.ltlabs.ltmechanic.viewmodels.shared

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.ltlabs.ltmechanic.constant.AppConfig
import co.ltlabs.ltmechanic.constant.AppConfig.APP_NAME
import co.ltlabs.ltmechanic.constant.AppConfig.COMPANY_CODE
import co.ltlabs.ltmechanic.database.DatabaseAuthDetails
import co.ltlabs.ltmechanic.database.DatabaseLanguage
import co.ltlabs.ltmechanic.database.getDatabase
import co.ltlabs.ltmechanic.domain.Language
import co.ltlabs.ltmechanic.network.ApiGlobal
import co.ltlabs.ltmechanic.network.main.FileApi
import co.ltlabs.ltmechanic.network.main.dto.asLanguageDomainModel
import co.ltlabs.ltmechanic.repository.LtMechDatabaseRepository
import co.ltlabs.ltmechanic.util.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.*
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import java.lang.Exception
import javax.inject.Inject

private const val TAG = "ChangeLanguageViewModel";

class ChangeLanguageViewModel @Inject constructor(
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

    fun getLanguages(selectedFactory: String) {

        viewModelScope.launch {

            val params = mutableMapOf<String, String>()
            params["companyCode"] = COMPANY_CODE
            params["factoryId"] = selectedFactory
            params["app"] = APP_NAME

            val getLanguageDeferred = apiGlobal.getLanguagesAsync(params)

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

    fun getNewTranslate(context: Context, language: String, selectedFactory: String) {
        LanguageUtil.selectedFactory = selectedFactory
        SettingsUtil.language = language
        if (language != "en") {
            viewModelScope.launch {

                val getTranslationsDeferred =
                    fileApi.getTranslationsAsync(language, selectedFactory, "new")

                try {

                    val result = getTranslationsDeferred.await()

                    val file =
                        File("${context.getExternalFilesDir(null)}${File.separator}language.json")
                    //Socheat:
                    //Reason : Check if language is different version or different lang
                    val strResponse = result.string()
                    val jsonData = Gson().fromJson(strResponse, JsonObject::class.java)
                    val versionResponse = jsonData["version"].toString()

                    //case 1 : same language but different version
                    //case 2 : different language
                    val temp = if(versionResponse.isEmpty()) 0 else versionResponse.toIntOrNull()?:0
                    val prefLang = SharePrefUtil.getString(AppConfig.SP_SELECTED_LANG,"en")
                    if(language != prefLang || temp != LanguageUtil.translationVersion){
                        //do translate
                        var writtenToDisk = false
                        runBlocking {
                            writtenToDisk = FileUtil.writeToDisk(strResponse, context)
                        }
                        Timber.e(">>> Write to disk : $writtenToDisk")

                        val file = File("${context.getExternalFilesDir(null)}${File.separator}language.json")

                        //set to setting
                        LanguageUtil.translationVersion = temp
                        SettingsUtil.translationVersion = temp
                        SettingsUtil.factory = selectedFactory

                        file.forEachLine {
                            val regex = Regex("""\,(?=([^"]*"[^"]*")*[^"]*$)""")
                            val languages = it.replace("{","")
                                .replace("}","")
                                .replace("\"data\":","")
                                .split(regex)
                            for(language in languages){
                                try {
                                    val splitStr = language.split(":")
                                    val key = splitStr[0].replace("\"", "")
                                    val value = splitStr[1].replace("\"", "").replace("\\r", "")
                                    languageJsonObject.put(key.toLowerCase().trim(), value)
                                    LanguageUtil.languageJsonObject.put(key.toLowerCase().trim(), value)
                                }catch (e:java.lang.Exception){
                                    _languages.value = null
                                    Timber.e(e.localizedMessage)
                                }
                            }
                        }

                        _translation.value = true
                    }else{
                        //skip translate
                        _translation.value = null
                    }
                } catch (t: Throwable) {
                    Timber.e(t.localizedMessage)
                    _translation.value = null
                }
            }
        } else {
            SettingsUtil.language = "en"
            _translation.value = null
        }

        updateConfigFile(context)

        insertLanguageDatabase(
            arrayOf(
                DatabaseLanguage(
                    selectedLanguage = language
                )
            )
        )

    }

    fun getTranslations(context: Context, language: String, selectedFactory: String) {
        LanguageUtil.selectedFactory = selectedFactory
        SettingsUtil.language = language
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

                    //Socheat:
                    //Reason : Check if language is different version or different lang
                    val strResponse = result.string()
                    val jsonData = Gson().fromJson(strResponse, JsonObject::class.java)
                    val versionResponse = jsonData["version"].toString()

                    //case 1 : same language but different version
                    //case 2 : different language
                    val temp =
                        if (versionResponse.isEmpty()) 0 else versionResponse.toIntOrNull() ?: 0
                    val prefLang = SharePrefUtil.getString(AppConfig.SP_SELECTED_LANG, "en")
                    if (language != prefLang || temp != LanguageUtil.translationVersion) {
                        //do translatess
                        var writtenToDisk = false
                        runBlocking {
                            writtenToDisk = FileUtil.writeToDisk(strResponse, context)
                        }
                        Timber.e(">>> Write to disk : $writtenToDisk")

                        val file =
                            File("${context.getExternalFilesDir(null)}${File.separator}language.json")

                        //set to setting
                        LanguageUtil.translationVersion = temp
                        SettingsUtil.translationVersion = temp
                        SettingsUtil.factory = selectedFactory

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
                                    LanguageUtil.languageJsonObject.put(
                                        key.toLowerCase().trim(),
                                        value
                                    )
                                } catch (e: java.lang.Exception) {
                                    _languages.value = null
                                    Timber.e(e.localizedMessage)
                                }
                            }
                        }

                        _translation.value = true
                    } else {
                        //skip translate
                        _translation.value = null
                    }
                } catch (t: Throwable) {
                    Timber.e(t.localizedMessage)
                    _translation.value = null
                }
            }
        } else {
            SettingsUtil.language = "en"
            _translation.value = null
        }

        updateConfigFile(context)

        insertLanguageDatabase(
            arrayOf(
                DatabaseLanguage(
                    selectedLanguage = language
                )
            )
        )

    }

//Remove by Socheat
// Reason wrong check logic
//    fun getTranslations(context: Context, language: String, selectedFactory: String) {
//        LanguageUtil.selectedFactory = selectedFactory
//        SettingsUtil.language = language
//        Log.d(TAG, "getTranslations: language: $language")
//        if (language != "en") {
//            viewModelScope.launch {
//
//                val getTranslationsDeferred = fileApi.getTranslationsAsync(language, selectedFactory, "new")
//
//                try {
//
//                    val result = getTranslationsDeferred.await()
//
//                    val file = File("${context.getExternalFilesDir(null)}${File.separator}language.json")
//
//
//                    Log.d(TAG, "getTranslations: file.exists(): ${file.exists()}")
////                    if (file.exists()) {
////                        file.delete()
//
//                    val writtenToDisk = FileUtil.writeResponseBodyToDisk(result, context)
//
////                        Log.d(TAG, "getLanguageFile: writtenToDisk: $writtenToDisk")
////
////                        Log.d(TAG, "getLanguageFile: result file name: ${result}")
////                    }
//
//
//
//                    val startTime = System.currentTimeMillis()
//                    Log.d(TAG, "getLanguageFile: startTime: $startTime")
//
//
//
//                    file.forEachLine {
//                        val languages = it.replace("\"data\":{", "").replace("{", "").replace("}", "").split(",")
//
////                        languages.forEach { language ->
////                            val splitStr = language.split(":")
////                            val key = splitStr[0].replace("\"", "")
////                            val value = splitStr[1].replace("\"", "").replace("\\r", "")
////                            languageJsonObject.put(key.toLowerCase().trim(), value)
////                        }
//
//                        val version = JSONObject(it)["version"].toString().toInt()
//                        SettingsUtil.translationVersion = version.toString().toInt()
//
//                        if (LanguageUtil.translationVersion != version) {
//                            LanguageUtil.translationVersion = version
//                            SettingsUtil.translationVersion = version
//
//                            SettingsUtil.factory = selectedFactory
//
//                            val languageStr = "{${it.replace("{", "").replace("}", "").replace("\"new\":true,\"version\":$version,\"data\":", "")}}"
//                            val newLanguages = languageStr.split(":").toTypedArray()
//                            val upperHalf = newLanguages.copyOfRange(0, (newLanguages.size + 1) / 2)
//                            val lowerHalf = newLanguages.copyOfRange((newLanguages.size + 1) / 2, newLanguages.size)
//
//                            val newLanguageObject = JSONObject(languageStr)
//
//                            newLanguageObject.keys().forEach { key ->
//                                Log.d(TAG, "key: $key, value: ${newLanguageObject["$key"]}")
//                                val keyVal = key.toString().replace("\"", "")
//                                val value = newLanguageObject["$key"].toString().replace("\"", "").replace("\\r", "")
//                                languageJsonObject.put(keyVal.toLowerCase().trim(), value)
//                                LanguageUtil.languageJsonObject.put(keyVal.toLowerCase().trim(), value)
//                            }
//
//
//
//                        }
//
//                    }
//
//                    //Log.d(TAG, "getTranslations: languageJsonObject: $languageJsonObject")
//                    //Log.d(TAG, "getTranslations: languageJsonObject size: ${languageJsonObject.length()}")
//
//
//
//
//                } catch (t: Throwable) {
//                    Log.e(TAG, "getLanguages: ", t)
//                    _languages.value = null
//                }
//            }
////        } else {
////            val file = File("${context.getExternalFilesDir(null)}${File.separator}language.json")
////            if (file.exists()) {
////                file.delete()
////
////                file.createNewFile()
////            }
////
////            languageJsonObject = JSONObject()
////
////            Log.d(TAG, "getTranslations: languageJsonObject length: ${languageJsonObject.length()}")
////
////            _translation.value = true
//        } else {
//            SettingsUtil.language = "en"
//        }
//
//        updateConfigFile(context)
//
//        insertLanguageDatabase(
//            arrayOf(
//                DatabaseLanguage(
//                    selectedLanguage = language
//                )
//            )
//        )
//
//        _translation.value = true
//
//    }
 // END of remove by socheat

    fun loadTranslationFile(context: Context) {
        Log.d(TAG, "loadTranslationFile: ")
        try {

            File("${context.getExternalFilesDir(null)}${File.separator}language.json").forEachLine {
                val languages = it.replace("{", "").replace("}", "").split(",")

                val version = JSONObject(it)["version"]
                SettingsUtil.translationVersion = version.toString().toInt()
                SettingsUtil.factory = LanguageUtil.selectedFactory
                val languageStr = "{${it.replace("{", "").replace("}", "").replace("\"new\":true,\"version\":$version,\"data\":", "")}}"
                val newLanguages = languageStr.split(":").toTypedArray()
                val upperHalf = newLanguages.copyOfRange(0, (newLanguages.size + 1) / 2)
                val lowerHalf = newLanguages.copyOfRange((newLanguages.size + 1) / 2, newLanguages.size)

                Log.d(TAG, "half: upperHalf: ${upperHalf.toMutableList()}")
                Log.d(TAG, "half: lowerHalf: ${lowerHalf.toMutableList()}")
                Log.d(TAG, "languageStr: $languageStr")
                Log.d(TAG, "JSONObject(it): ${JSONObject(languageStr)["Request send sucessfully"]}")



//                Log.d(TAG, "loadTranslationFile: length of it: ${it}")
//                Log.d(TAG, "loadTranslationFile: length of: ${it.replace("{", "").replace("}", "").replace("\"new\":true,\"version\":1,\"data\":", "").split(":")}")
//                Log.d(TAG, "loadTranslationFile: length of: ${it.replace("{", "").replace("}", "").replace("\"new\":true,\"version\":1,\"data\":", "").split(":").size}")
//
//                Log.d(TAG, "languages: $languages")
//
//                Log.d(TAG, "jsonobject: ${JSONObject(it)["version"]}")

//                languages.forEach { language ->
//                    val splitStr = language.split(":")
//                    val key = splitStr[0].replace("\"", "")
//                    val value = splitStr[1].replace("\"", "").replace("\\r", "")
//                    Log.d(TAG, "loadTranslationFile: key: $key")
//                    languageJsonObject.put(key.toLowerCase().trim(), value)
//                }

//                upperHalf.forEachIndexed { index, language ->
//                    val splitStr = language.split(":")
//                    val key = upperHalf[index].replace("\"", "")
//                    val value = lowerHalf[index].replace("\"", "").replace("\\r", "")
//                    Log.d(TAG, "loadTranslationFile: key: $key")
//                    languageJsonObject.put(key.toLowerCase().trim(), value)
//                }
//
//                for (i in 0 until newLanguages.size - 1 step 2 ) {
//                    Log.d(TAG, "index: $i")
//                    val key = newLanguages[i]
//                    val value = newLanguages[i + 1]
//                    Log.d(TAG, "index key: $key")
//                    Log.d(TAG, "index value: $value")
//                }

                val newLanguageObject = JSONObject(languageStr)

                newLanguageObject.keys().forEach { key ->
                    Log.d(TAG, "key: $key, value: ${newLanguageObject["$key"]}")
                    val keyVal = key.toString().replace("\"", "")
                    val value =
                        newLanguageObject["$key"].toString().replace("\"", "").replace("\\r", "")
                    languageJsonObject.put(keyVal.toLowerCase().trim(), value)
                    LanguageUtil.languageJsonObject.put(keyVal.toLowerCase().trim(), value)
                }
                updateConfigFile(context)

//                languageJsonObject.put(key, value)


            }

            LanguageUtil.languageSelected.value = true

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

    fun updateConfigFile(context: Context) {
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

    fun languagesComplete() {
        _languages.value = null
    }

    fun insertToAuthDetailsDatabase(authDetails: Array<DatabaseAuthDetails>) {
        viewModelScope.launch {
            ltMechDatabaseRepository.insertAuthDetails(authDetails)
        }
    }

}