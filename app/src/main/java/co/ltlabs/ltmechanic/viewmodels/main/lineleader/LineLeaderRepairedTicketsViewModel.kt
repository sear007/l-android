package co.ltlabs.ltmechanic.viewmodels.main.lineleader

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.ltlabs.ltmechanic.database.getDatabase
import co.ltlabs.ltmechanic.domain.ClosedTicket
import co.ltlabs.ltmechanic.domain.RepairedTicket
import co.ltlabs.ltmechanic.repository.LtMechDatabaseRepository
import javax.inject.Inject

class LineLeaderRepairedTicketsViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val database = getDatabase(application)
    private val mfgLinesDatabaseRepository = LtMechDatabaseRepository(database)

    var isAlreadyGoToDetail = false
    var isClosedTab = false
    val mfgLinesFromDatabase = mfgLinesDatabaseRepository.mfgLines

    private val _navigateToTicketPreview = MutableLiveData<RepairedTicket>()
    val navigateToTicketPreview: LiveData<RepairedTicket>
        get() = _navigateToTicketPreview

    private val _navigateToClosedTicketPreview = MutableLiveData<ClosedTicket>()
    val navigateToClosedTicketPreview: LiveData<ClosedTicket>
        get() = _navigateToClosedTicketPreview

    fun setNavigateToTicketPreview(repairedTicket: RepairedTicket) {
        _navigateToTicketPreview.value = repairedTicket
    }

    fun navigateToTicketPreviewComplete() {
        _navigateToTicketPreview.value = null
    }

    fun setNavigateToClosedTicketPreview(closedTicket: ClosedTicket) {
        _navigateToClosedTicketPreview.value = closedTicket
    }

    fun navigateToClosedTicketPreviewComplete() {
        _navigateToClosedTicketPreview.value = null
    }
}