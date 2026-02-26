package co.ltlabs.ltmechanic.domain

import java.util.*

data class MachineMaintenance (
    val id: Long,
    val ticketNo: String,
    val machineID: Int,
    val machineNo: String,
    val machineLocation: String,
    var npmDate: Date?,
    val ticketStatus: String,
    var date: Date?,
    val lastUpdatedBy: String
)