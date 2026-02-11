package co.ltlabs.ltmechanic.viewmodels.shared

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.ltlabs.ltmechanic.domain.NonLineArea
import co.ltlabs.ltmechanic.domain.RequestType
import co.ltlabs.ltmechanic.domain.SolutionType
import co.ltlabs.ltmechanic.domain.StatusId
import co.ltlabs.ltmechanic.network.main.ReferenceApi
import co.ltlabs.ltmechanic.network.main.dto.asNonLineAreaDomainModel
import co.ltlabs.ltmechanic.network.main.dto.asRequestTypeDomainMode
import co.ltlabs.ltmechanic.network.main.dto.asSolutionTypeDomainModel
import co.ltlabs.ltmechanic.network.main.dto.asStatusIdDomainModel
import co.ltlabs.ltmechanic.util.AuthUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ReferenceViewModel";

class ReferenceViewModel @Inject constructor(private val referenceApi: ReferenceApi) : ViewModel() {

    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _solutionTypes = MutableLiveData<List<SolutionType>>()
    val solutionTypes: LiveData<List<SolutionType>>
        get() = _solutionTypes

    private val _nonLineAreas = MutableLiveData<List<NonLineArea>>()
    val nonLineAreas: LiveData<List<NonLineArea>>
        get() = _nonLineAreas

    private val _requestTypes = MutableLiveData<List<RequestType>>()
    val requestTypes: LiveData<List<RequestType>>
        get() = _requestTypes

    private val _statusId = MutableLiveData<StatusId>()
    val statusId: LiveData<StatusId>
        get() = _statusId

    fun getSolutionTypes() {

        viewModelScope.launch {

            val getSolutionTypesDeferred = referenceApi.getSolutionTypesAsync(accessToken = "Bearer ${AuthUtil.token}")

            try {

                val result = getSolutionTypesDeferred.await()

                if (result.success) {

                    _solutionTypes.value = result.solutions.result.asSolutionTypeDomainModel()

                } else {
                    _solutionTypes.value = null
                }

            } catch (t: Throwable) {


                Log.e(TAG, "getSolutionTypes: ", t)

            }

        }

    }

    fun getStatusByDesc(status: String, module: String) {

        viewModelScope.launch {

            val getStatusByDescDeferred = referenceApi.getStatusByDescAsync(status, module, accessToken = "Bearer ${AuthUtil.token}")

            try {

                val result = getStatusByDescDeferred.await()

                if (result.success) {

                    val statusIdFetched = result.asStatusIdDomainModel()
                    statusIdFetched.type = status
                    statusIdFetched.module = module

                    _statusId.value = statusIdFetched

                } else {
                    _statusId.value = null
                }

            } catch (t: Throwable) {


                Log.e(TAG, "getStatusByDesc: ", t)

            }

        }

    }

    fun getSolutionTypesByProblemId(problemTypeId: Long) {

        Log.d(TAG, "getSolutionTypesByProblemId: problemTypeId: $problemTypeId")

        viewModelScope.launch {

            val getSolutionTypesDeferred = referenceApi.getSolutionTypesByProblemIdAsync(problemTypeId, accessToken = "Bearer ${AuthUtil.token}")

            try {

                val result = getSolutionTypesDeferred.await()

                if (result.success) {

                    _solutionTypes.value = result.solutions.asSolutionTypeDomainModel()

                } else {
                    _solutionTypes.value = null
                }

            } catch (t: Throwable) {


                Log.e(TAG, "getSolutionTypes: ", t)

            }

        }

    }

    fun getNoneLineAreas() {
        viewModelScope.launch {

            val getNonLineAreasDeferred = referenceApi.getNonLineAreasAsync("Bearer ${AuthUtil.token}")

            try {
                val result = getNonLineAreasDeferred.await()

                if (result.success) {
                    _nonLineAreas.value = result.machine.asNonLineAreaDomainModel()
                } else {
                    _nonLineAreas.value = null
                }
            } catch (t: Throwable) {
                Log.e(TAG, "getNoneLineAreas: ", t)
                _nonLineAreas.value = null
            }
        }
    }

    fun getProductConfig() {
        viewModelScope.launch {

            val getProductConfigDeferred = referenceApi.getProductConfigAsync(
                accessToken = "Bearer ${AuthUtil.token}"
            )

            try {

                val result = getProductConfigDeferred.await()

                if (result.success) {
                    _requestTypes.value = result.data.asRequestTypeDomainMode()
                } else {
                    _requestTypes.value = null
                }

            } catch (t: Throwable) {
                Log.e(TAG, "getProductConfig: ", t)

                _requestTypes.value = null
            }

        }
    }


    fun nonLineAreasComplete() {
        _nonLineAreas.value = null
    }

    fun solutionTypecomplete() {
        _solutionTypes.value = null
    }

    fun requestTypeComplete() {
        _requestTypes.value = null
    }

    fun statusIdComplete() {
        _statusId.value = null
    }

    override fun onCleared() {
        super.onCleared()

        viewModelJob.cancel()
    }
}