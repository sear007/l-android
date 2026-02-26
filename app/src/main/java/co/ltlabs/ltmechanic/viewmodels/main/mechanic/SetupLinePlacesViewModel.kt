package co.ltlabs.ltmechanic.viewmodels.main.mechanic

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.ltlabs.ltmechanic.domain.MachineInStation
import co.ltlabs.ltmechanic.network.main.MachineApi
import co.ltlabs.ltmechanic.network.main.dto.asMachineInStationDomainModel
import co.ltlabs.ltmechanic.util.ApiStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "SetupPlacesViewModel";

class SetupLinePlacesViewModel @Inject constructor( val machineApi: MachineApi) : ViewModel() {

    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _status = MutableLiveData<ApiStatus>()
    val status: LiveData<ApiStatus>
        get() = _status

    private val _machinesInStation = MutableLiveData<MutableList<MachineInStation>>()
    val machinesInStation: LiveData<MutableList<MachineInStation>>
        get() = _machinesInStation

    private val _selectedMachineStation = MutableLiveData<MachineInStation>()
    val selectedMachineStation: LiveData<MachineInStation>
        get() = _selectedMachineStation

    private val _navigateToScanMachine = MutableLiveData<MachineInStation>()
    val navigateToScanMachine: LiveData<MachineInStation>
        get() = _navigateToScanMachine

    init {
        Log.d(TAG, "init: viewmodel is working...")

    }

    fun setSelectedMachineStation(machineInStation: MachineInStation) {
        _selectedMachineStation.value = machineInStation
    }

    fun setSelectedMachineStationComplete() {
        _selectedMachineStation.value = null
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun setNavigateToScanMachine(machineInStation: MachineInStation) {
        _navigateToScanMachine.value = machineInStation
    }

    fun navigateToScanMachineComplete() {
        _navigateToScanMachine.value = null
    }

}