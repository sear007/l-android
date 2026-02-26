package co.ltlabs.ltmechanic.repository.peraccess

import co.ltlabs.ltmechanic.constant.AppConfig.APP_NAME
import co.ltlabs.ltmechanic.domain.PerAccessResponse
import co.ltlabs.ltmechanic.network.ApiGlobal
import co.ltlabs.ltmechanic.network.ProductConfigResponse
import co.ltlabs.ltmechanic.network.main.ReferenceApi
import com.ltlabs.lt_core.network.Resource
import com.ltlabs.lt_core.network.networkRequest
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import javax.inject.Inject

class PerAccessRepoImpl @Inject constructor(
    private val api: ApiGlobal,
    private val referenceApi: ReferenceApi
) : PerAccessRepo {

    override fun getProductAccessAsync(): Deferred<PerAccessResponse> {
        val params = mutableMapOf<String, String>()
        params["product"] = APP_NAME
        return api.getProductAccessAsync(params)
    }

    override fun getProductConfig(): Flow<Resource<out ProductConfigResponse?>> {
        return networkRequest { referenceApi.getProductConfig() }
    }

}