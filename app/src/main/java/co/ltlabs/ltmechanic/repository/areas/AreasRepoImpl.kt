package co.ltlabs.ltmechanic.repository.areas

import co.ltlabs.ltmechanic.domain.AreaResponse
import co.ltlabs.ltmechanic.network.SaveAreasNoLines
import co.ltlabs.ltmechanic.network.main.LineApi
import kotlinx.coroutines.Deferred
import javax.inject.Inject

class AreasRepoImpl @Inject constructor(
    private val api: LineApi
) : AreasRepo {

    override fun getAreasNoLinesAsync(buildingId: Int?): Deferred<AreaResponse> {
        return if (buildingId == null)
            api.getAreasNoLinesAsync()
        else api.getAreasNoLinesAsync(buildingId)
    }

    override fun saveAreasNoLinesAsync(body: SaveAreasNoLines): Deferred<Any> {
        return api.saveAreasNoLinesAsync(body)
    }

}