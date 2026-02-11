package co.ltlabs.ltmechanic.viewmodels.main.mechanic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.ltlabs.ltmechanic.domain.MachineAvailable
import co.ltlabs.ltmechanic.domain.MachineLocation
import javax.inject.Inject

class MechanicReportedTicketsAlternativeMachineLocationsViewModel @Inject constructor() : ViewModel() {

    private val _navigateToAvailableMachines = MutableLiveData<MachineLocation>()
    val navigateToAvailableMachines: LiveData<MachineLocation>
        get() = _navigateToAvailableMachines

    fun setNavigateToAvailableMachines(machineLocation: MachineLocation) {
        _navigateToAvailableMachines.value = machineLocation
    }

    fun navigateToAvailableMachinesComplete() {
        _navigateToAvailableMachines.value = null
    }
}