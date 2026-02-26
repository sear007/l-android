package co.ltlabs.ltmechanic.repository.lines

import co.ltlabs.ltmechanic.network.LinesResponse
import kotlinx.coroutines.Deferred

interface LinesRepository {

    fun getAssignedLinesByAreasAsync(): Deferred<LinesResponse>

}