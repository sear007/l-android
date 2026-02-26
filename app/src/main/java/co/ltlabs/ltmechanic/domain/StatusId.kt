package co.ltlabs.ltmechanic.domain

data class StatusId (
    val statusId: Long,
    var type: String = "",
    var module: String = ""
)