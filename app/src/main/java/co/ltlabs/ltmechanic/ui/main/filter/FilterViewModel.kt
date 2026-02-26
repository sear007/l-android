package co.ltlabs.ltmechanic.ui.main.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ltlabs.ltmechanic.domain.Areas
import co.ltlabs.ltmechanic.domain.MfgLine
import co.ltlabs.ltmechanic.network.main.LineApi
import co.ltlabs.ltmechanic.network.main.dto.asDomainModel
import co.ltlabs.ltmechanic.repository.areas.AreasRepoImpl
import co.ltlabs.ltmechanic.ui.main.Resource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class FilterViewModel @Inject constructor(
    private val lineApi: LineApi,
    private val repo: AreasRepoImpl
) : ViewModel() {

    private val _lines: MutableSharedFlow<Resource<List<MfgLine>>> = MutableSharedFlow()
    val lines: SharedFlow<Resource<List<MfgLine>>> = _lines

    private val _areasNoLines: MutableSharedFlow<Resource<List<Areas>>> = MutableSharedFlow()
    val areasNoLines: SharedFlow<Resource<List<Areas>>> = _areasNoLines

    fun getLinesInAreas() {
        viewModelScope.launch {
            _lines.emit(Resource.loading(null))
            try {
                val result = lineApi.getUserLinesByAssignedAndSelectedAsync().await()
                if (result.success) {
                    val list = result.lines.asDomainModel().toMutableList()
                    _lines.emit(Resource.success(list))
                }
            } catch (e: Exception) {
            }
        }
    }

    fun getAreasNoLines() {
        viewModelScope.launch {
            _areasNoLines.emit(Resource.loading(null))
            try {
                val result = repo.getAreasNoLinesAsync().await()
                if (result.success == true) {
                    _areasNoLines.emit(Resource.success(result.areas))
                }
            } catch (e: Exception) {

            }
        }
    }

}