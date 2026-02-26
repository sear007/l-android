package co.ltlabs.ltmechanic.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ltlabs.ltmechanic.domain.Ticket
import co.ltlabs.ltmechanic.network.main.TicketApi
import co.ltlabs.ltmechanic.network.main.dto.asTicketDomainModel
import co.ltlabs.ltmechanic.util.AuthUtil
import co.ltlabs.ltmechanic.util.SingleLiveEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class SocketViewModel @Inject constructor(
    private val ticketApi: TicketApi
) : ViewModel() {

    private val _refreshDashboard: MutableSharedFlow<Boolean> = MutableSharedFlow()
    val refreshDashboard: SharedFlow<Boolean> = _refreshDashboard

    private val _socketTicket = SingleLiveEvent<Ticket>()
    val socketTicket: LiveData<Ticket> = _socketTicket

    private val _fcmTicket : MutableSharedFlow<Ticket> = MutableSharedFlow()
    val fcmTicket: SharedFlow<Ticket> = _fcmTicket

    fun refreshDashboard() {
        viewModelScope.launch {
            _refreshDashboard.emit(true)
        }
    }

    fun getTicketDetailsByTicketNo(ticketNo: String, isFCM: Boolean = false) {
        viewModelScope.launch {
            val getTicketDetailsDeferred =
                ticketApi.getTicketDetailsByTicketNoAsync(ticketNo, "Bearer ${AuthUtil.token}")
            val result = getTicketDetailsDeferred.await()

            if (result.success && result.tickets.asTicketDomainModel().isNotEmpty()) {
                if (isFCM) _fcmTicket.emit(result.tickets.asTicketDomainModel()[0])
                 else _socketTicket.value = result.tickets.asTicketDomainModel()[0]
            }
        }
    }
}