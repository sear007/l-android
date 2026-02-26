package co.ltlabs.ltmechanic.domain

data class TicketChecklist (
    val id: Long,
    val task: String,
    var checked: Boolean = false,
    val subtask: Boolean = false,
    var tag: String = "",
    val identity: String = ""
)

fun List<TicketChecklist>.asMaintenanceChecklistDomainModel(): List<MaintenanceChecklist> {
    return map {
        MaintenanceChecklist(
            it.id,
            it.task,
            it.checked,
            it.subtask,
            it.tag,
            it.identity
        )
    }
}