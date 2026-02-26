package co.ltlabs.ltmechanic.network.auth

import co.ltlabs.ltmechanic.network.AppStore
import co.ltlabs.ltmechanic.network.AppStoreInfo
import co.ltlabs.ltmechanic.network.NonLineAreasResponse
import co.ltlabs.ltmechanic.util.REFERENCE_API_ADDED_URL
import kotlinx.coroutines.Deferred
import retrofit2.http.*

interface Reference2Api {

    @GET("$REFERENCE_API_ADDED_URL/api/ref/areas/non-line")
    fun getNonLineAreasAsync(@Header("Authorization") accessToken: String):
            Deferred<NonLineAreasResponse>

    @POST("$REFERENCE_API_ADDED_URL/api/ref/appStore")
    fun getAppInfoAsync(@Body requestBody: AppStore): Deferred<List<AppStoreInfo>>

}