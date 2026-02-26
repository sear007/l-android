package co.ltlabs.ltmechanic.viewmodels.main.mechanic

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import co.ltlabs.ltmechanic.database.DatabaseMfgArea
import co.ltlabs.ltmechanic.database.DatabaseMfgLine
import co.ltlabs.ltmechanic.database.getDatabase
import co.ltlabs.ltmechanic.domain.MfgLine
import co.ltlabs.ltmechanic.network.Error
import co.ltlabs.ltmechanic.network.TicketStatisticsRequest
import co.ltlabs.ltmechanic.network.main.LineApi
import co.ltlabs.ltmechanic.network.main.TicketApi
import co.ltlabs.ltmechanic.repository.LtMechDatabaseRepository
import co.ltlabs.ltmechanic.util.ApiStatus
import co.ltlabs.ltmechanic.util.AuthUtil
import co.ltlabs.ltmechanic.util.Line
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.Socket
import com.google.gson.Gson
import kotlinx.coroutines.*
import retrofit2.HttpException
import javax.inject.Inject

private const val TAG = "MechanicHomeViewModel";

class MechanicHomeViewModel @Inject constructor(private val lineApi: LineApi,
                                                private val ticketApi: TicketApi,
                                                application: Application)
    : AndroidViewModel(application) {

    private val viewModelJob = SupervisorJob()

    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _selectedLine = MutableLiveData<Line>()
    val selectedLine: LiveData<Line>
        get() = _selectedLine

    private val _mfgLines = MutableLiveData<MutableList<MfgLine>>()
    val mfgLines: LiveData<MutableList<MfgLine>>
        get() = _mfgLines

    private val _status = MutableLiveData<ApiStatus>()
    val status: LiveData<ApiStatus>
        get() = _status

    private val _eventLineListSearchResultNotFound = MutableLiveData<Boolean>()
    val eventLineListSearchResultNotFound: LiveData<Boolean>
        get() = _eventLineListSearchResultNotFound

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

    private val _maintenanceTicketsCount = MutableLiveData<Long>()
    val maintenanceTicketsCount: LiveData<Long>
        get() = _maintenanceTicketsCount

    private val _machinesTicketsCount = MutableLiveData<List<Long>>()
    val machinesTicketsCount: LiveData<List<Long>>
        get() = _machinesTicketsCount

    private val database = getDatabase(application)
    private val mfgLinesDatabaseRepository = LtMechDatabaseRepository(database)

//    val mfgLinesFromDatabase = mfgLinesDatabaseRepository.mfgLinesByUsername(AuthUtil.username)
    val mfgLinesFromDatabase = mfgLinesDatabaseRepository.mfgLines

    private lateinit var socket: Socket



    init {

//        val onAuthenticatedMessage = Emitter.Listener { args ->
////        val data: JSONObject = args[0] as JSONObject
////            socket.on("new_ticket", onNewTicketMessage)
////            socket.on("updated_ticket", onNewTicketMessage)
//            Log.d(TAG, "onAuthenticatedMessage: ${args[0]}")
//
//        }
//
//        try {
////            val opts = IO.Options()
////            opts.reconnection = true
////            opts.forceNew = false
//            socket = IO.socket(SOCKET_IO_URL)
//
//
//            socket.on("new_ticket", object : Emitter.Listener {
//                override fun call(vararg args: Any?) {
//                    Log.d(TAG, "call: new_ticket")
//                }
//
//            }).on(Socket.EVENT_CONNECT) {
//
//                socket.emit("authentication", SocketIOToken(1, "sample_token", "clare"))
//
////            socket.on("updated_ticket", onUpdateTicketMessage)
//
////                socket.on("new_ticket", object : Emitter.Listener {
////                    override fun call(vararg args: Any?) {
////                        Log.d(TAG, "call: new_ticket")
////                    }
////
////                })
////                socket.on("new_ticket", object : Emitter.Listener {
////                    override fun call(vararg args: Any?) {
////                        Log.d(TAG, "call: new_ticket")
////                    }
////
////                })
//
//            }.on("authenticated", onAuthenticatedMessage)
//                .on("new_ticket", object : Emitter.Listener {
//                override fun call(vararg args: Any?) {
//                    Log.d(TAG, "call: new_ticket")
//                }
//
//            }).on(Socket.EVENT_DISCONNECT) {
//                Log.d(TAG, "disconnected: ")
//                socket.connect()
//            }
//
//
//
////            socket
//
//            socket.connect()
//
//        } catch (e: Exception) {
//            Log.e(TAG, "init: ", e)
//        }

    }

    fun getMachineTicketCounts(selectedLines: List<String>) {
        viewModelScope.launch {
            val getMachineTicketCountsDeferred = ticketApi.getMachineTicketCountsAsync(
                TicketStatisticsRequest(selectedLines),
                "Bearer ${AuthUtil.token}")

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
        Log.d(TAG, "getTicketStatistics: selectedLines: $selectedLines")
        Log.d(TAG, "getTicketStatistics: AuthUtil.token: ${AuthUtil.token}")
        viewModelScope.launch {
            val getTicketStatisticsDeferred = ticketApi.getTicketStatisticsAsync(TicketStatisticsRequest(selectedLines))

            try {

                val result = getTicketStatisticsDeferred.await()

                Log.d(TAG, "getTicketStatistics: result: $result")

                if (result.success) {
                    val tickets = result.tickets
                    _reportedTicketsCount.value = tickets.Reported
                    _reopenedTicketsCount.value = tickets.Reopen
                    _repairedTicketsCount.value = tickets.Repaired
                    _inRepairTicketsCount.value = tickets.InRepair
                    _maintenanceTicketsCount.value =
                        tickets.Maintenance
                    _coRequestCount.value = tickets.CORequest
                }

            } catch (t: Throwable) {
                Log.e(TAG, "getTicketStatistics: ", t)

                if (t is HttpException) {

                    try {
                        val error = t.response()?.errorBody()?.string()

                        val gson = Gson()
                        val errorObj: String? = error
                        val errorJson = gson.fromJson(errorObj, Error::class.java)

                        Log.d(TAG, "checkInMachine: error: $error")
                        Log.d(TAG, "checkInMachine: errorObj: $errorJson")
//                    Log.d(TAG, "checkInMachine: errorObj: $errorObj")


                        if (errorJson.errors != null) {
                            Log.d(TAG, "checkInMachine: errorJson.errors[0].id: ${errorJson.errors[0].id}")
//                        if (errorJson.errors[0].id?.contains("machine must be in working condition") == true) {
//                            _machineCheckInStatus.value = MachineCheckinStatus.MACHINE_NOT_WORKING
//                        }  else if (errorJson.errors[0].id?.contains("currently in place") == true) {
//                            _machineCheckInStatus.value = MachineCheckinStatus.MACHINE_CURRENTLY_IN_PLACE
//                        } else if (errorJson.errors[0].id?.contains("must be in Floating area") == true) {
//                            _machineCheckInStatus.value = MachineCheckinStatus.NOT_IN_FLOATING_AREA
//                        } else if (errorJson.errors[0].id?.contains("machine has open tickets") == true) {
//                            _machineCheckInStatus.value = MachineCheckinStatus.HAS_OPEN_TICKETS
//                        }
                        }
                    } catch (e: Exception) {}


                } else {
//                    _machineCheckInStatus.value = null
                }

                _reportedTicketsCount.value = 0
                _inRepairTicketsCount.value = 0
                _repairedTicketsCount.value = 0
                _maintenanceTicketsCount.value = 0
                _coRequestCount.value = 0
            }
        }
    }

    fun insertToMfgLineDatabase(mfgLines: Array<DatabaseMfgLine>) {
        viewModelScope.launch {
            Log.d(TAG, "insertToMfgLineDatabase: mfgLines to save to DB: $mfgLines")
            Log.d(TAG, "insertToMfgLineDatabase: mfgLines to save to DB size: ${mfgLines.size}")
            mfgLinesDatabaseRepository.insertMfgLines(mfgLines)
        }
    }

    fun insertToMfgAreasDatabase(mfgAreas: Array<DatabaseMfgArea>) {
        viewModelScope.launch {
            Log.d(TAG, "insertToMfgLineDatabase: mfgLines to save to DB: $mfgAreas")
            Log.d(TAG, "insertToMfgLineDatabase: mfgLines to save to DB size: ${mfgAreas.size}")
            mfgLinesDatabaseRepository.insertMfgArea(mfgAreas)
        }
    }

    private val onNewTicketMessage = Emitter.Listener { args ->
//        val data: JSONObject = args[0] as JSONObject
        Log.d(TAG, "onNewTicketMessage: ")
        Log.d(TAG, "onNewTicketMessage: ${args[0]}")

    }

    private val onUpdateTicketMessage = Emitter.Listener { args ->
//        val data: JSONObject = args[0] as JSONObject
        Log.d(TAG, "onUpdateTicketMessage: ${args[0]}")

    }

    private val onAuthenticatedMessage = Emitter.Listener { args ->
//        val data: JSONObject = args[0] as JSONObject
//        socket.on("new_ticket", onNewTicketMessage)
        socket.on("new_ticket", object : Emitter.Listener {
            override fun call(vararg args: Any?) {
                Log.d(TAG, "call: new_ticket")
            }

        })
        socket.on("updated_ticket", onNewTicketMessage)
        Log.d(TAG, "onAuthenticatedMessage: ${args[0]}")

    }

    fun setEventLineListSearchResultNotFoundToTrue() {
        _eventLineListSearchResultNotFound.value = true
    }

    fun setEventLineListSearchResultNotFoundToFalse() {
        _eventLineListSearchResultNotFound.value = false
    }

}