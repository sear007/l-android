package co.ltlabs.ltmechanic.viewmodels.main.mechanic

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.ltlabs.ltmechanic.domain.MaintenanceHistory
import co.ltlabs.ltmechanic.domain.Ticket
import co.ltlabs.ltmechanic.network.main.TicketApi
import co.ltlabs.ltmechanic.network.main.dto.asTicketDomainModel
import co.ltlabs.ltmechanic.util.ApiStatus
import co.ltlabs.ltmechanic.util.AuthUtil
import co.ltlabs.ltmechanic.util.DateUtil
import co.ltlabs.ltmechanic.util.TicketUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MaintenanceHistoryVM";

class MaintenanceHistoryViewModel @Inject constructor(
    val ticketApi: TicketApi
) : ViewModel() {

    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _navigateToCheckList = MutableLiveData<Ticket>()
    val navigateToCheckList: LiveData<Ticket>
        get() = _navigateToCheckList

    private val _maintenanceHistoryTickets = MutableLiveData<List<Ticket>>()
    val maintenanceHistoryTickets: LiveData<List<Ticket>>
        get() = _maintenanceHistoryTickets

    private val _status = MutableLiveData<ApiStatus>()
    val status: LiveData<ApiStatus>
        get() = _status

    val maintenanceHistory = listOf(
        MaintenanceHistory (
            "23589797987",
            "Best solution of the problem that has occurred and fixed the machine very well.",
            "REPAIRED",
            "14/01/2020",
            "John Doe"
        ),
        MaintenanceHistory (
            "23589797988",
            "Best solution of the problem that has occurred and fixed the machine very well.",
            "REPAIRED",
            "14/01/2020",
            "John Doe"
        )
    )

    fun getMaintenanceHistory(machineId: Long) {

        Log.d(TAG, "getMaintenanceHistory: machineId: $machineId")
        Log.d(TAG, "getMaintenanceHistory: maintenanceTicketStatus: ${TicketUtil.maintenanceTicketStatus}")

        viewModelScope.launch {

            val getMaintenanceHistoryDeferred = ticketApi.getMaintenanceHistoryAsync(
                machineId = machineId,
                status = TicketUtil.maintenanceTicketStatus,
                accessToken = "Bearer ${AuthUtil.token}"
            )

            try {

                _status.value = ApiStatus.LOADING

                val result = getMaintenanceHistoryDeferred.await()

                if (result.success) {
                    val maintenanceHistoryTicketsData = result.tickets.asTicketDomainModel()

                    _maintenanceHistoryTickets.value = maintenanceHistoryTicketsData
                } else {
                    _maintenanceHistoryTickets.value = null
                }

                _status.value = ApiStatus.DONE

            } catch (t: Throwable) {

                _status.value = ApiStatus.ERROR

                _maintenanceHistoryTickets.value = null

                Log.e(TAG, "getMaintenanceHistory: ", t)

            }

        }

    }

    fun maintenanceHistoryComplete() {
        _maintenanceHistoryTickets.value = null
    }

    fun setNavigateToChecklist(maintenanceHistory: Ticket) {
        _navigateToCheckList.value = maintenanceHistory
    }

    fun navigateToChecklistComplete() {
        _navigateToCheckList.value = null
    }
}