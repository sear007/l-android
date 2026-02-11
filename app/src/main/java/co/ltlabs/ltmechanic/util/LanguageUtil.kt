package co.ltlabs.ltmechanic.util

import androidx.lifecycle.MutableLiveData
import co.ltlabs.ltmechanic.constant.AppConfig
import org.json.JSONObject
import timber.log.Timber
import java.lang.Exception
import java.util.*

private const val TAG = "LanguageUtil"

fun JSONObject.getTranslation(phrase: String): String =
    try {
        val selectedLang = SharePrefUtil.getString(AppConfig.SP_SELECTED_LANG, "en")
        LanguageUtil.selectedLanguage = selectedLang.toString()
        if (LanguageUtil.selectedLanguage != LanguageUtil.ENGLISH) {
            val keyPhrase = phrase.lowercase(Locale.getDefault()).trim().replace("\n", "\\n")
            this[keyPhrase].toString()
        } else {
            LanguageUtil.translated = true
            phrase
        }
    } catch (e: Exception) {
        LanguageUtil.translated = false
        Timber.e(e.localizedMessage)
        phrase
    }


class LanguageUtil {


    companion object {

        const val ENGLISH = "en"
        const val KHMER = "km"
        const val CHINESE = "zh-CN"

        val languageSelected: MutableLiveData<Boolean> = MutableLiveData()

        var languageJsonObject = JSONObject()

        var selectedLanguage = ""

        var translated = false

        var translationVersion = 0
        var selectedFactory = ""

        // Login Screen
        var loginScreenLabelWelcomeTo = "Welcome to"
        var loginScreenLabelLTMechanic = "LT Mechanic"
        var loginScreenLabelUsername = "username"
        var loginScreenLabelPassword = "password"
        var loginScreenButtonLogin = "LOGIN"

        // Line Leader Home
        var lineLeaderHomeButtonCreateTicket = "CREATE TICKET"
        var lineLeaderHomeButtonReportedTickets = "REPORTED TICKET"
        var lineLeaderHomeButtonInRepairTickets = "IN-REPAIR TICKETS"
        var lineLeaderHomeButtonRepairedTickets = "REPAIRED TICKETS"
        var lineLeaderHomeButtonMachines = "MACHINES"
        var lineLeaderHomeButtonQueryMachine = "QUERY MACHINE"
        var lineLeaderHomeButtonSendRequest = "SEND REQUEST"


    }
}