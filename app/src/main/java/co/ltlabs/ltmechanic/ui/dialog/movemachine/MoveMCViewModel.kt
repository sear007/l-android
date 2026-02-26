package co.ltlabs.ltmechanic.ui.dialog.movemachine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ltlabs.ltmechanic.domain.Areas
import co.ltlabs.ltmechanic.domain.BuildingItem
import co.ltlabs.ltmechanic.repository.areas.AreasRepoImpl
import co.ltlabs.ltmechanic.repository.tickets.TicketsRepositoryImpl
import co.ltlabs.ltmechanic.ui.main.Resource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class MoveMCViewModel @Inject constructor(
    private val ticketsRepo: TicketsRepositoryImpl,
    private val areaRepo: AreasRepoImpl
) : ViewModel() {

    private val _building: MutableSharedFlow<Resource<List<BuildingItem>>> = MutableSharedFlow()
    val building: SharedFlow<Resource<List<BuildingItem>>> = _building

    private val _area: MutableSharedFlow<Resource<List<Areas>>> = MutableSharedFlow()
    val area: SharedFlow<Resource<List<Areas>>> = _area

    fun getBuilding() {
        viewModelScope.launch {
            _building.emit(Resource.loading(null))
            try {
                val result = ticketsRepo.getBuildingAsync().await()
                if (result.success == true) {
                    _building.emit(Resource.success(result.data))
                }
            } catch (e: Exception) {
                _building.emit(Resource.error(e.localizedMessage, null))
            }
        }
    }

    fun getAreaByBuilding(buildingId: Int) {
        viewModelScope.launch {
            _area.emit(Resource.loading(null))
            try {
                val result = areaRepo.getAreasNoLinesAsync(buildingId).await()
                if (result.success == true) {
                    _area.emit(Resource.success(result.areas))
                }
            } catch (e: Exception) {
                _area.emit(Resource.error(e.localizedMessage, null))
            }
        }
    }

}