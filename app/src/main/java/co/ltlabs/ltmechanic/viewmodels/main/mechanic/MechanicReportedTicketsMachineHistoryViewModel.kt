package co.ltlabs.ltmechanic.viewmodels.main.mechanic

import androidx.lifecycle.ViewModel
import co.ltlabs.ltmechanic.domain.MachineHistory
import java.util.*
import javax.inject.Inject

class MechanicReportedTicketsMachineHistoryViewModel @Inject constructor() : ViewModel() {


    val machineHistory = listOf(
        MachineHistory(
            "23589797987",
            "Common Machine problem 123",
            "Solution",
            "Best solution of the problem that has occurred and fixed the machine very well.",
            "REPAIRED",
            Date(),
            "John Doe"
        ),
        MachineHistory(
            "23589797987",
            "Common Machine problem 123",
            "Solution",
            "Best solution of the problem that has occurred and fixed the machine very well.",
            "REPAIRED",
            Date(),
            "John Doe"
        ),
        MachineHistory(
            "23589797987",
            "Common Machine problem 123",
            "Solution",
            "Best solution of the problem that has occurred and fixed the machine very well.",
            "REPAIRED",
            Date(),
            "John Doe"
        ),
        MachineHistory(
            "23589797987",
            "Common Machine problem 123",
            "Solution",
            "Best solution of the problem that has occurred and fixed the machine very well.",
            "REPAIRED",
            Date(),
            "John Doe"
        ),
        MachineHistory(
            "23589797987",
            "Common Machine problem 123",
            "Solution",
            "Best solution of the problem that has occurred and fixed the machine very well.",
            "REPAIRED",
            Date(),
            "John Doe"
        )
    )
}