package co.ltlabs.ltmechanic.repository.co

import androidx.paging.PagingData
import co.ltlabs.ltmechanic.domain.changeover.COItem
import co.ltlabs.ltmechanic.domain.changeover.COItemResponse
import co.ltlabs.ltmechanic.domain.changeover.COResponse
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow

interface CORepository {

    fun getCOList(status: String, lineSelected: String): Flow<PagingData<COItem>>
    fun getCODetailAsync(coRequestNo: String): Deferred<COResponse>
    fun updateCOTicketAsync(coId: Int, statusCode: Int): Deferred<Any>

    fun updateCOItemAsync(
        operationId: Int,
        coRequestId: Int,
        statusCode: Int,
        machineId: Long?,
        note: String?
    ): Deferred<COItemResponse>

}