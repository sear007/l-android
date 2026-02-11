package co.ltlabs.ltmechanic.util

import androidx.lifecycle.MutableLiveData
import org.json.JSONObject

class MainUtil {
    companion object {
        var count = 0;
        var googlePlayAvailableInt = 0
        var googlePlayAvailable = false

        val fragmentResumed = MutableLiveData<Boolean>()
        val updatedLanguageJsonObject = MutableLiveData<JSONObject>()

        fun fragmentResumedComplete() {
            fragmentResumed.value = false
        }
    }
}