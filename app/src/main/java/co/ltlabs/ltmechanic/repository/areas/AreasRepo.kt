package co.ltlabs.ltmechanic.repository.areas

import co.ltlabs.ltmechanic.domain.AreaResponse
import co.ltlabs.ltmechanic.network.SaveAreasNoLines
import kotlinx.coroutines.Deferred

interface AreasRepo {

    fun getAreasNoLinesAsync(buildingId: Int? = null): Deferred<AreaResponse>

    fun saveAreasNoLinesAsync(body: SaveAreasNoLines): Deferred<Any>

}