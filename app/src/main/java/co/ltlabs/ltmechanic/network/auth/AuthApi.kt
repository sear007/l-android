package co.ltlabs.ltmechanic.network.auth

import co.ltlabs.ltmechanic.network.LoginRequest
import co.ltlabs.ltmechanic.network.LoginResponse
import co.ltlabs.ltmechanic.util.AUTH_API_ADDED_URL
import kotlinx.coroutines.Deferred
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("$AUTH_API_ADDED_URL/api/login")
    fun loginAsync(@Body loginRequest: LoginRequest):
            Deferred<LoginResponse>
}