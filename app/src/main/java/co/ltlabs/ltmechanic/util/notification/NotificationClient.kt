package co.ltlabs.ltmechanic.util.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import co.ltlabs.ltmechanic.BuildConfig
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.constant.AppConfig
import co.ltlabs.ltmechanic.constant.TicketType
import co.ltlabs.ltmechanic.constant.type.UserType
import co.ltlabs.ltmechanic.domain.NextMainDate
import co.ltlabs.ltmechanic.domain.Ticket
import co.ltlabs.ltmechanic.util.AuthUtil
import co.ltlabs.ltmechanic.util.DateUtil
import java.util.*

object NotificationClient {

    private val notificationIdList = mutableListOf<Int>()
    private const val CHANNEL_ID = "ltm-channel"
    private const val CHANNEL_NAME = "LTm Channel"
    private const val CHANNEL_DESCRIPTION = "LTm Notification"

    const val KEY_TITLE = "title"
    const val KEY_BODY = "body"
    const val KEY_CATEGORY = "category"
    const val KEY_ACTION = "action"
    const val KEY_REFERENCE = "referenceNo"
    const val KEY_FACTORY_ID = "factoryId"
    const val KEY_COMPANY_CODE = "companyCode"
    const val SEND_BROADCAST = "SEND_BROADCAST"

    fun sendNotification(
        context: Context,
        title: String,
        content: String,
        action: String,
        intent: Intent
    ) {
        val type = TicketType.fromCodeToType(action)
        // To check if notification type is Reported or Re-opened
        val isLineLeader: Boolean
        val audio: Int
        if (type is TicketType.Reported || type is TicketType.Reopen) {
            audio = R.raw.reported_reported_audio
            isLineLeader = false
        } else {
            if (AuthUtil.role == UserType.LINE_LEADER && type is TicketType.Repaired) {
                audio = R.raw.line_leader_ringtone
                isLineLeader = true
            } else {
                stopAudio()
                audio = 0
                isLineLeader = false
            }
        }

        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        sendNotify(context)
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.description = CHANNEL_DESCRIPTION
            notificationChannel.enableLights(true)
            notificationChannel.vibrationPattern = listOf<Long>(0, 1000, 500, 1000).toLongArray()
            notificationChannel.enableVibration(true)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val notificationBuilder = NotificationCompat.Builder(
            context,
            CHANNEL_ID
        )
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentIntent(pendingIntent)
            .setContentTitle(title)
            .setAutoCancel(true)
            .setSound(defaultSound)
            .setContentText(content)
            .setDefaults(Notification.DEFAULT_ALL)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(content)
            )
        val id = notificationId()
        notificationIdList.add(id)
        notificationManager.notify(id, notificationBuilder.build())
        playAudio(context, audio, isLineLeader)

    }

    private fun sendNotify(context: Context) {
        val intent = Intent(SEND_BROADCAST).apply {
            setPackage(BuildConfig.APPLICATION_ID)
            putExtra(AppConfig.EXTRA_GET_NOTIFY, true)
        }
        context.sendBroadcast(intent)
    }

    private fun notificationId(): Int {
        val random = Random()
        return random.nextInt(100) + 100
    }

    private var mediaPlayer: MediaPlayer? = null
    private var count = 1
    private fun playAudio(context: Context, audio: Int, isLineLeader: Boolean) {
        if (audio == 0) return
        if (mediaPlayer != null) {
            mediaPlayer?.release()
            mediaPlayer = null
            count = 1
        }
        mediaPlayer = MediaPlayer.create(context, audio)
        mediaPlayer?.apply {
            if (isPlaying) {
                count = 1
                seekTo(0)
                return
            }
            start()
        }
        if (isLineLeader) {
            mediaPlayer?.setOnCompletionListener {
                if (count < 3) {
                    it.seekTo(0) // Reset the media playback to the beginning
                    it.start() // Start playing again
                    count++
                } else {
                    count = 1
                    stopAudio()
                }
            }
        }
    }

    fun stopAudio() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun removeNotification(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationIdList.map {
            notificationManager.cancel(it)
        }
        notificationIdList.clear()
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
            is TicketType.Close -> {
                if (isLineLeader) {
                    val bundle = bundleOf("isClosedTicket" to true)
                    navController.navigate(
                        R.id.action_global_to_lineLeaderRepairedTicketsFragment,
                        bundle
                    )
                }
            }
            is TicketType.Reported, TicketType.Reopen -> {
                val bundle = bundleOf(
                    "ticket_type" to type.code
                )
                if (isLineLeader) {
                    navController.navigate(R.id.lineLeaderReportedTicketsFragment, bundle)
                } else {
                    navController.navigate(R.id.mechanicReportedTicketsFragment, bundle)
                }
            }
            else -> {
                return
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
            "reportedTime" to formatDate(ticket.reported),
            "grabbedTime" to formatDate(ticket.grabbedDt),
            "closedTime" to formatDate(ticket.closedDt),
            "elapsedDuration" to ticket.elapsedDuration,
            "repairedTime" to formatDate(ticket.repairedDt),
            "nextMainDate" to NextMainDate(ticket.reported, ticket.nextMaintDate)
        )
    }

    private fun formatDate(date: Date?) = DateUtil.formatToDate(date, DateUtil.DATE_TIME_FORMAT)

}