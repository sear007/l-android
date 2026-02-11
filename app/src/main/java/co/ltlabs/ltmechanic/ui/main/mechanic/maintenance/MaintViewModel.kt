package co.ltlabs.ltmechanic.ui.main.mechanic.maintenance

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import co.ltlabs.ltmechanic.constant.type.MaintType
import co.ltlabs.ltmechanic.database.getDatabase
import co.ltlabs.ltmechanic.domain.Machine
import co.ltlabs.ltmechanic.domain.maint.MaintItem
import co.ltlabs.ltmechanic.network.main.MachineApi
import co.ltlabs.ltmechanic.network.main.dto.asDomainModel
import co.ltlabs.ltmechanic.repository.maintenance.MaintRepositoryImpl
import co.ltlabs.ltmechanic.ui.main.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class MaintViewModel @Inject constructor(
    application: Application,
    private val repo: MaintRepositoryImpl,
    private val machineApi: MachineApi
) : AndroidViewModel(application) {

    val database = getDatabase(application)

    private val _machine: MutableSharedFlow<Resource<Machine>> = MutableSharedFlow()
    val machine: SharedFlow<Resource<Machine>> = _machine

    init {
        viewModelScope.launch {
            repo.database.coRequestDao.deleteAll()
        }
    }

    fun getMachineByRfid(rfid: String) {
        viewModelScope.launch {
            _machine.emit(Resource.loading(null))
            try {
                val result =
                    machineApi.getMachineByRfidAsync(rfid).await()
                if (result.asDomainModel().isNotEmpty()) {
                    val machine = result.asDomainModel()[0]
                    _machine.emit(Resource.success(machine))
                } else {
                    _machine.emit(Resource.error("Machine not found", null))
                }
            } catch (e: Exception) {
                _machine.emit(Resource.error(e.localizedMessage, null))
            }
        }
    }

    fun getMaints(
        type: String,
        machine: String? = null,
        lineSelected: String? = null,
        areaSelected: String? = null
    ): Flow<PagingData<MaintItem>> {
        return repo.getMaints(type.lowercase(), machine, lineSelected, areaSelected)
    }

    fun getPagingSizeOverdueStatus() = repo.database.maintDao.getMeta(MaintType.OVERDUE.lowercase())
    fun getPagingSizeScheduleStatus() =
        repo.database.maintDao.getMeta(MaintType.SCHEDULED.lowercase())

    fun getPagingSizeCloseStatus() = repo.database.maintDao.getMeta(MaintType.CLOSED.lowercase())

}