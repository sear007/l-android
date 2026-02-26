package co.ltlabs.ltmechanic.util

class MachineUtil {
    companion object {
        var machineNo = ""
        var machineLocation = ""
        var machineArea = ""
        var machineHasOpenTickets = false
        var machineOpenTicketNo = ""
        var machineFound = false
        var mchineNotFound = false
        var machineStatus = ""
        var message = ""

        fun clear() {
            machineNo = ""
            machineLocation = ""
            machineArea = ""
            machineHasOpenTickets = false
            machineOpenTicketNo = ""
        }
    }
}