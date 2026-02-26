package co.ltlabs.ltmechanic.network

import co.ltlabs.ltmechanic.domain.AppConfigRequest
import co.ltlabs.ltmechanic.domain.CompanyInfoResponse
import kotlinx.coroutines.Deferred
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiCompanyLogin {

    @POST("mobile/companylogin")
    fun loginCompanyAsync(@Body body: AppConfigRequest): Deferred<CompanyInfoResponse>

}