package co.ltlabs.ltmechanic.repository.co

import android.app.Application
import androidx.paging.Pager
import androidx.paging.PagingConfig
import co.ltlabs.ltmechanic.database.getDatabase
import co.ltlabs.ltmechanic.domain.changeover.COItemResponse
import co.ltlabs.ltmechanic.domain.changeover.CORequest
import co.ltlabs.ltmechanic.domain.changeover.COResponse
import co.ltlabs.ltmechanic.network.ApiCO
import kotlinx.coroutines.Deferred
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import javax.inject.Inject

class CORepositoryImpl @Inject constructor(
    private val api: ApiCO,
    application: Application
) : CORepository {

    val database = getDatabase(application)

    override fun getCOList(status: String, lineSelected: String) = Pager(
        config = PagingConfig(
            pageSize = 20,
            enablePlaceholders = false
        ),
        pagingSourceFactory = {
            COPagingDataSource(
                api,
                status,
                lineSelected,
                database.coRequestDao
            )
        }
    ).flow

    override fun getCODetailAsync(coRequestNo: String): Deferred<COResponse> {
        return api.getCODetailAsync(coRequestNo)
    }

    override fun updateCOTicketAsync(coId: Int, statusCode: Int): Deferred<Any> {
        val json = JSONObject()
        json.put("status", statusCode)
        val body =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return api.updateCOTicketAsync(coId, body)
    }

    override fun updateCOItemAsync(
        operationId: Int,
        coRequestId: Int,
        statusCode: Int,
        machineId: Long?,
        note: String?
    ): Deferred<COItemResponse> {
        val json = JSONObject()
        json.put("coRequestId", coRequestId)
        json.put("status", statusCode)
        if (machineId != null)
            json.put("machineId", machineId)
        if (note != null && note.isNotEmpty())
            json.put("note", note)
        val body =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        return api.updateCOItemAsync(operationId, body)
    }

}