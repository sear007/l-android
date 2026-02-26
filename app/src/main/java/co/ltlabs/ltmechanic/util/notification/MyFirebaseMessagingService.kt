package co.ltlabs.ltmechanic.util.notification

import android.content.Intent
import co.ltlabs.ltmechanic.constant.AppConfig
import co.ltlabs.ltmechanic.ui.main.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        remoteMessage.data.isNotEmpty().let {
            val intent =
                Intent(this, MainActivity::class.java).apply {
                    putExtra(
                        AppConfig.EXTRA_REFERENCE,
                        remoteMessage.data[NotificationClient.KEY_REFERENCE].toString()
                    )
                    putExtra(
                        AppConfig.EXTRA_ACTION,
                        remoteMessage.data[NotificationClient.KEY_ACTION].toString()
                    )
                }
            NotificationClient.sendNotification(
                this,
                remoteMessage.data[NotificationClient.KEY_TITLE].toString(),
                remoteMessage.data[NotificationClient.KEY_BODY].toString(),
                remoteMessage.data[NotificationClient.KEY_ACTION].toString(),
                intent
            )
        }
    }
}