package co.ltlabs.ltmechanic.viewmodels.main.mechanic

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ltlabs.ltmechanic.domain.Machine
import co.ltlabs.ltmechanic.network.main.MachineApi
import co.ltlabs.ltmechanic.network.main.dto.asDomainModel
import co.ltlabs.ltmechanic.ui.main.Resource
import co.ltlabs.ltmechanic.util.AuthUtil
import co.ltlabs.ltmechanic.util.SingleLiveEvent
import kotlinx.coroutines.launch
import javax.inject.Inject

class MechanicInRepairTicketsPreviewViewModel @Inject constructor(
    private val machineApi: MachineApi
) : ViewModel() {

    private val _machineResponse: SingleLiveEvent<Resource<Machine>> = SingleLiveEvent()
    val machine: LiveData<Resource<Machine>> = _machineResponse

    fun getMachineByMachineNo(machineNo: String, userId: Int = 1) {
        viewModelScope.launch {
            _machineResponse.postValue(Resource.loading(null))
            try {
                val result = machineApi.getMachineByMachineNoAsync(
                    machineNo,
                    userId,
                    "Bearer ${AuthUtil.token}"
                ).await()
                if (result.asDomainModel().isNotEmpty()) {
                    val machine = result.asDomainModel()[0]
                    _machineResponse.postValue(Resource.success(machine))
                } else {
                    _machineResponse.postValue(Resource.error("Machine not found", null))
                }
            } catch (e: Exception) {
                _machineResponse.postValue(Resource.error(e.localizedMessage, null))
            }
        }
    }

    fun getMachineByRfid(rfid: String) {
        viewModelScope.launch {
            _machineResponse.postValue(Resource.loading(null))
            try {
                val result =
                    machineApi.getMachineByRfidAsync(rfid).await()
                if (result.asDomainModel().isNotEmpty()) {
                    val machine = result.asDomainModel()[0]
                    _machineResponse.postValue(Resource.success(machine))
                } else {
                    _machineResponse.postValue(Resource.error("Machine not found", null))
                }
            } catch (e: Exception) {
                _machineResponse.postValue(Resource.error(e.localizedMessage, null))
            }
        }
    }

}