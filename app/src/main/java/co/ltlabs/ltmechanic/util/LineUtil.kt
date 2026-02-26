package co.ltlabs.ltmechanic.util

import co.ltlabs.ltmechanic.domain.MfgLine
import co.ltlabs.ltmechanic.network.ClearLineErrorMachine

class LineUtil {
    companion object {
        var selectedMfgLine = ""
        var selectedMfgLineId = 0L

        var uncheckedLines = mutableListOf<MfgLine>()

        fun clearSelectedLine() {
            selectedMfgLine = ""
            selectedMfgLineId = 0
        }

        var lastSelectedStation = ""
        var finishedSetupLine = false

        var fromLinePlaces = false

        var machinesHasTickets = mutableListOf<ClearLineErrorMachine>()
    }
}