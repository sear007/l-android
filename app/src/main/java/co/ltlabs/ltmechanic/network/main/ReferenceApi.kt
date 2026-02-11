package co.ltlabs.ltmechanic.network.main

import co.ltlabs.ltmechanic.domain.maintenance.ChecklistResponse
import co.ltlabs.ltmechanic.network.*
import co.ltlabs.ltmechanic.util.REFERENCE_API_ADDED_URL
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface ReferenceApi {

    @GET("$REFERENCE_API_ADDED_URL/api/ref/macsubtypes/{id}")
    fun getMacSubTypeByIdAsync(
        @Path("id") id: Int, @Header("Authorization") accessToken: String
    ): Deferred<MacSubTypeResponse>

    @GET("$REFERENCE_API_ADDED_URL/api/ref/solutiontypes")
    fun getSolutionTypesAsync(
        @Query("isActive") isActive: Int = 1, @Header("Authorization") accessToken: String
    ): Deferred<SolutionTypeResponse>

    @GET("$REFERENCE_API_ADDED_URL/api/ref/solutiontypes/by-problem")
    fun getSolutionTypesByProblemIdAsync(
        @Query("problemTypeId") problemTypeId: Long, @Header("Authorization") accessToken: String
    ): Deferred<SolutionTypeByProblemIdResponse>

    @GET("$REFERENCE_API_ADDED_URL/api/ref/areas/non-line")
    fun getNonLineAreasAsync(@Header("Authorization") accessToken: String): Deferred<NonLineAreasResponse>

    @GET("$REFERENCE_API_ADDED_URL/api/ref/productconfig/requests")
    fun getProductConfigAsync(
        @Query("isActive") isActive: String = "1", @Header("Authorization") accessToken: String
    ): Deferred<ProductConfigResponse>

    @GET("$REFERENCE_API_ADDED_URL/api/ref/productconfig")
    suspend fun getProductConfig(): Response<ProductConfigResponse>

    @GET("$REFERENCE_API_ADDED_URL/api/ref/factories/assigned")
    fun getAssignedFactoriesAsync(@Header("Authorization") accessToken: String): Deferred<FactoryResponse>

    @GET("$REFERENCE_API_ADDED_URL/api/ref/status/by-desc")
    fun getStatusByDescAsync(
        @Query("status") status: String,
        @Query("module") module: String,
        @Header("Authorization") accessToken: String
    ): Deferred<StatusResponse>

    @GET("$REFERENCE_API_ADDED_URL/api/ref/checklists")
    fun getChecklistAsync(
        @Query("isActive") action: Int = 1,
        @Query("brandId") brandId: String = "all",
        @Query("macSubTypeId") macSubTypeId: String = "all",
        @Query("searchString") searchString: String = "all",
        @Query("currentPage") currentPage: Int = 1,
        @Query("pageSize") pageSize: Int = 10000
    ): Deferred<ChecklistResponse>

}