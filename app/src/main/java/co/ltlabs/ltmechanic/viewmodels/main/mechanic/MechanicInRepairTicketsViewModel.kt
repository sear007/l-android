package co.ltlabs.ltmechanic.viewmodels.main.mechanic

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.ltlabs.ltmechanic.database.getDatabase
import co.ltlabs.ltmechanic.domain.InRepairTicket
import co.ltlabs.ltmechanic.domain.RepairedTicket
import co.ltlabs.ltmechanic.repository.LtMechDatabaseRepository
import javax.inject.Inject

class MechanicInRepairTicketsViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {

    private val database = getDatabase(application)
    private val mfgLinesDatabaseRepository = LtMechDatabaseRepository(database)

    val mfgLinesFromDatabase = mfgLinesDatabaseRepository.mfgLines

    private val _navigateToTicketPreview = MutableLiveData<InRepairTicket>()
    val navigateToTicketPreview: LiveData<InRepairTicket>
        get() = _navigateToTicketPreview

    fun setNavigateToTicketPreview(inRepairTicket: InRepairTicket) {
        _navigateToTicketPreview.value = inRepairTicket
    }

    fun navigateToTicketPreviewComplete() {
        _navigateToTicketPreview.value = null
    }
}