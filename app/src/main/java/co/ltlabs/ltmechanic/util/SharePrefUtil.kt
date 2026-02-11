package co.ltlabs.ltmechanic.util

import android.content.Context
import android.content.SharedPreferences
import co.ltlabs.ltmechanic.constant.AppConfig.BASE_APPLICATION
import co.ltlabs.ltmechanic.constant.AppConfig.PREFS_NAME
import javax.inject.Inject
import javax.inject.Singleton

object SharePrefUtil {
    private val prefsName = PREFS_NAME
    private val sharedPref: SharedPreferences = BASE_APPLICATION.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    fun set(keyName: String, value: Any) {
        val editor: SharedPreferences.Editor = sharedPref.edit()

        if (value is Boolean) {
            editor.putBoolean(keyName, (value as Boolean?)!!)
        } else if (value is Int || value is Byte) {

            editor.putInt(keyName, (value as Int?)!!)
        } else if (value is Long) {

            editor.putLong(keyName, (value as Long?)!!)
        } else if (value is Float) {

            editor.putFloat(keyName, (value as Float?)!!)
        } else if (value is String) {

            editor.putString(keyName, value as String?)
        } else {

            editor.putString(keyName, value.toString())
        }

        editor.apply()
    }

    fun getString(keyName: String, defaultValue: String): String? {
        return sharedPref.getString(keyName, defaultValue)
    }

    fun getInt(keyName: String, defaultValue: Int): Int {
        return sharedPref.getInt(keyName, defaultValue)
    }

    fun getBoolean(keyName: String, defaultValue: Boolean): Boolean {
        return sharedPref.getBoolean(keyName, defaultValue)
    }

    fun getLong(keyName: String, defaultValue: Long): Long {
        return sharedPref.getLong(keyName, defaultValue)
    }

    fun getFloat(keyName: String, defaultValue: Float): Float {
        return sharedPref.getFloat(keyName, defaultValue)
    }

    fun clearSharedPreference() {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.clear()
        editor.apply()
    }

    fun removeValue(keyName: String) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.remove(keyName)
        editor.apply()
    }
}