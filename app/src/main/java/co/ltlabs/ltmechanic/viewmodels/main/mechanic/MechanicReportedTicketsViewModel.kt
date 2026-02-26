package co.ltlabs.ltmechanic.viewmodels.main.mechanic

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import co.ltlabs.ltmechanic.constant.type.TicketType
import co.ltlabs.ltmechanic.database.getDatabase
import co.ltlabs.ltmechanic.domain.ReportedTicket
import co.ltlabs.ltmechanic.network.main.TicketApi
import co.ltlabs.ltmechanic.network.main.dto.asReportedTicketDomainModel
import co.ltlabs.ltmechanic.repository.LtMechDatabaseRepository
import co.ltlabs.ltmechanic.util.ApiStatus
import co.ltlabs.ltmechanic.util.AuthUtil
import co.ltlabs.ltmechanic.util.DateUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ReportedTViewModel";

class MechanicReportedTicketsViewModel @Inject constructor(
    private val ticketApi: TicketApi, application: Application
) : AndroidViewModel(application) {

    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _reportedTickets = MutableLiveData<List<ReportedTicket>>()
    val reportedTickets: LiveData<List<ReportedTicket>>
        get() = _reportedTickets

    private val _status = MutableLiveData<ApiStatus>()
    val status: LiveData<ApiStatus>
        get() = _status

    private val _navigateToTicketPreview = MutableLiveData<ReportedTicket>()
    val navigateToTicketPreview: LiveData<ReportedTicket>
        get() = _navigateToTicketPreview

    private val database = getDatabase(application)
    private val mfgLinesDatabaseRepository = LtMechDatabaseRepository(database)

    val mfgLinesFromDatabase = mfgLinesDatabaseRepository.mfgLines

    fun getReportedOrReopenedTicket(type: TicketType, list: List<ReportedTicket>) {
        viewModelScope.launch {
            val filter = list.filter {
                TicketType.fromCodeToType(it.status) == type
            }
            _reportedTickets.postValue(filter)
        }
    }

    fun getReportedTickets() {

        viewModelScope.launch {

            val getReportedTicketsDeferred = ticketApi.getReportedTicketsAsync()

            _status.value = ApiStatus.LOADING

            try {
                val result = getReportedTicketsDeferred.await()


                if (result.success) {

                    val reportedTicketResult = result.data.asReportedTicketDomainModel()
                    _reportedTickets.value = reportedTicketResult

                    _status.value = ApiStatus.DONE

                } else {
                    _reportedTickets.value = null
                }

            } catch (t: Throwable) {

                Log.e(TAG, "getReportedTickets: ", t)
                _status.value = ApiStatus.ERROR
            }
        }

    }

    fun setNavigateToTicketPreview(reportedTicket: ReportedTicket) {
        _navigateToTicketPreview.value = reportedTicket
    }

    fun navigateToTicketPreviewComplete() {
        _navigateToTicketPreview.value = null
    }

    override fun onCleared() {
        super.onCleared()

        viewModelJob.cancel()
    }
}