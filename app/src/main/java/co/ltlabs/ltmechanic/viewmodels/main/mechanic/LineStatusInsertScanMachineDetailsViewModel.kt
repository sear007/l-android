package co.ltlabs.ltmechanic.viewmodels.main.mechanic

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.ltlabs.ltmechanic.network.MachineInsertRequest
import co.ltlabs.ltmechanic.network.main.LineApi
import co.ltlabs.ltmechanic.util.ApiStatus
import co.ltlabs.ltmechanic.util.AuthUtil
import co.ltlabs.ltmechanic.util.MachineInsertStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import javax.inject.Inject

private const val TAG = "InsertViewModel";

class LineStatusInsertScanMachineDetailsViewModel @Inject constructor(
    private val lineApi: LineApi
) : ViewModel() {

    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _status = MutableLiveData<ApiStatus>()
    val status: LiveData<ApiStatus>
        get() = _status

    private val _machineInsertStatus = MutableLiveData<MachineInsertStatus>()
    val machineInsertStatus: LiveData<MachineInsertStatus>
        get() = _machineInsertStatus

    fun insertBetweenMachines(id: Long? = null, station: String, mfgLineId: Long) {

        val machineInsertRequest = MachineInsertRequest(id, station, mfgLineId, DateTime(
            DateTimeZone.UTC).toString())

        viewModelScope.launch {

            val insertMachineBetweenDeferred = lineApi.insertMachineBetweenAsync(machineInsertRequest, "Bearer ${AuthUtil.token}")

            try {
                _status.value = ApiStatus.LOADING
                val result = insertMachineBetweenDeferred.await()

                _status.value = ApiStatus.DONE
                if (result.success) {
                    _machineInsertStatus.value = MachineInsertStatus.SUCCESS
                } else {
                    _machineInsertStatus.value = MachineInsertStatus.FAILED
                }
            } catch (t: Throwable) {

                Log.e(TAG, "insertBetweenMachines: ", t)

                _machineInsertStatus.value = MachineInsertStatus.FAILED
                _status.value = ApiStatus.ERROR
            }
        }
    }

    fun insertBetweenMachinesComplete() {
        _machineInsertStatus.value = null
    }

    override fun onCleared() {
        super.onCleared()

        viewModelJob.cancel()
    }
}