package co.ltlabs.ltmechanic.viewmodels.main.mechanic

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import co.ltlabs.ltmechanic.database.DatabaseMfgLine
import co.ltlabs.ltmechanic.database.getDatabase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "StationDetailsViewModel";

class LineStatusStationDetailsViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val database = getDatabase(application)

    private val _line: MutableSharedFlow<List<DatabaseMfgLine>> = MutableSharedFlow()
    val line: SharedFlow<List<DatabaseMfgLine>> = _line

    fun getLine(id: Long) {
        viewModelScope.launch {
            val result = database.mfgLineDao.getMfgLinesById(id)
            _line.emit(result)
        }
    }

}