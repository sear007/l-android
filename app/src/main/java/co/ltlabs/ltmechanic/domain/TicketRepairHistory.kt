package co.ltlabs.ltmechanic.domain

import java.util.*

class TicketRepairHistory (
    val ticketNo: String,
    val problem: String,
    val solution: String,
    val remarks: String,
    val status: String,
    var date: Date?,
    val username: String
)

fun List<TicketRepairHistory>.asMachineHistoryDomainModel(): List<MachineHistory> {
    return map {
        MachineHistory(
            it.ticketNo,
            it.problem,
            it.solution,
            it.remarks,
            it.status,
            it.date,
            it.username
        )
    }
}