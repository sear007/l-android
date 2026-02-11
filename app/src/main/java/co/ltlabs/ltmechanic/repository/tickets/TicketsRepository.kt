package co.ltlabs.ltmechanic.repository.tickets

import co.ltlabs.ltmechanic.domain.BuildingResponse
import co.ltlabs.ltmechanic.network.DashboardStatisticsRequest
import co.ltlabs.ltmechanic.network.MachineTicketResponse
import kotlinx.coroutines.Deferred

interface TicketsRepository {

    fun getTicketStatisticsAsync(
        req: DashboardStatisticsRequest
    ): Deferred<MachineTicketResponse>

    fun getBuildingAsync(): Deferred<BuildingResponse>

}