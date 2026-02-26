package co.ltlabs.ltmechanic.network

import co.ltlabs.ltmechanic.domain.changeover.COItem
import co.ltlabs.ltmechanic.domain.changeover.COItemResponse
import co.ltlabs.ltmechanic.domain.changeover.COResponse
import co.ltlabs.ltmechanic.repository.paging.BasePaging
import kotlinx.coroutines.Deferred
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiCO {

    @GET("co-requests/by-line")
    suspend fun getCOList(
        @Query("lineSelected") lineSelected: String? = "",
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
        @Query("status") status: String
    ): Response<BasePaging<COItem>>

    @GET("co-requests/no/{coRequestNo}")
    fun getCODetailAsync(
        @Path("coRequestNo") coRequestNo: String,
        @Query("group") group: Int = 1,
    ): Deferred<COResponse>

    @PATCH("co-requests/{coId}")
    fun updateCOTicketAsync(
        @Path("coId") coId: Int,
        @Body body: RequestBody,
    ): Deferred<Any>

    @PATCH("co-requests/items/{itemId}")
    fun updateCOItemAsync(
        @Path("itemId") operationId: Int,
        @Body body: RequestBody,
    ): Deferred<COItemResponse>

}