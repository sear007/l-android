package co.ltlabs.ltmechanic.viewmodels.main.mechanic

import androidx.lifecycle.ViewModel
import co.ltlabs.ltmechanic.domain.RepairHistory
import javax.inject.Inject

class MaintenanceRepairHistoryViewModel @Inject constructor() : ViewModel() {

    val repairHistory = listOf(
        RepairHistory (
            "23589797987",
            "Common Machine problem 123",
            "Solution",
            "Best solution of the problem that has occurred and fixed the machine very well.",
            "REPAIRED",
            "14/01/2020",
            "John Doe"
        ),
        RepairHistory (
            "23589797988",
            "Common Machine problem 124",
            "Solution",
            "Best solution of the problem that has occurred and fixed the machine very well.",
            "REPAIRED",
            "14/01/2020",
            "John Doe"
        )
    )
}