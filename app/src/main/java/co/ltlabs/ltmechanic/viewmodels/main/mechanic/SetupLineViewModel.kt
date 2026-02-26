package co.ltlabs.ltmechanic.viewmodels.main.mechanic

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import co.ltlabs.ltmechanic.database.getDatabase
import co.ltlabs.ltmechanic.network.main.MachineApi
import co.ltlabs.ltmechanic.repository.LtMechDatabaseRepository
import co.ltlabs.ltmechanic.util.Line
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject

private const val TAG = "SetupLineViewModel";

class SetupLineViewModel @Inject constructor(application: Application, private val machineApi: MachineApi) : AndroidViewModel(application) {

    /**
     * This is the job for all coroutines started by this ViewModel.
     *
     * Cancelling this job will cancel all coroutines started by this ViewModel.
     */
    private val viewModelJob = SupervisorJob()

    /**
     * This is the main scope for all coroutines launched by MainViewModel.
     *
     * Since we pass viewModelJob, you can cancel all coroutines launched by uiScope by calling
     * viewModelJob.cancel()
     */
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)


    private val _eventLinesListChanged = MutableLiveData<Boolean>()
    val eventLinesChanged: LiveData<Boolean>
        get() = _eventLinesListChanged

    private val _eventLineListSearchResultNotFound = MutableLiveData<Boolean>()
    val eventLineListSearchResultNotFound: LiveData<Boolean>
        get() = _eventLineListSearchResultNotFound

    private val database = getDatabase(application)
    private val mfgLinesDatabaseRepository = LtMechDatabaseRepository(database)

    val mfgLinesFromDatabase = mfgLinesDatabaseRepository.mfgLines

    val lineList = listOf(
        Line("YTI-01", position = 0),
        Line("YTI-02", true, position = 1),
        Line("YTI-03", position = 2),
        Line("YTI-04", position = 3),
        Line("YTI-05", position = 4),
        Line("YTI-06", position = 5),
        Line("YTI-07", position = 6),
        Line("YTI-08", position = 7),
        Line("YTI-09", position = 8),
        Line("YTI-10", position = 9)
    )

    var selectedLine: String = "YTI-02"
    var popupFirstOpen = false

    init {
        Log.d(TAG, "init: viewmodel is working...")
    }

    fun setEventLineListSearchResultNotFoundToTrue() {
        Log.d(TAG, "setEventLineListSearchResultNotFoundToTrue: no results line list size ${lineList.size}")
        _eventLineListSearchResultNotFound.value = true
    }

    fun setEventLineListSearchResultNotFoundToFalse() {
        Log.d(TAG, "setEventLineListSearchResultNotFoundToFalse: with results")
        _eventLineListSearchResultNotFound.value = false
    }

    override fun onCleared() {
        super.onCleared()

        viewModelJob.cancel()
    }
}