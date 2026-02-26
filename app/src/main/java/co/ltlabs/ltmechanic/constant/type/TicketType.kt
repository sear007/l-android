package co.ltlabs.ltmechanic.constant.type

sealed class TicketType(val title: String, val code: String) {

    companion object {
        const val REPORTED = "REPORTED"
        const val IN_REPAIR = "IN-REPAIR"
        const val REPAIRED = "REPAIRED"
        const val REOPEN = "RE-OPENED"

        fun fromCodeToType(code: String?): TicketType {
            return when (code) {
                REPORTED -> Reported
                IN_REPAIR -> InRepair
                REPAIRED -> Repaired
                else -> Reopen
            }
        }
    }

    object Reported : TicketType("New Reported Ticket", REPORTED)
    object InRepair : TicketType("New In-repair Ticket", IN_REPAIR)
    object Repaired : TicketType("New Repaired Ticket", REPAIRED)
    object Reopen : TicketType("New Re-opened Ticket", REOPEN)

}
