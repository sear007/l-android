package co.ltlabs.ltmechanic.network.main

import co.ltlabs.ltmechanic.network.FirebaseSendNotificationRequest
import co.ltlabs.ltmechanic.network.FirebaseSendNotificationResponse
import kotlinx.coroutines.Deferred
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface FirebaseCloudMessagingApi {

    @POST("/v1/projects/ltmechanic/messages:send")
    fun sendNotificationByTopicAsync(
        @Header("Authorization") accessToken: String,
        @Body firebaseSendNotificationRequest: FirebaseSendNotificationRequest):
            Deferred<FirebaseSendNotificationResponse>
}