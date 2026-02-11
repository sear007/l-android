package co.ltlabs.ltmechanic.network.main

import co.ltlabs.ltmechanic.network.*
import co.ltlabs.ltmechanic.util.MACHINE_API_ADDED_URL
import kotlinx.coroutines.Deferred
import retrofit2.http.*

interface MachineApi {

    @GET("$MACHINE_API_ADDED_URL/api/machines/{id}")
    fun getMachineByIdAsync(@Path("id") id: Int, @Header("Authorization") accessToken: String):
            Deferred<MachineResponse>

    @GET("$MACHINE_API_ADDED_URL/api/machines")
    fun getMachinesByFactoryIdAsync(
        @Query("factoryId") factoryId: Int,
        @Query("currentPage") currentPage: Int = 1,
        @Query("pageSize") pageSize: Int = 10
    ):
            Deferred<MachinesResponse>

    @GET("$MACHINE_API_ADDED_URL/api/machines/by-machineno/{machineNo}")
//    @FormUrlEncoded
    fun getMachineByMachineNoAsync(
        @Path("machineNo", encoded = true) machineNo: String, @Query("userId") userId: Int,
        @Header("Authorization") accessToken: String
    ):
            Deferred<MachineResponse>

    @GET("$MACHINE_API_ADDED_URL/api/machines/by-rfid/{rfid}")
    fun getMachineByRfidAsync(@Path("rfid") rfid: String):
            Deferred<MachineResponse>

    @GET("$MACHINE_API_ADDED_URL/api/machines/by-serial/{serialNo}")
    fun getMachineBySerialNoAsync(@Path("serialNo") machineNo: String, @Query("userId") userId: Int,
                                  @Header("Authorization") accessToken: String):
            Deferred<MachineResponse>

    @PUT("$MACHINE_API_ADDED_URL/api/machines/checkin")
    fun checkInMachineAsync(@Body machineCheckInRequest: MachineCheckInRequest, @Query("replace") replace: String = "n",
                            @Header("Authorization") accessToken: String):
            Deferred<MachineCheckInResponse>

    @PUT("$MACHINE_API_ADDED_URL/api/machines/{machineId}/checkout")
    fun checkOutMachineAsync(@Body machineCheckOutRequest: MachineCheckOutRequest, @Path("machineId") machineId: Int,
                             @Header("Authorization") accessToken: String):
            Deferred<MachineCheckOutResponse>

    @GET("$MACHINE_API_ADDED_URL/api/machines/by-line-placement/{lineId}")
    fun getMachinesByLinePlacementAsync(@Path("lineId") lineId: Long, @Query("userId") userId: Int,
                                        @Header("Authorization") accessToken: String):
            Deferred<MachinesStationResponse>

    @GET("$MACHINE_API_ADDED_URL/api/machines/by-station/{station}")
    fun getMachinesByStationNumberAsync(@Path("station") station: String,
                                        @Query("mfgLineId") mfgLineId: Long,
                                        @Header("Authorization") accessToken: String): Deferred<MachinesInStationResponse>

    @PUT("$MACHINE_API_ADDED_URL/api/machines/{machineId}/rfid")
    fun attachMachineRFIDAsync(
        @Path("machineId") machineId: Long,
        @Body attachMachineNFCRequest: AttachMachineNFCRequest,
        @Header("Authorization") accessToken: String
    ): Deferred<AttachMachineNFCResponse>

}