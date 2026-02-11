package co.ltlabs.ltmechanic.viewmodels.main.lineleader

import android.app.Application
import androidx.lifecycle.*
import co.ltlabs.ltmechanic.constant.type.TicketType
import co.ltlabs.ltmechanic.database.getDatabase
import co.ltlabs.ltmechanic.domain.ReportedTicket
import co.ltlabs.ltmechanic.repository.LtMechDatabaseRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class LineLeaderReportedTicketsViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val _navigateToTicketPreview = MutableLiveData<ReportedTicket>()
    val navigateToTicketPreview: LiveData<ReportedTicket>
        get() = _navigateToTicketPreview

    private val _reportedTickets = MutableLiveData<List<ReportedTicket>>()
    val reportedTickets: LiveData<List<ReportedTicket>>
        get() = _reportedTickets

    private val database = getDatabase(application)
    private val mfgLinesDatabaseRepository = LtMechDatabaseRepository(database)

    val mfgLinesFromDatabase = mfgLinesDatabaseRepository.mfgLines

    fun setNavigateToTicketPreview(reportedTicket: ReportedTicket) {
        _navigateToTicketPreview.value = reportedTicket
    }

    fun navigateToTicketPreviewComplete() {
        _navigateToTicketPreview.value = null
    }

    fun getReportedOrReopenedTicket(type: TicketType, list: List<ReportedTicket>) {
        viewModelScope.launch {
            val filter = list.filter {
                TicketType.fromCodeToType(it.status) == type
            }
            _reportedTickets.postValue(filter)
        }
    }
}