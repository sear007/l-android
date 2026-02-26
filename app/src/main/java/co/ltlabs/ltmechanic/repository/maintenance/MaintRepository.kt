package co.ltlabs.ltmechanic.repository.maintenance

import androidx.paging.PagingData
import co.ltlabs.ltmechanic.domain.ChecklistRequest
import co.ltlabs.ltmechanic.domain.MaintenanceChecklist
import co.ltlabs.ltmechanic.domain.changeover.COItem
import co.ltlabs.ltmechanic.domain.changeover.COItemResponse
import co.ltlabs.ltmechanic.domain.changeover.COResponse
import co.ltlabs.ltmechanic.domain.maint.MaintItem
import co.ltlabs.ltmechanic.domain.maintenance.ChecklistResponse
import co.ltlabs.ltmechanic.repository.paging.BasePaging
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import retrofit2.http.Query

interface MaintRepository {

    fun getMaints(
        type: String,
        machine: String? = null,
        lineSelected: String? = null,
        areaSelected: String? = null
    ): Flow<PagingData<MaintItem>>

    fun getChecklistAsync(): Deferred<ChecklistResponse>

    fun attachMachineWithChecklistAsync(ticketId: Long, body: ChecklistRequest): Deferred<Any>

}