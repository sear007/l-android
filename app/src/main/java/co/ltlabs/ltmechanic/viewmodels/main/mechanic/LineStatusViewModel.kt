package co.ltlabs.ltmechanic.viewmodels.main.mechanic

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import co.ltlabs.ltmechanic.database.getDatabase
import co.ltlabs.ltmechanic.domain.MfgLine
import co.ltlabs.ltmechanic.repository.LtMechDatabaseRepository
import javax.inject.Inject

class LineStatusViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {

    private val database = getDatabase(application)
    private val mfgLinesDatabaseRepository = LtMechDatabaseRepository(database)

    val mfgLinesFromDatabase = mfgLinesDatabaseRepository.mfgLines

    private val _navigateToStations = MutableLiveData<MfgLine>()
    val navigateToStations: LiveData<MfgLine>
        get() = _navigateToStations


    fun setNavigateToStations(mfgLine: MfgLine) {
        _navigateToStations.value = mfgLine
    }

    fun setNavigateToStationsComplete() {
        _navigateToStations.value = null
    }

}