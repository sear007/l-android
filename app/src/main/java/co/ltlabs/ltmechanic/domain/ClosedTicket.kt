package co.ltlabs.ltmechanic.domain

import java.util.*

data class ClosedTicket (
    val id: Long,
    val ticketNo: String,
    val machineNo: String,
    val username: String,
    val status: String,
    var date: Date?,
    var time: String,
    var place: String?,
    var reportedPlace: String?
)