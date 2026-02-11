package co.ltlabs.ltmechanic.viewmodels.main.mechanic

import android.util.Log
import androidx.lifecycle.ViewModel
import co.ltlabs.ltmechanic.domain.MaintenanceChecklist
import javax.inject.Inject

class MaintenanceChecklistViewmodel @Inject constructor() : ViewModel() {

    val maintenanceChecklist = listOf(
        MaintenanceChecklist (
            0,
            "Thread stand",
            false,
            false,
            "",
            ""
        )
    )

    var checklistsTemp = mutableListOf<MaintenanceChecklist>()

    fun setChecklists(checklists: List<MaintenanceChecklist>) {
        checklistsTemp.addAll(checklists)
    }

    fun updateChecklists(checklists: List<MaintenanceChecklist>) {


        if (checklistsTemp.isNotEmpty()) {
            checklistsTemp = checklists.toMutableList()

        }

    }
}