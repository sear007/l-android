package co.ltlabs.ltmechanic.repository.lines

import co.ltlabs.ltmechanic.network.LinesResponse
import co.ltlabs.ltmechanic.network.main.LineApi
import kotlinx.coroutines.Deferred
import javax.inject.Inject

class LinesRepositoryImpl @Inject constructor(
    private val api: LineApi
) : LinesRepository {

    override fun getAssignedLinesByAreasAsync(): Deferred<LinesResponse> {
        return api.getUserLinesByAssignedAndSelectedAsync()
    }

}