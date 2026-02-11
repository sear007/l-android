package co.ltlabs.ltmechanic.domain

import java.util.*

data class TicketLogs(
    val reportedBy: String?,
    val reportedDt: Date?,
    val repairedBy: String?,
    val solution: String?,
    val remarks: String?,
    var reopenedDt: Date?,
    var reopenedBy: String?,
)