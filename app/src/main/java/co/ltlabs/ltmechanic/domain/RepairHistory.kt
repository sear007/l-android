package co.ltlabs.ltmechanic.domain

data class RepairHistory (
    val ticketNo: String,
    val problem: String,
    val solution: String,
    val remarks: String,
    val status: String,
    var date: String,
    val username: String
)