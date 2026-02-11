package co.ltlabs.ltmechanic.domain

import java.util.*

data class MachineHistory (
    val ticketNo: String,
    val problem: String,
    val solution: String,
    val remarks: String,
    val status: String,
    var date: Date?,
    val username: String
)