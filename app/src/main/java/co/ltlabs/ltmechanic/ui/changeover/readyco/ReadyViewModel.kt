package co.ltlabs.ltmechanic.ui.changeover.readyco

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ltlabs.ltmechanic.constant.type.COStatusType
import co.ltlabs.ltmechanic.domain.changeover.COItemResponse
import co.ltlabs.ltmechanic.network.main.MachineApi
import co.ltlabs.ltmechanic.network.main.dto.Machine
import co.ltlabs.ltmechanic.repository.co.CORepositoryImpl
import co.ltlabs.ltmechanic.ui.main.Resource
import co.ltlabs.ltmechanic.util.AuthUtil
import co.ltlabs.ltmechanic.util.SingleLiveEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

class ReadyViewModel @Inject constructor(
    private val machineApi: MachineApi,
    private val repo: CORepositoryImpl
) : ViewModel() {

    private val _status: MutableLiveData<COStatusType> = SingleLiveEvent()
    val status: LiveData<COStatusType> = _status

    private val _machine: MutableLiveData<Resource<List<Machine>>> = SingleLiveEvent()
    val machine: LiveData<Resource<List<Machine>>> = _machine

    fun updateStatus(status: COStatusType?) {
        status ?: return
        viewModelScope.launch {
            _status.postValue(status)
        }
    }

    fun updateCOItem(
        coItemId: Int,
        coRequestId: Int,
        machineId: Long?,
        note: String?
    ): Flow<Resource<COItemResponse>> {
        return flow {
            val co: COItemResponse? = null
            emit(Resource.loading(co))
            try {
                val result =
                    repo.updateCOItemAsync(
                        coItemId,
                        coRequestId,
                        COStatusType.READY_CODE,
                        machineId,
                        note
                    ).await()
                emit(Resource.success(result))
            } catch (e: HttpException) {
                if (e.code() == 413) {
                    e.response()?.errorBody()?.let {
                        val error = JSONObject(it.string())
                        emit(Resource.error(error.getString("message"), co))
                    }
                }
            }
        }
    }

    fun getMachineByMachineNo(machineNo: String) {
        viewModelScope.launch {
            _machine.postValue(Resource.loading(null))
            try {
                val result =
                    machineApi.getMachineByMachineNoAsync(machineNo, 1, "Bearer ${AuthUtil.token}")
                        .await()
                if (result.success) {
                    _machine.postValue(Resource.success(result.machine))
                }
            } catch (e: Exception) {
                _machine.postValue(Resource.error(e.localizedMessage, null))
            }
        }
    }

    fun getMachineByRfid(rfid: String) {
        viewModelScope.launch {
            _machine.postValue(Resource.loading(null))
            try {
                val result =
                    machineApi.getMachineByRfidAsync(rfid)
                        .await()
                if (result.success) {
                    _machine.postValue(Resource.success(result.machine))
                }
            } catch (e: Exception) {
                _machine.postValue(Resource.error(e.localizedMessage, null))
            }
        }
    }

}