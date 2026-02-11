package co.ltlabs.ltmechanic.viewmodels.main.mechanic

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.ltlabs.ltmechanic.database.getDatabase
import co.ltlabs.ltmechanic.domain.MachineInStation
import co.ltlabs.ltmechanic.network.main.MachineApi
import co.ltlabs.ltmechanic.network.main.dto.asMachineInStationDomainModel
import co.ltlabs.ltmechanic.repository.LtMechDatabaseRepository
import co.ltlabs.ltmechanic.util.ApiStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "StationsViewModel"

class LineStatusStationsViewModel @Inject constructor(val machineApi: MachineApi) : ViewModel() {

    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _status = MutableLiveData<ApiStatus>()
    val status: LiveData<ApiStatus>
        get() = _status

    private val _navigateToStationDetails = MutableLiveData<MachineInStation>()
    val navigateToStationDetails: LiveData<MachineInStation>
        get() = _navigateToStationDetails

    var endLineStation = false

    var stationsTemp = mutableListOf<MachineInStation>()

    fun setNavigateToStationDetails(machineInStation: MachineInStation) {
        _navigateToStationDetails.value = machineInStation
    }

    fun setNavigateToStationDetailsComplete() {
        _navigateToStationDetails.value = null
    }

    fun setEndStationToTrue() {
        endLineStation = true
    }

}