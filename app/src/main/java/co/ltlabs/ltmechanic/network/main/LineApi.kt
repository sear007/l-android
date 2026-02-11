package co.ltlabs.ltmechanic.network.main

import co.ltlabs.ltmechanic.domain.AreaResponse
import co.ltlabs.ltmechanic.network.*
import co.ltlabs.ltmechanic.util.LINE_API_ADDED_URL
import kotlinx.coroutines.Deferred
import retrofit2.http.*

interface LineApi {

    @GET("$LINE_API_ADDED_URL/api/lines/assigned")
    fun getAssignedLinesByAreasAsync():
            Deferred<LinesResponse>

    @GET("$LINE_API_ADDED_URL/api/lines/by-assigned-areas")
    fun getUserLinesByAssignedAreasAsync(@Header("Authorization") accessToken: String):
            Deferred<LinesResponse>

    @GET("$LINE_API_ADDED_URL/api/lines/assigned-and-selected")
    fun getUserLinesByAssignedAndSelectedAsync():
            Deferred<LinesResponse>

    @GET("$LINE_API_ADDED_URL/api/lines/unassigned")
    fun getUserUnAssignedLinesByAreaAsync(@Header("Authorization") accessToken: String):
            Deferred<LinesResponse>

    @POST("$LINE_API_ADDED_URL/api/lines/save-selected-lines")
    fun assignLinesAsync(@Body lineAssignRequest: LineAssignRequest):
            Deferred<LinesAssignResponse>

    @PUT("$LINE_API_ADDED_URL/api/lines/clear")
    fun clearLineAsync(@Body clearLineRequest: ClearLineRequest, @Header("Authorization") accessToken: String):
            Deferred<ClearLineResponse>

    @PUT("$LINE_API_ADDED_URL/api/lines/clear/validate")
    fun clearLineValidateAsync(@Body clearLineRequest: ClearLineRequest, @Header("Authorization") accessToken: String):
            Deferred<ClearLineValidateResponse>

    @PUT("$LINE_API_ADDED_URL/api/lines/checkin")
    fun checkInMachineAsync(@Body machineCheckInRequest: MachineCheckInRequest, @Query("replace") replace: String = "n",
                            @Header("Authorization") accessToken: String):
            Deferred<MachineCheckInResponse>

    @PUT("$LINE_API_ADDED_URL/api/lines/new-machine-checkin")
    fun checkInNewMachineAsync(@Body machineCheckInRequest: MachineCheckInRequest, @Query("replace") replace: String = "n",
                            @Header("Authorization") accessToken: String):
            Deferred<MachineCheckInResponse>

    @PUT("$LINE_API_ADDED_URL/api/lines/checkout")
    fun checkOutMachineAsync(@Body machineLineCheckOutRequest: MachineLineCheckOutRequest,
                             @Header("Authorization") accessToken: String):
            Deferred<MachineCheckOutResponse>

    @PUT("$LINE_API_ADDED_URL/api/lines/checkin-between")
    fun insertMachineBetweenAsync(@Body machineInsertRequest: MachineInsertRequest,
                                  @Header("Authorization") accessToken: String):
            Deferred<MachineCheckOutResponse>

    @GET("$LINE_API_ADDED_URL/api/areas/and-total-alt-machines")
    fun getStorageAreasBySubTypeAsync(@Query("macSubTypeId") macSubTypeId: Long,
                                      @Header("Authorization") accessToken: String):
            Deferred<StorageAreasResponse>

    @GET("$LINE_API_ADDED_URL/api/areas/{areaId}/alt-machines")
    fun getAlternativeMachinesByAreaAsync(
        @Path("areaId") areaId: Long,
        @Query("macSubTypeId") macSubTypeId: Long,
        @Query("brandId") brandId: String,
        @Header("Authorization") accessToken: String
    ): Deferred<MachineAreaAvailableResponse>

    @PUT("$LINE_API_ADDED_URL/api/lines/checkin/non-line")
    fun moveMachineAsync(
        @Header("Authorization") accessToken: String,
        @Body moveMachineRequest: MoveMachineRequest
    ): Deferred<MoveMachineResponse>

    @POST("$LINE_API_ADDED_URL/api/lines/request")
    fun sendRequestAsync(
        @Body sendRequestRequest: SendRequestRequest,
        @Header("Authorization") accessToken: String
    ): Deferred<SendRequestResponse>

    @POST("$LINE_API_ADDED_URL/api/areas/save-selected-areas-no-lines")
    fun saveAreasNoLinesAsync(
        @Body body: SaveAreasNoLines
    ): Deferred<Any>

    @GET("$LINE_API_ADDED_URL/api/areas/selected-areas-no-lines")
    fun getAreasNoLinesAsync(): Deferred<AreaResponse>

    @GET("$LINE_API_ADDED_URL/api/areas/areas-no-lines")
    fun getAreasNoLinesAsync(
        @Query("buildingId") buildingId: Int? = null
    ): Deferred<AreaResponse>

}