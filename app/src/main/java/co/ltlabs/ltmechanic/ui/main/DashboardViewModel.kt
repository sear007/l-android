package co.ltlabs.ltmechanic.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ltlabs.ltmechanic.domain.Areas
import co.ltlabs.ltmechanic.domain.MfgLine
import co.ltlabs.ltmechanic.network.DashboardStatisticsRequest
import co.ltlabs.ltmechanic.network.MachineTicketResult
import co.ltlabs.ltmechanic.network.SaveAreasNoLines
import co.ltlabs.ltmechanic.network.main.dto.asDomainModel
import co.ltlabs.ltmechanic.repository.areas.AreasRepoImpl
import co.ltlabs.ltmechanic.repository.lines.LinesRepositoryImpl
import co.ltlabs.ltmechanic.repository.tickets.TicketsRepositoryImpl
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class DashboardViewModel @Inject constructor(
    private val areasRepo: AreasRepoImpl,
    private val linesRepo: LinesRepositoryImpl,
    private val ticketsRepo: TicketsRepositoryImpl
) : ViewModel() {

    private var statisticJob: Job? = null

    val selectedAreaNoLines = mutableListOf<Areas>()

    private val selectedLinesName = mutableListOf<String>()
    private val selectedAreasName = mutableListOf<String>()

    private val _selectedMfgAreas = MutableLiveData<List<Areas>>()
    val selectedMfgAreas: LiveData<List<Areas>>
        get() = _selectedMfgAreas

    // For filter on dashboard
    private val _filter: MutableSharedFlow<String> = MutableSharedFlow()
    val filter: SharedFlow<String> = _filter

    private val _areasNoLines: MutableSharedFlow<List<Areas>?> = MutableSharedFlow()
    val areasNoLines: SharedFlow<List<Areas>?> = _areasNoLines

    private val _assignedLines: MutableSharedFlow<List<MfgLine>> = MutableSharedFlow()
    val assignedLines: SharedFlow<List<MfgLine>> = _assignedLines

    private val _statisticCount: MutableSharedFlow<Resource<MachineTicketResult?>> =
        MutableSharedFlow()
    val statisticCount: SharedFlow<Resource<MachineTicketResult?>> = _statisticCount

    private val _sharedAreasNoLines: MutableList<Areas> = arrayListOf()
    var sharedAreasNoLines = _sharedAreasNoLines
    var isFilterAll = false

    fun getDashboardStatistic() {
        statisticJob?.cancel()
        statisticJob = null
        statisticJob = viewModelScope.launch {
            _statisticCount.emit(Resource.loading(null))
            try {
                coroutineScope {
                    // Get Assigned Lines
                    val resultLines = linesRepo.getAssignedLinesByAreasAsync().await()
                    val filterLiens = resultLines.lines.filter {
                        it.isSelected == true
                    }
                    _assignedLines.emit(filterLiens.asDomainModel())
                    selectedLinesName.clear()
                    filterLiens.map {
                        selectedLinesName.add(it.mfgLine ?: "")
                    }

                    // Get Areas without Lines
                    selectedAreasName.clear()
                    if (selectedAreaNoLines.isNotEmpty()) {
                        selectedAreaNoLines.map {
                            if (it.isSelected) selectedAreasName.add(it.area ?: "")
                        }
                        selectedAreasName.clear()
                        setSharedAreasNoLine(selectedAreaNoLines)
                    }
                    //Get area
                    val resultAreas = areasRepo.getAreasNoLinesAsync().await()
                    _areasNoLines.emit(resultAreas.areas)
                    resultAreas.areas?.map {
                        if (it.isSelected) selectedAreasName.add(it.area ?: "")
                    }

                    //Auto check line & area is select all item or not
                    val isLineAllChecked = resultLines.lines.filter { it.isSelected == false }
                    val isAreaAllChecked = resultAreas.areas?.filter { !it.isSelected }
                    isAreaAllChecked?.let {
                        isFilterAll =
                            isLineAllChecked.isEmpty() && isAreaAllChecked.isEmpty()
                    }
                    setSharedAreasNoLine(resultAreas.areas ?: emptyList())

                    // Get statistics count
                    val result = ticketsRepo.getTicketStatisticsAsync(
                        DashboardStatisticsRequest(selectedLinesName, selectedAreasName)
                    ).await()
                    if (result.success) {
                        _statisticCount.emit(Resource.success(result.tickets))
                        setFilter()
                    }
                }
            } catch (e: Exception) {
                _statisticCount.emit(Resource.error(e.localizedMessage.toString(), null))
            }
        }
    }

    fun refreshDashboardStatistic(selectedLines: List<MfgLine>) {
        viewModelScope.launch {
            _statisticCount.emit(Resource.loading(null))
            try {
                setSharedAreasNoLine(selectedAreaNoLines)
                selectedAreasName.clear()
                selectedAreaNoLines.map {
                    selectedAreasName.add(it.area ?: "")
                }

                selectedLinesName.clear()
                selectedLines.map {
                    selectedLinesName.add(it.mfgLine)
                }

                // Get statistics count
                val result = ticketsRepo.getTicketStatisticsAsync(
                    DashboardStatisticsRequest(selectedLinesName, selectedAreasName)
                ).await()
                if (result.success) {
                    _statisticCount.emit(Resource.success(result.tickets))
                    setFilter()
                }
            } catch (e: Exception) {
                _statisticCount.emit(Resource.error(e.localizedMessage.toString(), null))
            }
        }
    }

    fun saveAreasNoLines(list: List<Areas>?) {
        list ?: return
        viewModelScope.launch {
            try {
                val l = arrayListOf<Int>()
                list.map {
                    l.add(it.id ?: 0)
                }
                areasRepo.saveAreasNoLinesAsync(SaveAreasNoLines(l)).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setFilter() {
        viewModelScope.launch {
            var filter = ""
            if (selectedLinesName.isNotEmpty() || selectedAreasName.isNotEmpty()) {
                selectedLinesName.map {
                    filter = "$filter $it"
                }
                selectedAreasName.map {
                    filter = "$filter $it"
                }
            } else {
                filter = "No filter"
            }


            _filter.emit(filter)
        }
    }

    fun setSelectedMfgArea(mfgAreas: List<Areas>) {
        _selectedMfgAreas.value = mfgAreas
    }

    private fun setSharedAreasNoLine(areas: List<Areas>) {
        _sharedAreasNoLines.clear()
        _sharedAreasNoLines.addAll(areas.filter {
            it.isSelected
        })
    }

}