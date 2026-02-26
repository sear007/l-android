package co.ltlabs.ltmechanic.repository.tickets

import co.ltlabs.ltmechanic.domain.BuildingResponse
import co.ltlabs.ltmechanic.network.DashboardStatisticsRequest
import co.ltlabs.ltmechanic.network.MachineTicketResponse
import co.ltlabs.ltmechanic.network.main.TicketApi
import kotlinx.coroutines.Deferred
import javax.inject.Inject

class TicketsRepositoryImpl @Inject constructor(
    private val ticketsApi: TicketApi
) : TicketsRepository {

    override fun getTicketStatisticsAsync(
        req: DashboardStatisticsRequest
    ): Deferred<MachineTicketResponse> {
        return ticketsApi.getTicketStatistics2Async(req)
    }

    override fun getBuildingAsync(): Deferred<BuildingResponse> {
        return ticketsApi.getBuildingAsync()
    }
}