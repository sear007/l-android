package co.ltlabs.ltmechanic.ui.changeover.prepareco

import androidx.lifecycle.ViewModel
import co.ltlabs.ltmechanic.constant.type.COStatusType
import co.ltlabs.ltmechanic.domain.changeover.COItemResponse
import co.ltlabs.ltmechanic.domain.changeover.CORequest
import co.ltlabs.ltmechanic.domain.changeover.COResponse
import co.ltlabs.ltmechanic.repository.co.CORepositoryImpl
import co.ltlabs.ltmechanic.ui.main.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import javax.inject.Inject

class PrepareCOViewModel @Inject constructor(
    private val repo: CORepositoryImpl
) : ViewModel() {

    fun updateCOItem(
        coItemId: Int,
        coRequestId: Int
    ): Flow<Resource<COItemResponse>> {
        return flow {
            val co: COItemResponse? = null
            emit(Resource.loading(co))
            try {
                val result =
                    repo.updateCOItemAsync(
                        coItemId,
                        coRequestId,
                        COStatusType.IN_PROGRESS_CODE,
                        null,
                        null
                    ).await()
                emit(Resource.success(result))
            } catch (e: Exception) {
                emit(Resource.error(e.localizedMessage, co))
            }
        }
    }

}