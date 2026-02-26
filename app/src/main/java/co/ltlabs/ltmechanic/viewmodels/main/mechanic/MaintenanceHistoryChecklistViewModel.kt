package co.ltlabs.ltmechanic.viewmodels.main.mechanic

import androidx.lifecycle.ViewModel
import co.ltlabs.ltmechanic.domain.MaintenanceHistoryChecklist
import javax.inject.Inject

class MaintenanceHistoryChecklistViewModel @Inject constructor() : ViewModel() {

    val maintenanceChecklist = listOf(
        MaintenanceHistoryChecklist (
            0,
            "Thread stand",
            false,
            false,
            "",
            ""
        ),
        MaintenanceHistoryChecklist (
            0,
            "Thread stand 2",
            true,
            false,
            "",
            ""
        )
    )

    var checklistsTemp = mutableListOf<MaintenanceHistoryChecklist>()

    fun setChecklists(checklists: List<MaintenanceHistoryChecklist>) {
        checklistsTemp.addAll(checklists)
    }

    fun updateChecklists(checklists: List<MaintenanceHistoryChecklist>) {


        if (checklistsTemp.isNotEmpty()) {
            checklistsTemp = checklists.toMutableList()

        }

    }
}