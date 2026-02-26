package co.ltlabs.ltmechanic.network

import co.ltlabs.ltmechanic.constant.AppConfig.DEVICE_TOKEN
import co.ltlabs.ltmechanic.constant.AppConfig.LANGUAGES
import co.ltlabs.ltmechanic.constant.AppConfig.LOGOUT
import co.ltlabs.ltmechanic.constant.AppConfig.PRODUCT_ACCESS
import co.ltlabs.ltmechanic.constant.AppConfig.REFRESH_TOKEN
import co.ltlabs.ltmechanic.constant.AppConfig.SWITCH_FACTORY
import co.ltlabs.ltmechanic.constant.AppConfig.TRANSLATION
import co.ltlabs.ltmechanic.constant.AppConfig.USER_LOGIN
import co.ltlabs.ltmechanic.domain.Employee
import co.ltlabs.ltmechanic.domain.EmployeeResponse
import co.ltlabs.ltmechanic.domain.PerAccessResponse
import co.ltlabs.ltmechanic.domain.RfidRequest
import co.ltlabs.ltmechanic.domain.request.DeviceTokenRequest
import co.ltlabs.ltmechanic.domain.request.LogoutRequest
import com.google.gson.JsonObject
import kotlinx.coroutines.Deferred
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiGlobal {

    @POST(USER_LOGIN)
    fun loginEmployeeAsync(@Body body: Employee): Deferred<EmployeeResponse>

    @POST(USER_LOGIN)
    fun loginEmployeeWithRfidAsync(@Body body: RfidRequest): Deferred<EmployeeResponse>

    @GET(LANGUAGES)
    fun getLanguagesAsync(
        @QueryMap params: Map<String, String>
    ): Deferred<LanguagesResponse>

    @GET(TRANSLATION)
    fun getTranslationsAsync(
        @QueryMap params: Map<String, String>
    ): Deferred<ResponseBody>

    @FormUrlEncoded
    @POST(REFRESH_TOKEN)
    fun refreshTokenAsync(
        @Field("refreshToken") refreshToken: String
    ): Deferred<JsonObject>

    @GET(PRODUCT_ACCESS)
    fun getProductAccessAsync(
        @QueryMap params: Map<String, String>
    ): Deferred<PerAccessResponse>

    @FormUrlEncoded
    @POST(SWITCH_FACTORY)
    fun switchFactoryAsync(
        @Field("factoryId") factoryId: Int,
        @Field("accessToken") refreshToken: String
    ): Deferred<SwitchFactoryResponse>

    @POST(DEVICE_TOKEN)
    fun deviceRegister(
        @Body body: DeviceTokenRequest
    ): Call<Any>

    @POST(LOGOUT)
    fun logout(
        @Body body: LogoutRequest
    ): Call<Any>

}