package co.ltlabs.ltmechanic.viewmodels.main.lineleader

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import co.ltlabs.ltmechanic.database.DatabaseMfgArea
import co.ltlabs.ltmechanic.database.DatabaseMfgLine
import co.ltlabs.ltmechanic.database.getDatabase
import co.ltlabs.ltmechanic.network.TicketStatisticsRequest
import co.ltlabs.ltmechanic.network.main.LineApi
import co.ltlabs.ltmechanic.network.main.TicketApi
import co.ltlabs.ltmechanic.repository.LtMechDatabaseRepository
import co.ltlabs.ltmechanic.util.ApiStatus
import co.ltlabs.ltmechanic.util.AuthUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "LineLeaderHomeViewModel";

class LineLeaderHomeViewModel @Inject constructor(
    private val lineApi: LineApi, private val ticketApi: TicketApi,
    application: Application
) : AndroidViewModel(application) {

    /**
     * This is the job for all coroutines started by this ViewModel.
     *
     * Cancelling this job will cancel all coroutines started by this ViewModel.
     */
    private val viewModelJob = SupervisorJob()

    /**
     * This is the main scope for all coroutines launched by MainViewModel.
     *
     * Since we pass viewModelJob, you can cancel all coroutines launched by uiScope by calling
     * viewModelJob.cancel()
     */
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _eventLineListSearchResultNotFound = MutableLiveData<Boolean>()
    val eventLineListSearchResultNotFound: LiveData<Boolean>
        get() = _eventLineListSearchResultNotFound

    private val database = getDatabase(application)
    private val mfgLinesDatabaseRepository = LtMechDatabaseRepository(database)

    //    val mfgLinesFromDatabase = mfgLinesDatabaseRepository.mfgLinesByUsername(AuthUtil.username)
    val mfgLinesFromDatabase = mfgLinesDatabaseRepository.mfgLines

    var tempEventLinesChanged = false

    var popupFirstOpen = false

    private val _status = MutableLiveData<ApiStatus>()
    val status: LiveData<ApiStatus>
        get() = _status


    private val _reportedTicketsCount = MutableLiveData<Long>()
    val reportedTicketsCount: LiveData<Long>
        get() = _reportedTicketsCount

    private val _coRequestCount = MutableLiveData<Long>()
    val coRequestCount: LiveData<Long>
        get() = _coRequestCount

    private val _reopenedTicketsCount = MutableLiveData<Long>()
    val reopenedTicketsCount: LiveData<Long>
        get() = _reopenedTicketsCount

    private val _inRepairTicketsCount = MutableLiveData<Long>()
    val inRepairTicketsCount: LiveData<Long>
        get() = _inRepairTicketsCount

    private val _repairedTicketsCount = MutableLiveData<Long>()
    val repairedTicketsCount: LiveData<Long>
        get() = _repairedTicketsCount

    private val _machinesTicketsCount = MutableLiveData<List<Long>>()
    val machinesTicketsCount: LiveData<List<Long>>
        get() = _machinesTicketsCount

    init {
        Log.d(TAG, "init: viewmodel is working...")

    }

    fun getMachineTicketCounts(selectedLines: List<String>) {
        viewModelScope.launch {
            val getMachineTicketCountsDeferred = ticketApi.getMachineTicketCountsAsync(
                TicketStatisticsRequest(selectedLines), "Bearer ${AuthUtil.token}"
            )

            try {

                val result = getMachineTicketCountsDeferred.await()

                if (result.success) {
                    val tickets = result.tickets
                    val counts = listOf(tickets.count ?: 0, tickets.totalMachine ?: 0)
                    _machinesTicketsCount.value = counts
                }

            } catch (e: Exception) {
                Log.e(TAG, "getTicketStatistics: ", e)
                _machinesTicketsCount.value = listOf(0, 0)
            }
        }
    }

    fun getTicketStatistics(selectedLines: List<String>) {
        viewModelScope.launch {
            val getTicketStatisticsDeferred = ticketApi.getTicketStatisticsAsync(
                TicketStatisticsRequest(selectedLines)
            )

            try {

                val result = getTicketStatisticsDeferred.await()

                if (result.success) {
                    val tickets = result.tickets
                    _reportedTicketsCount.value = tickets.Reported
                    _reopenedTicketsCount.value = tickets.Reopen
                    _repairedTicketsCount.value = tickets.Repaired
                    _inRepairTicketsCount.value = tickets.InRepair
                    _coRequestCount.value = tickets.CORequest
                }

            } catch (e: Exception) {
                Log.e(TAG, "getTicketStatistics: ", e)
                _reportedTicketsCount.value = 0
                _inRepairTicketsCount.value = 0
                _repairedTicketsCount.value = 0
                _coRequestCount.value = 0
            }
        }
    }

//    fun sendNotificationToMechanics(token: String, topic: String, title: String = "New repair ticket", content: String = "A new repair ticket has been submitted.") {
//
//        val data = FCMData(title, content)
//        val message = FCMMessage(topic, data)
//        val firebaseSendNotificationRequest = FirebaseSendNotificationRequest(message)
//
//        viewModelScope.launch {
//
//            val sendNotificationByTopicDeferred = firebaseCloudMessagingApi.sendNotificationByTopicAsync(token, firebaseSendNotificationRequest)
//
//            try {
//
//                val result = sendNotificationByTopicDeferred.await()
//
//                if (result.name != null) {
//                    Log.d(TAG, "sendNotificationToMechanics: name: ${result.name}")
//                } else {
//                    result.error?.let {
//                        Log.d(TAG, "sendNotificationToMechanics: error: ${result.error}")
//                        Log.d(TAG, "sendNotificationToMechanics: error message: ${result.error.message}")
//                    }
//                }
//
//
//            } catch (t: Throwable) {
//
//                Log.e(TAG, "sendNotificationToMechanics: ", t)
//            }
//        }
//    }

    fun insertToMfgAreasDatabase(mfgAreas: Array<DatabaseMfgArea>) {
        viewModelScope.launch {
            mfgLinesDatabaseRepository.insertMfgArea(mfgAreas)
        }
    }

    fun insertToMfgLineDatabase(mfgLines: Array<DatabaseMfgLine>) {
        viewModelScope.launch {
            mfgLinesDatabaseRepository.insertMfgLines(mfgLines)
        }

    }

    fun setEventLineListSearchResultNotFoundToTrue() {
        _eventLineListSearchResultNotFound.value = true
    }

    fun setEventLineListSearchResultNotFoundToFalse() {
        _eventLineListSearchResultNotFound.value = false
    }

}