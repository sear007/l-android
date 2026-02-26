package co.ltlabs.ltmechanic.repository.peraccess

import co.ltlabs.ltmechanic.domain.PerAccessResponse
import co.ltlabs.ltmechanic.network.ProductConfigResponse
import com.ltlabs.lt_core.network.Resource
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow

interface PerAccessRepo {

    fun getProductAccessAsync(): Deferred<PerAccessResponse>

    fun getProductConfig(): Flow<Resource<out ProductConfigResponse?>>

}