package co.ltlabs.ltmechanic.viewmodels.shared

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import co.ltlabs.ltmechanic.database.getDatabase
import co.ltlabs.ltmechanic.repository.LtMechDatabaseRepository
import javax.inject.Inject

private const val TAG = "QueryMachineViewModel";

class QueryMachineViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val database = getDatabase(application)
    private val ltMechDatabaseRepository = LtMechDatabaseRepository(database)

    val mfgLinesFromDatabase = ltMechDatabaseRepository.mfgLines
}