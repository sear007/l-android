package co.ltlabs.ltmechanic.network

import co.ltlabs.ltmechanic.domain.maint.MaintItem
import co.ltlabs.ltmechanic.domain.maint.MaintResponse
import co.ltlabs.ltmechanic.repository.paging.BasePaging
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiMaint {

    @GET("maintenances")
    suspend fun getMaints(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
        @Query("type") type: String,
        @Query("machine") machine: String? = null,
        @Query("lineSelected") lineSelected: String? = null,
        @Query("areaSelected") areaSelected: String? = null
    ): Response<MaintResponse>

}