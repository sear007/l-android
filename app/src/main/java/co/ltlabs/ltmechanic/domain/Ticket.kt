package co.ltlabs.ltmechanic.domain

import java.util.*

data class Ticket(
    val id: Long,
    val machineId: Long,
    val ticketNo: String,
    val machineNo: String,
    val problem: String,
    val solution: String,
    val subType: String,
    val lpmDate: Date?,
    val maintenanceFreq: String,
    val rental: String,
    val problemTypeId: Long,
    val solutionTypeId: Long?,
    val remarks: String,
    var status: String,
    val place: String,
    val reportedPlace: String,
    val brand: String,
    var ticketAsset: List<TicketAsset>? = null,
    val closedBy: String?,
    val createdDt: Date?,
    val createdBy: String,
    val grabbedBy: String,
    val updatedDt: Date?,
    val reported: Date?,
    val grabbedDt: Date?,
    val repairedDt: Date?,
    var closedDt: Date?,
    val elapsedDuration: String,
    val repairedBy: String?,
    val inrepairDuration: String?,
    val nextMaintDate: Date?
)