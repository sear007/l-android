package co.ltlabs.ltmechanic.viewmodels.main.mechanic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.ltlabs.ltmechanic.domain.Problem
import javax.inject.Inject

class MechanicCreateTicketViewModel @Inject constructor() : ViewModel() {

    private val _eventLineListSearchResultNotFound = MutableLiveData<Boolean>()
    val eventLineListSearchResultNotFound: LiveData<Boolean>
        get() = _eventLineListSearchResultNotFound

    private val _selectedProblem = MutableLiveData<Problem>()
    val selectedProblem: LiveData<Problem>
        get() = _selectedProblem

    var popupFirstOpen = false

    fun setEventLineListSearchResultNotFoundToTrue() {
        _eventLineListSearchResultNotFound.value = true
    }

    fun setEventLineListSearchResultNotFoundToFalse() {
        _eventLineListSearchResultNotFound.value = false
    }

    fun setSelectedProblem(selectedProblem: Problem) {
        _selectedProblem.value = selectedProblem
    }
}