package co.ltlabs.ltmechanic.viewmodels.shared

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.ltlabs.ltmechanic.util.popup.SpinnerItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject

private const val TAG = "SpinnerViewModel";

open class SpinnerViewModel @Inject constructor(): ViewModel() {

    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    // General
    private val _eventSearchResultNotFound = MutableLiveData<Boolean>()
    val eventSearchResultNotFound: LiveData<Boolean> get() = _eventSearchResultNotFound

    private val _itemData = MutableLiveData<List<SpinnerItem>>()
    val itemData: LiveData<List<SpinnerItem>> get() = _itemData

    private val _selectItemData = MutableLiveData<SpinnerItem>()
    val selectedItemData: LiveData<SpinnerItem> get() = _selectItemData

    var submitData = MutableLiveData<SpinnerItem>()

    var popupFirstOpen = false
    var items = listOf<SpinnerItem>()
        set(value) {
            field = value
            _itemData.postValue(value)
        }

    var itemsChecked = listOf<SpinnerItem>()
        get() {
            val copyItems: List<SpinnerItem> = items
            copyItems.filter { it.id == selectedItem.id }.map { it.checked = true}
            return copyItems
        }

    var selectedItem = SpinnerItem("")
        set(value) {
            field = value
            _selectItemData.postValue(value)
        }

    fun setEventSearchResultNotFound(isNotFound: Boolean = true) {
        _eventSearchResultNotFound.postValue(isNotFound)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}