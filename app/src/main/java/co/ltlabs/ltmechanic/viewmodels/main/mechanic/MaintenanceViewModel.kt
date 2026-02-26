package co.ltlabs.ltmechanic.viewmodels.main.mechanic

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import co.ltlabs.ltmechanic.domain.ChecklistRequest
import co.ltlabs.ltmechanic.domain.maintenance.ChecklistItem
import co.ltlabs.ltmechanic.repository.maintenance.MaintRepositoryImpl
import co.ltlabs.ltmechanic.ui.main.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class MaintenanceViewModel @Inject constructor(
    private val repo: MaintRepositoryImpl,
    application: Application
) : AndroidViewModel(application) {

    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _data: MutableSharedFlow<Resource<List<ChecklistItem>>> = MutableSharedFlow()
    val data: SharedFlow<Resource<List<ChecklistItem>>> = _data

    private val _addChecklist: MutableSharedFlow<Resource<Boolean>> = MutableSharedFlow()
    val addCheckList: SharedFlow<Resource<Boolean>> = _addChecklist

    fun attachChecklistToMachine(ticketId: Long, checklistId: Long) {
        viewModelScope.launch {
            _addChecklist.emit(Resource.loading(null))
            try {
                val result = repo.attachMachineWithChecklistAsync(
                    ticketId,
                    ChecklistRequest(checklistId)
                ).await()
                _addChecklist.emit(Resource.success(result.success))
            } catch (e: Exception) {
                _addChecklist.emit(Resource.error(e.localizedMessage, null))
            }
        }
    }

    fun getChecklist() {
        viewModelScope.launch {
            _data.emit(Resource.loading(null))
            try {
                val result = repo.getChecklistAsync().await()
                _data.emit(Resource.success(result.machines?.result))
            } catch (e: Exception) {
                _data.emit(Resource.error(e.localizedMessage, null))
            }
        }
    }



}