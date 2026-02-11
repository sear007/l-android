package co.ltlabs.ltmechanic.domain

import co.ltlabs.ltmechanic.database.DatabaseNotification
import java.util.*

data class Notification (
    val id: Int,
    val username: String,
    val dateTime: Date,
    val message: String,
    val type: String,
    val ticketId: String,
    val generatedDate: String,
    val location: String,
    val machineNo: String,
    val rfid: String,
    val subType: String,
    val millis: String
)

fun List<Notification>.asNotificationDatabaseModel(): List<DatabaseNotification> {
    return map {
        DatabaseNotification(
            username = it.username,
            createdDate = it.dateTime,
            message = it.message,
            type = it.type,
            ticketId = it.ticketId,
            generatedDate = it.generatedDate,
            location = it.location,
            machineNo = it.machineNo,
            rfid = it.rfid,
            subType = it.subType,
            millis = it.millis
        )
    }
}

fun Notification.asNotificationObjDatabaseModel(): DatabaseNotification {
    return DatabaseNotification(
        username = this.username,
        createdDate = this.dateTime,
        message = this.message,
        type = this.type,
        ticketId = this.ticketId,
        generatedDate = this.generatedDate,
        location = this.location,
        machineNo = this.machineNo,
        rfid = this.rfid,
        subType = this.subType,
        millis = this.millis
    )
}