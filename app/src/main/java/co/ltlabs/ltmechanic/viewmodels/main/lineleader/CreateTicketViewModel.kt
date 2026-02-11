package co.ltlabs.ltmechanic.viewmodels.main.lineleader

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.ltlabs.ltmechanic.database.getDatabase
import co.ltlabs.ltmechanic.domain.Problem
import co.ltlabs.ltmechanic.repository.LtMechDatabaseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject

class CreateTicketViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val database = getDatabase(application)
    private val mfgLinesDatabaseRepository = LtMechDatabaseRepository(database)

    val mfgLinesFromDatabase = mfgLinesDatabaseRepository.mfgLines

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