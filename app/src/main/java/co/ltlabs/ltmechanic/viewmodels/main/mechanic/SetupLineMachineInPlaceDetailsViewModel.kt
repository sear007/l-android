package co.ltlabs.ltmechanic.viewmodels.main.mechanic

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.ltlabs.ltmechanic.domain.Machine
import co.ltlabs.ltmechanic.domain.MachineInStation
import co.ltlabs.ltmechanic.network.MachineCheckInRequest
import co.ltlabs.ltmechanic.network.main.MachineApi
import co.ltlabs.ltmechanic.network.main.dto.asDomainModel
import co.ltlabs.ltmechanic.network.main.dto.asMachineInStationDomainModel
import co.ltlabs.ltmechanic.util.ApiStatus
import co.ltlabs.ltmechanic.util.MachineCheckinStatus
import co.ltlabs.ltmechanic.util.MachineStatus
import co.ltlabs.ltmechanic.util.SetupLineConfirmEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MachineDetailsViewModel";

class SetupLineMachineInPlaceDetailsViewModel @Inject constructor(val machineApi: MachineApi) : ViewModel() {

    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    init {
        Log.d(TAG, "init: viewmodel is working...")
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

}