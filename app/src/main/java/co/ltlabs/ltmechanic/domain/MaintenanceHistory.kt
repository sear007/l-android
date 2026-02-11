package co.ltlabs.ltmechanic.domain

data class MaintenanceHistory (
    val ticketNo: String,
    val remarks: String,
    val status: String,
    var date: String,
    val username: String
)