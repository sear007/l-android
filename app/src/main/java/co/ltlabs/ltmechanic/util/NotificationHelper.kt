package co.ltlabs.ltmechanic.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.constant.TicketType
import co.ltlabs.ltmechanic.constant.type.UserType
import co.ltlabs.ltmechanic.domain.Ticket
import co.ltlabs.ltmechanic.ui.main.MainActivity
import org.json.JSONObject
import javax.inject.Inject

class NotificationHelper @Inject constructor(
    private val languageJsonObject: JSONObject
) {

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "101"
        private const val notificationId = 1
        const val TICKET_TYPE_EXTRA = "TICKET_TYPE_EXTRA"
    }

    fun sendNotification(
        context: Context,
        content: String,
        type: TicketType
    ) {
        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val title = languageJsonObject.getTranslation(type.title)
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra(TICKET_TYPE_EXTRA, type.code)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "LTm Notification",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            // Configure Notification Channel
            notificationChannel.description = "LTm Notification"
            notificationChannel.enableLights(true)
            notificationChannel.vibrationPattern = listOf<Long>(0, 1000, 500, 1000).toLongArray()
            notificationChannel.enableVibration(true)

            notificationManager.createNotificationChannel(notificationChannel)
        }

        val contentStyle = NotificationCompat
            .BigTextStyle()
            .bigText(content)

        val notificationBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setAutoCancel(true)
            .setSound(defaultSound)
            .setContentText(content)
            .setContentIntent(pendingIntent)
            .setStyle(contentStyle)
            .setWhen(System.currentTimeMillis())
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    fun removeNotification(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }

    fun navigateTo(
        navController: NavController,
        type: TicketType,
        ticket: Ticket?
    ) {
        ticket ?: return
        val isLineLeader = AuthUtil.role == UserType.LINE_LEADER
        when (type) {
            is TicketType.InRepair -> {
                if (isLineLeader) {
                    navController.navigate(
                        R.id.action_global_to_lineLeaderTicketPreviewFragment,
                        bundleTicketPreviewFragment(ticket)
                    )
                } else {
                    navController.navigate(
                        R.id.action_global_to_mechanicInRepairTicketsPreviewFragment,
                        bundleTicketPreviewFragment(ticket)
                    )
                }
            }
            is TicketType.Repaired -> {
                if (isLineLeader) {
                    navController.navigate(R.id.lineLeaderRepairedTicketsFragment)
                } else {
                    navController.navigate(R.id.mechanicRepairedTicketsFragment)
                }
            }
            else -> {
                if (isLineLeader) {
                    navController.navigate(R.id.lineLeaderReportedTicketsFragment)
                } else {
                    navController.navigate(R.id.mechanicReportedTicketsFragment)
                }
            }
        }
    }

    private fun bundleTicketPreviewFragment(ticket: Ticket): Bundle {
        var imageAttachmentUrl1 = ""
        var imageAttachmentUrl2 = ""
        var imageAttachmentUrl3 = ""
        var videoAttachmentUrl = ""
        var videoAttachmentUrl2 = ""
        ticket.ticketAsset?.let { assets ->

            val videoAssets = assets.filter { it.link.contains(".mp4") }
            val imageAssets =
                assets.filter { it.link.contains(".png") || it.link.contains(".jpg") }
                    .sortedByDescending { it.id }


            for ((index, videoAsset) in videoAssets.withIndex()) {
                when (index) {
                    0 -> {
                        videoAttachmentUrl = videoAsset.link
                    }
                    1 -> {
                        videoAttachmentUrl2 = videoAsset.link
                    }
                }

            }

            for ((index, imageAsset) in imageAssets.withIndex()) {

                when (index) {

                    0 -> {
                        imageAttachmentUrl1 = imageAsset.link
                    }

                    1 -> {
                        imageAttachmentUrl2 = imageAsset.link
                    }

                    2 -> {
                        imageAttachmentUrl3 = imageAsset.link
                    }

                }

            }

        }
        return bundleOf(
            "ticketId" to ticket.id,
            "ticketNo" to ticket.ticketNo,
            "machineNo" to ticket.machineNo,
            "problem" to ticket.problem,
            "remarks" to ticket.remarks,
            "solution" to ticket.solution,
            "brand" to ticket.brand,
            "place" to ticket.place,
            "reportedPlace" to ticket.reportedPlace,
            "imageAttachmentUrl1" to imageAttachmentUrl1,
            "imageAttachmentUrl2" to imageAttachmentUrl2,
            "imageAttachmentUrl3" to imageAttachmentUrl3,
            "videoAttachmentUrl" to videoAttachmentUrl,
            "videoAttachmentUrl2" to videoAttachmentUrl2,
            "status" to ticket.status,
            "machineId" to ticket.machineId,
            "problemTypeId" to ticket.problemTypeId,
            "solutionTypeId" to ticket.solutionTypeId,
            "reportedTime" to ticket.reported,
            "grabbedTime" to ticket.grabbedDt,
            "closedTime" to ticket.closedDt,
            "elapsedDuration" to ticket.elapsedDuration,
            "repairedTime" to ticket.repairedDt
        )
    }

}