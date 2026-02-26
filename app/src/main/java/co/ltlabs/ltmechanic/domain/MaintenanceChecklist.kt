package co.ltlabs.ltmechanic.domain

data class MaintenanceChecklist (
    val id: Long,
    val task: String,
    var checked: Boolean = false,
    val subtask: Boolean = false,
    var tag: String = "",
    val identity: String = ""
)