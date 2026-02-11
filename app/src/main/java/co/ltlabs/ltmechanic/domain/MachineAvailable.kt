package co.ltlabs.ltmechanic.domain

import java.util.*

data class MachineAvailable (
    val machineNo: String,
    var date: Date?,
    val status: String,
    val subType: String,
    val username: String,
    val attachment: String
)