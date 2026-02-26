package co.ltlabs.ltmechanic.ui.changeover.detailco

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ltlabs.ltmechanic.domain.changeover.CORequest
import co.ltlabs.ltmechanic.domain.changeover.OperatorItem
import co.ltlabs.ltmechanic.repository.co.CORepositoryImpl
import co.ltlabs.ltmechanic.ui.main.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

class CODetailViewModel @Inject constructor(
    private val repo: CORepositoryImpl
) : ViewModel() {

    private val _coDetail: MutableStateFlow<Resource<CORequest>> =
        MutableStateFlow(Resource.loading(null))
    val coDetail: StateFlow<Resource<CORequest>> = _coDetail

    fun updateCOStatus(coId: Int, statusCode: Int): Flow<Resource<Boolean>> {
        return flow {
            emit(Resource.loading(false))
            try {
                val result = repo.updateCOTicketAsync(coId, statusCode).await()
                val json = JSONObject(result.toString())
                emit(Resource.success(json.getBoolean("success")))
            } catch (e: Exception) {
                emit(Resource.error(e.localizedMessage, false))
            }
        }
    }

    fun getCODetail(coRequestNo: String, isRefresh: Boolean = false) {
        if (!isRefresh && _coDetail.value.data != null) return

        viewModelScope.launch {
            _coDetail.emit(Resource.loading(null))
            try {
                val result = repo.getCODetailAsync(coRequestNo).await()
                val data = result.coRequest
                modifiesList(data?.items)
                _coDetail.emit(Resource.success(data))

            } catch (e: Exception) {
                _coDetail.emit(Resource.error(e.localizedMessage, null))
            }

        }
    }

    private fun modifiesList(list: List<OperatorItem?>?) {
        list ?: return
        viewModelScope.launch {
            list.forEachIndexed { index, currentOperator ->
                if (index > 0) {
                    val preOperator = list[index - 1]
                    if (preOperator?.operations?.isNotEmpty() == true && preOperator.operations.isNotEmpty()) {
                        val lastPreOperations = preOperator.operations.size - 1
                        val curOperation = currentOperator?.operations?.get(0)
                        val preOperation = preOperator.operations[lastPreOperations]
                        curOperation?.isExist = curOperation?.id == preOperation?.id
                    }
                }
            }
        }
    }
}