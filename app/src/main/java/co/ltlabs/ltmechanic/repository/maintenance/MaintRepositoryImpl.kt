package co.ltlabs.ltmechanic.repository.maintenance

import android.app.Application
import androidx.paging.Pager
import androidx.paging.PagingConfig
import co.ltlabs.ltmechanic.database.getDatabase
import co.ltlabs.ltmechanic.domain.ChecklistRequest
import co.ltlabs.ltmechanic.domain.CommonResponse
import co.ltlabs.ltmechanic.domain.maintenance.ChecklistResponse
import co.ltlabs.ltmechanic.network.ApiMaint
import co.ltlabs.ltmechanic.network.main.ReferenceApi
import co.ltlabs.ltmechanic.network.main.TicketApi
import kotlinx.coroutines.Deferred
import javax.inject.Inject

class MaintRepositoryImpl @Inject constructor(
    private val api: ApiMaint,
    private val referApi: ReferenceApi,
    private val ticketApi: TicketApi,
    application: Application
) : MaintRepository {

    val database = getDatabase(application)

    override fun getMaints(
        type: String, machine: String?, lineSelected: String?, areaSelected: String?
    ) = Pager(config = PagingConfig(
        pageSize = 20, enablePlaceholders = false
    ), pagingSourceFactory = {
        MaintPagingDataSource(
            database.maintDao, api, type, machine, lineSelected, areaSelected
        )
    }).flow

    override fun getChecklistAsync(): Deferred<ChecklistResponse> {
        return referApi.getChecklistAsync()
    }

    override fun attachMachineWithChecklistAsync(
        ticketId: Long, body: ChecklistRequest
    ): Deferred<CommonResponse> {
        return ticketApi.attachMachineWithChecklistAsync(ticketId, body)
    }

}