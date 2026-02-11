package co.ltlabs.ltmechanic.constant

sealed class TicketType(val title: String, val code: String) {

    companion object {
        const val REPORTED = "REPORTED"
        const val IN_REPAIR = "IN-REPAIR"
        const val REPAIRED = "REPAIRED"
        const val REOPEN = "RE-OPENED"
        const val CANCELLED = "CANCELLED"
        const val CLOSE = "CLOSED"
        const val CO_NEW = "NEW-CO"
        const val CO_IN_PROGRESS = "IN_PROGRESS-CO"
        const val CO_READY = "READY-CO"
        const val CO_CLOSED = "CLOSED-CO"

        fun fromCodeToType(code: String?): TicketType {
            return when (code) {
                REPORTED -> Reported
                IN_REPAIR -> InRepair
                REPAIRED -> Repaired
                REOPEN -> Reopen
                CLOSE -> Close
                CO_NEW -> CONew
                CO_IN_PROGRESS -> COInProgress
                CO_READY -> COReady
                CO_CLOSED -> COClosed
                else -> Cancelled
            }
        }
    }

    object CONew : TicketType("New Changeover Request Ticket is created", CO_NEW)
    object COInProgress : TicketType("Changeover is on PROGRESS", CO_IN_PROGRESS)
    object COReady : TicketType("Changeover is READY", CO_READY)
    object COClosed : TicketType("Changeover is CLOSED", CO_CLOSED)
    object Reported : TicketType("New Reported Ticket", REPORTED)
    object InRepair : TicketType("New In-repair Ticket", IN_REPAIR)
    object Repaired : TicketType("New Repaired Ticket", REPAIRED)
    object Reopen : TicketType("New Re-opened Ticket", REOPEN)
    object Close : TicketType("New Closed Ticket", CLOSE)
    object Cancelled : TicketType("New Cancelled Ticket", CANCELLED)

}