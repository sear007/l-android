package co.ltlabs.ltmechanic.network.main

import co.ltlabs.ltmechanic.domain.AreasByBuildingResponse
import co.ltlabs.ltmechanic.domain.BuildingResponse
import co.ltlabs.ltmechanic.domain.ChecklistRequest
import co.ltlabs.ltmechanic.domain.CommonResponse
import co.ltlabs.ltmechanic.network.*
import co.ltlabs.ltmechanic.util.TICKET_API_ADDED_URL
import kotlinx.coroutines.Deferred
import retrofit2.http.*

interface TicketApi {

    @POST("$TICKET_API_ADDED_URL/api/tickets/statistics")
    fun getTicketStatistics2Async(
        @Body param: DashboardStatisticsRequest? = null
    ): Deferred<MachineTicketResponse>

    @POST("$TICKET_API_ADDED_URL/api/tickets/statistics")
    fun getTicketStatisticsAsync(@Body ticketStatisticsRequest: TicketStatisticsRequest):
            Deferred<MachineTicketResponse>

    @POST("$TICKET_API_ADDED_URL/api/tickets/total-tickets-over-machines")
    fun getMachineTicketCountsAsync(
        @Body ticketStatisticsRequest: TicketStatisticsRequest,
        @Header("Authorization") accessToken: String
    ):
            Deferred<MachineTicketResponse>

    @GET("$TICKET_API_ADDED_URL/api/tickets/problem-list")
    fun getTicketProblemsAsync(
        @Query("machineId") machineId: Long,
        @Header("Authorization") accessToken: String
    ):
            Deferred<MachineProblemResponse>

    @POST("$TICKET_API_ADDED_URL/api/tickets")
    fun createTicketAsync(
        @Body createTicketRequest: CreateTicketRequest,
        @Header("Authorization") accessToken: String
    ):
            Deferred<TicketResponse>

    @GET("$TICKET_API_ADDED_URL/api/tickets/reported")
    fun getReportedTicketsAsync(
        @Query("lineSelected") lineSelected: String = "",
        @Query("areaSelected") areaSelected: String = ""
    ): Deferred<ReportedTicketResponse>

    @GET("$TICKET_API_ADDED_URL/api/tickets/in-repair")
    fun getInRepairTicketsAsync(
        @Query("lineSelected") lineSelected: String = "",
        @Query("areaSelected") areaSelected: String = ""
    ):
            Deferred<InRepairTicketResponse>

    @GET("$TICKET_API_ADDED_URL/api/tickets/repaired")
    fun getRepairedTicketsAsync(
        @Query("lineSelected") lineSelected: String = "",
        @Query("areaSelected") areaSelected: String = ""
    ):
            Deferred<RepairedTicketResponse>

    @GET("$TICKET_API_ADDED_URL/api/tickets/closed")
    fun getClosedTicketsAsync(
        @Query("lineSelected") lineSelected: String = "",
        @Query("areaSelected") areaSelected: String = ""
    ):
            Deferred<ClosedTicketResponse>

    @GET("$TICKET_API_ADDED_URL/api/tickets/{ticketId}")
    fun getTicketDetailsAsync(
        @Path("ticketId") ticketId: Long,
        @Header("Authorization") accessToken: String
    ):
            Deferred<TicketResponse>

    @GET("$TICKET_API_ADDED_URL/api/tickets/by-ticketno/{ticketNo}")
    fun getTicketDetailsByTicketNoAsync(
        @Path("ticketNo") ticketNo: String,
        @Header("Authorization") accessToken: String
    ):
            Deferred<TicketResponse>

    @PUT("$TICKET_API_ADDED_URL/api/tickets/update-status")
    fun updateTicketStatusAsync(
        @Body updateTicketStatusRequest: UpdateTicketStatusRequest,
        @Header("Authorization") accessToken: String
    ):
            Deferred<UpdateTicketStatusResponse>

    @PUT("$TICKET_API_ADDED_URL/api/tickets/reopen")
    fun reopenTicketAsync(
        @Body reopenTicketRequest: ReopenTicketRequest,
        @Header("Authorization") accessToken: String
    ):
            Deferred<ReopenTicketResponse>

    @GET("$TICKET_API_ADDED_URL/api/tickets/machine-repaired-history")
    fun getMachineRepairedHistoryAsync(
        @Query("machineId") machineId: Long,
        @Header("Authorization") accessToken: String
    ):
            Deferred<MachineRepairedHistoryResponse>

    @PUT("$TICKET_API_ADDED_URL/api/tickets/update-checklist")
    fun updateChecklistAsync(
        @Body checklistRequest: UpdateChecklistRequest,
        @Header("Authorization") accessToken: String
    ):
            Deferred<UpdateChecklistResponse>

    @GET("$TICKET_API_ADDED_URL/api/tickets/open-pm-tickets")
    fun getMaintenanceTicketsAsync(
        @Query("lineIds") lineIds: String,
        @Query("pmPlanDt") pmPlanDt: String,
        @Query("status") status: String,
        @Header("Authorization") accessToken: String
    ): Deferred<MachineMaintenanceResponse>

    @GET("$TICKET_API_ADDED_URL/api/tickets/maintenance-history")
    fun getMaintenanceHistoryAsync(
        @Query("machineId") machineId: Long,
        @Query("status") status: String = "",
        @Query("isQueryMachine") isQueryMachine: Boolean = false,
        @Header("Authorization") accessToken: String
    ): Deferred<MaintenanceHistoryResponse>

    @GET("$TICKET_API_ADDED_URL/api/tickets/repair-history")
    fun getTicketRepairHistoryAsync(
        @Query("machineId") machineId: Long,
        @Query("isQueryMachine") isQueryMachine: Boolean = true,
        @Header("Authorization") accessToken: String
    ): Deferred<TicketRepairHistoryResponse>

    @GET("$TICKET_API_ADDED_URL/api/tickets/dashboard-building")
    fun getBuildingAsync(): Deferred<BuildingResponse>

    @GET("$TICKET_API_ADDED_URL/api/tickets/dashboard-area")
    fun getAreasByBuildingAsync(
        @Query("buildingId") buildingId: Int
    ): Deferred<AreasByBuildingResponse>

    @PUT("$TICKET_API_ADDED_URL/api/maintenances/{id}/attach-checklist")
    fun attachMachineWithChecklistAsync(
        @Path("id") id: Long,
        @Body body: ChecklistRequest
    ): Deferred<CommonResponse>

}