package co.ltlabs.ltmechanic.viewmodels.main.lineleader

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ltlabs.ltmechanic.domain.TicketLogs
import co.ltlabs.ltmechanic.network.main.TicketApi
import co.ltlabs.ltmechanic.util.AuthUtil
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class LineLeaderTicketPreviewViewModel @Inject constructor(
    private val ticketApi: TicketApi
) : ViewModel() {

    private val _ticketLogs = MutableLiveData<List<TicketLogs?>>()
    val ticketLogs: LiveData<List<TicketLogs?>>
        get() = _ticketLogs

    fun getTicketLogs(ticketNo: String) {

        viewModelScope.launch {

            val result = ticketApi
                .getTicketDetailsByTicketNoAsync(ticketNo, "Bearer ${AuthUtil.token}")
                .await()

            if (result.success) {
                // add reopen date from ticket detail into logs last index
                if (result.logs?.isNotEmpty() == true) {
                    result.logs[result.logs.size - 1].apply {
                        if (result.tickets.isNotEmpty()) {
                            this.reopenedBy = result.tickets[0].reopenedBy
                            this.reopenedDt = result.tickets[0].reopenedDt
                        }
                    }
                }
                _ticketLogs.value = result.logs
            }
        }
    }

}