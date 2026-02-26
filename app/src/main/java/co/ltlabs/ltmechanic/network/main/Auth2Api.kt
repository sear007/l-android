package co.ltlabs.ltmechanic.network.main

import co.ltlabs.ltmechanic.network.*
import co.ltlabs.ltmechanic.util.AUTH_API_ADDED_URL
import com.google.gson.JsonObject
import kotlinx.coroutines.Deferred
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.*

interface Auth2Api {

    @PUT("$AUTH_API_ADDED_URL/api/login/{userId}/changepwd")
    fun changePasswordAsync(
        @Body changePasswordRequest: ChangePasswordRequest,
        @Path("userId") userId: Int,
        @Header("Authorization") accessToken: String
    ): Deferred<ChangePasswordResponse>

    @POST("$AUTH_API_ADDED_URL/api/login")
    fun loginAsync(@Body loginRequest: LoginRequest):
            Deferred<LoginResponse>

    @FormUrlEncoded
    @POST("$AUTH_API_ADDED_URL/api/refresh")
    fun refreshToken(@Field("refresh_token") refresh_token : String) : Deferred<JsonObject>
}