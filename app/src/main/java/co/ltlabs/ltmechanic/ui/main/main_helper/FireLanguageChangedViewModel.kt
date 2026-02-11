package co.ltlabs.ltmechanic.ui.main.main_helper

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.ltlabs.ltmechanic.util.LanguageUtil
import javax.inject.Inject

class FireLanguageChangedViewModel @Inject constructor() : ViewModel() {

    private val _changeLanguage: MutableLiveData<String> = MutableLiveData()

    val changeLanguage: LiveData<String>
        get() = _changeLanguage

    fun setChangeLanguage(changedTo: String) {
        if (_changeLanguage.value != changedTo) {
            _changeLanguage.value = changedTo
        }
    }

}