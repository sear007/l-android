package co.ltlabs.ltmechanic.viewmodels.shared

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import co.ltlabs.ltmechanic.database.DatabaseSnackBarAction
import co.ltlabs.ltmechanic.database.getDatabase
import co.ltlabs.ltmechanic.domain.Machine
import co.ltlabs.ltmechanic.domain.MachineInStation
import co.ltlabs.ltmechanic.domain.MfgLine
import co.ltlabs.ltmechanic.network.*
import co.ltlabs.ltmechanic.network.main.LineApi
import co.ltlabs.ltmechanic.network.main.MachineApi
import co.ltlabs.ltmechanic.network.main.ReferenceApi
import co.ltlabs.ltmechanic.network.main.dto.asDomainModel
import co.ltlabs.ltmechanic.network.main.dto.asMachineInStationDomainModel
import co.ltlabs.ltmechanic.repository.LtMechDatabaseRepository
import co.ltlabs.ltmechanic.util.*
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.json.JSONObject
import retrofit2.HttpException
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.Exception

private const val TAG = "MachineViewModel";

class MachineViewModel @Inject constructor(private val machineApi: MachineApi, private val lineApi: LineApi, private val referenceApi: ReferenceApi, application: Application) : AndroidViewModel(application) {

    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val database = getDatabase(application)
    private val ltMechDatabaseRepository = LtMechDatabaseRepository(database)

    val snackBarActionsFromDatabase = ltMechDatabaseRepository.snackBarActions

    private val _machine = MutableLiveData<MachineInStation>()
    val machine: LiveData<MachineInStation>
        get() = _machine

    private val _status = MutableLiveData<ApiStatus>()
    val status: LiveData<ApiStatus>
        get() = _status

    private val _attachNFCStatus = MutableLiveData<AttachNFCStatus>()
    val attachNFCStatus: LiveData<AttachNFCStatus>
        get() = _attachNFCStatus

    private val _machineCheckInStatus = MutableLiveData<MachineCheckinStatus>()
    val machineCheckInStatus: LiveData<MachineCheckinStatus>
        get() = _machineCheckInStatus

    private val _machineCheckOutStatus = MutableLiveData<MachineCheckoutStatus>()
    val machineCheckOutStatus: LiveData<MachineCheckoutStatus>
        get() = _machineCheckOutStatus

    private val _machineDetailsByMachineNo = MutableLiveData<Machine>()
    val machineDetailsByMachineNo: LiveData<Machine>
        get() = _machineDetailsByMachineNo

    private val _machineDetailsByRfid = MutableLiveData<Machine>()
    val machineDetailsByRfid: LiveData<Machine>
        get() = _machineDetailsByRfid

    private val _machineStatus = MutableLiveData<MachineStatus>()
    val machineStatus: LiveData<MachineStatus>
        get() = _machineStatus

    private val _nextMachine = MutableLiveData<MachineInStation>()
    val nextMachine: LiveData<MachineInStation>
        get() = _nextMachine

    private val _nextMachineA = MutableLiveData<MachineInStation>()
    val nextMachineA: LiveData<MachineInStation>
        get() = _nextMachineA

    private val _selectedMfgLine = MutableLiveData<MfgLine>()
    val selectedMfgLine: LiveData<MfgLine>
        get() = _selectedMfgLine

    private val _machineInStationCount = MutableLiveData<Int>()
    val machineInStationCount: LiveData<Int>
        get() = _machineInStationCount

    private val _addMachineSuccess = MutableLiveData<Boolean>()
    val addMachineSuccess: LiveData<Boolean>
        get() = _addMachineSuccess

    private val _findMachineStatus = MutableLiveData<FindMachineStatus>()
    val findMachineStatus: LiveData<FindMachineStatus>
        get() = _findMachineStatus

    var currentMfgLineId = 0L

    private val _message: MutableSharedFlow<String> = MutableSharedFlow()
    val message: SharedFlow<String> = _message

    fun getNextMachineByStation(stationNo: String, mfgLineId: Long) {

        viewModelScope.launch {
            val getMachinesByStationNumberDeferred = machineApi.getMachinesByStationNumberAsync(stationNo, mfgLineId, "Bearer ${AuthUtil.token}")
            Log.d(TAG, "getNextMachineByStation: stationNo: $stationNo")
            Log.d(TAG, "getNextMachineByStation: mfgLineId: $mfgLineId")

            try {
                val result = getMachinesByStationNumberDeferred.await()

                Log.d(TAG, "getNextMachineByStation: result: ${result}")

                if (result.machines.isNotEmpty()) {
                    val machine = result.asMachineInStationDomainModel().filter { it.station == stationNo }[0]
                    _nextMachine.value = machine
                } else {
                    _nextMachine.value = MachineInStation(0, "", "", "", "")
                }

            } catch (e: Exception) {
                _nextMachine.value = null
            }
        }
    }

    fun getNextMachineAByStation(stationNo: String, mfgLineId: Long) {

        viewModelScope.launch {
            val getMachinesByStationNumberDeferred = machineApi.getMachinesByStationNumberAsync(stationNo, mfgLineId, "Bearer ${AuthUtil.token}")

            try {
                val result = getMachinesByStationNumberDeferred.await()

                if (result.machines.isNotEmpty()) {
                    val machine = result.asMachineInStationDomainModel().filter { it.station == stationNo }[0]
                    _nextMachineA.value = machine
                } else {
                    _nextMachineA.value = MachineInStation(0, "", "", "", "")
                }

            } catch (e: Exception) {
                _nextMachineA.value = null
            }
        }
    }

    fun getMachineByMachineNo(machineNo: String, userId: Int = 1) {
        Log.d(TAG, "getMachineByMachineNo: $machineNo")
        viewModelScope.launch {
            val getMachineByMachineNoDeferred = machineApi.getMachineByMachineNoAsync(machineNo, userId, "Bearer ${AuthUtil.token}")
            try {
                _status.value = ApiStatus.LOADING
                val result = getMachineByMachineNoDeferred.await()
                Log.d(TAG, "getMachineByMachineNo: result: $result")
                _status.value = ApiStatus.DONE
                if (result.machine.isNotEmpty()) {
                    MachineUtil.machineFound = true

                    _machineStatus.value = MachineStatus.FOUND
                    val machineFound = result.asDomainModel()[0]
                    machineFound.station = if (machineFound.station == "0") "" else machineFound.station
                    machineFound.hasOpenTicket = (result.hasOpenTicket == "true")
                    
                    MachineUtil.machineStatus = machineFound.status
//                    MachineUtil.machineStatus = "INACTIVE"

                    MachineUtil.machineNo = machineFound.machine
                    MachineUtil.machineArea = machineFound.area
                    MachineUtil.machineLocation = if (machineFound.area.toLowerCase().contains("prod")) {
                        "${machineFound.mfgLine} - ${machineFound.station}"
                    } else {
                        machineFound.area
                    }
                    MachineUtil.machineHasOpenTickets = machineFound.hasOpenTicket

                    result.ticketNos?.let {ticketNos ->
                        if (ticketNos.isNotEmpty()) {
                            MachineUtil.machineOpenTicketNo = ticketNos[0].ticketNo
                        }
                    }
                    _machineDetailsByMachineNo.value = machineFound


                } else {
                    MachineUtil.message = result.message ?: ""
                    Log.d(TAG, "getMachineByMachineNo: MachineUtil.message: ${MachineUtil.message}")

                    MachineUtil.machineFound = false
                    _machineStatus.value = MachineStatus.NOT_FOUND
                    _machineDetailsByMachineNo.value = null
                }
            } catch (e: Exception) {
                Log.e(TAG, "getMachineByMachineNo: ", e)
                MachineUtil.machineFound = false
                _status.value = ApiStatus.ERROR
                _machineDetailsByMachineNo.value = null
            }
        }
    }

    fun getMachineByRfid(rfid: String) {
        Log.d(TAG, "getMachineByRfid: rfid: $rfid")
        viewModelScope.launch {
            val getMachineByRfidDeferred = machineApi.getMachineByRfidAsync(rfid)
            try {
                _status.value = ApiStatus.LOADING
                val result = getMachineByRfidDeferred.await()
                _status.value = ApiStatus.DONE
                Log.d(TAG, "getMachineByRfid: result.machine.isNotEmpty(): ${result.machine.isNotEmpty()}")
                if (result.machine.isNotEmpty()) {
                    MachineUtil.machineFound = true
                    _machineStatus.value = MachineStatus.FOUND
                    val machineFound = result.asDomainModel()[0]
                    machineFound.station = if (machineFound.station == "0") "" else machineFound.station
                    machineFound.hasOpenTicket = (result.hasOpenTicket == "true")

                    MachineUtil.machineStatus = machineFound.status
                    MachineUtil.machineNo = machineFound.machine
                    MachineUtil.machineArea = machineFound.area
                    MachineUtil.machineLocation = if (machineFound.area.toLowerCase().contains("prod")) {
                        "${machineFound.mfgLine} - ${machineFound.station}"
                    } else {
                        machineFound.area
                    }
                    MachineUtil.machineHasOpenTickets = machineFound.hasOpenTicket

                    result.ticketNos?.let {ticketNos ->
                        if (ticketNos.isNotEmpty()) {
                            MachineUtil.machineOpenTicketNo = ticketNos[0].ticketNo
                        }
                    }

                    _machineDetailsByRfid.value = machineFound

                } else {
                    MachineUtil.message = result.message ?: ""
                    MachineUtil.machineFound = false
                    _machineStatus.value = MachineStatus.NOT_FOUND
                    _machineDetailsByRfid.value = null
                }
            } catch (e: Exception) {
                MachineUtil.machineFound = false
                Log.e(TAG, "getMachineByRfid: ", e)
                _status.value = ApiStatus.ERROR
                _machineStatus.value = MachineStatus.NOT_FOUND
                _machineDetailsByRfid.value = null
            }
        }
    }

    fun getMachineById(machineId: Int) {
        viewModelScope.launch {
            val getMachineByIdDeferred = machineApi.getMachineByIdAsync(machineId,"Bearer ${AuthUtil.token}")
            try {
                _status.value = ApiStatus.LOADING
                val result = getMachineByIdDeferred.await()
                _status.value = ApiStatus.DONE
                if (result.machine.isNotEmpty()) {

                    _machineStatus.value = MachineStatus.FOUND
                    val machineFound = result.asDomainModel()[0]
                    machineFound.station = if (machineFound.station == "0") "" else machineFound.station
                    machineFound.hasOpenTicket = (result.hasOpenTicket == "true")

                    MachineUtil.machineNo = machineFound.machine
                    MachineUtil.machineArea = machineFound.area
                    MachineUtil.machineLocation = if (machineFound.area.toLowerCase().contains("prod")) {
                        "${machineFound.mfgLine} - ${machineFound.station}"
                    } else {
                        machineFound.area
                    }
                    MachineUtil.machineHasOpenTickets = machineFound.hasOpenTicket

                    result.ticketNos?.let {ticketNos ->
                        if (ticketNos.isNotEmpty()) {
                            MachineUtil.machineOpenTicketNo = ticketNos[0].ticketNo
                        }
                    }
                    _machineDetailsByMachineNo.value = machineFound
                } else {
                    _machineStatus.value = MachineStatus.NOT_FOUND
                    _machineDetailsByMachineNo.value = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _status.value = ApiStatus.ERROR
                _machineStatus.value = MachineStatus.NOT_FOUND
                _machineDetailsByMachineNo.value = null
            }
        }
    }

    fun getMachineBySerialNo(serialNo: String, userId: Int = 1) {
        _status.value = ApiStatus.LOADING
        Log.d(TAG, "getMachineBySerialNo: $serialNo")
        viewModelScope.launch {
            val getMachineByMachineNoDeferred = machineApi.getMachineBySerialNoAsync(serialNo, userId, "Bearer ${AuthUtil.token}")
            try {
                val result = getMachineByMachineNoDeferred.await()
                _status.value = ApiStatus.DONE
                if (result.machine.isNotEmpty()) {
                    _machineStatus.value = MachineStatus.FOUND

                    val machineFound = result.asDomainModel()[0]
                    machineFound.station = if (machineFound.station == "0") "" else machineFound.station

                    _machineDetailsByMachineNo.value = machineFound
                } else {
                    _machineStatus.value = MachineStatus.NOT_FOUND
                    _machineDetailsByMachineNo.value = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _status.value = ApiStatus.ERROR
                _machineDetailsByMachineNo.value = null
            }
        }
    }

    fun replaceMachine(oldMachineId: Long, newMachineId: Long, station: String, mfgLineId: Long, existingMachineInPlaceId: Boolean = false) {

//        val checkOutRequest = MachineCheckInRequest(
//            oldMachineId,
//            "",
//            null,
//            DateTime(DateTimeZone.UTC).toString()
//        )

        Timber.tag("Thearith").d("MachineId: $newMachineId")

        if (MachineUtil.machineStatus.toLowerCase().contains("inactive")) {
            _machineCheckInStatus.value = MachineCheckinStatus.MACHINE_NOT_WORKING
        } else {
            val checkInRequest = MachineCheckInRequest(
                newMachineId,
                station,
                mfgLineId,
                DateTime(DateTimeZone.UTC).toString()
            )

            Log.d(TAG, "replaceMachine: checkInRequest: $checkInRequest")

            viewModelScope.launch {

//            val checkOutMachineDeferred = machineApi.checkInMachineAsync(checkOutRequest)
                val checkInMachineDeferred = lineApi.checkInMachineAsync(checkInRequest, "y", "Bearer ${AuthUtil.token}")

                try {

                    _status.value = ApiStatus.LOADING

//                val checkOutResult = checkOutMachineDeferred.await()
//
//                if (checkOutResult.success) {

                    val checkInResult = checkInMachineDeferred.await()

                    if (checkInResult.success) {

                        Log.d(TAG, "replaceMachine: insertToSnackBarActionDatabase: $SNACK_BAR_ACTION_REPLACE_MACHINE")

                        insertToSnackBarActionDatabase(arrayOf(DatabaseSnackBarAction(1, SNACK_BAR_ACTION_REPLACE_MACHINE,true)))

                        _machineCheckInStatus.value = MachineCheckinStatus.SUCCESS

                    } else {
                        _machineCheckInStatus.value = MachineCheckinStatus.FAILED
                    }

//                } else {
//                    _machineCheckInStatus.value = MachineCheckinStatus.FAILED
//                }

                    _status.value = ApiStatus.DONE

                } catch (t: Throwable) {
                    Log.e(TAG, "replaceMachine: ", t)

                    if (t is HttpException) {

                        val error = t.response()?.errorBody()?.string()

                        try {
                            val gson = Gson()
                            val errorObj: String? = error
                            val errorJson = gson.fromJson(errorObj, Error::class.java)

                            Log.d(TAG, "checkInMachine: error: $error")
                            Log.d(TAG, "checkInMachine: errorObj: $errorJson")
//                    Log.d(TAG, "checkInMachine: errorObj: $errorObj")


                            Log.d(TAG, "checkInMachine: errorJson.errors[0].id: ${errorJson.errors[0].id}")
                            if (errorJson.errors[0].id?.contains("machine must be in working condition") == true ||
                                errorJson.errors[0].id?.contains("machine is already retired") == true) {
                                _machineCheckInStatus.value = MachineCheckinStatus.MACHINE_NOT_WORKING
                            }  else if (errorJson.errors[0].id?.contains("currently in place") == true) {
                                _machineCheckInStatus.value = MachineCheckinStatus.MACHINE_CURRENTLY_IN_PLACE
                            } else if (errorJson.errors[0].id?.contains("must be in Floating area") == true) {
                                _machineCheckInStatus.value = MachineCheckinStatus.NOT_IN_FLOATING_AREA
                            } else if (errorJson.errors[0].id?.contains("machine has open tickets") == true) {
                                _machineCheckInStatus.value = MachineCheckinStatus.HAS_OPEN_TICKETS
                            } else if (errorJson.errors[0].id?.contains("") == true) {
                                _machineCheckInStatus.value = MachineCheckinStatus.USER_ON_FLOATING_AREA_ASSIGNED
                            }
                        } catch (e: Exception) {}

                    } else {
                        _machineCheckInStatus.value = null
                    }

                    _status.value = ApiStatus.ERROR
                }

            }
        }

    }

    fun checkOutMachine(machineId: Long, areaId: Int? = null, keepEmpty: Boolean = false, remove: Boolean = false) {

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
        val date = simpleDateFormat.format(Date())

        val machineCheckOutRequest = MachineLineCheckOutRequest(machineId, date, areaId)

        viewModelScope.launch {
            val checkOutMachineDeferred = lineApi.checkOutMachineAsync(machineCheckOutRequest, "Bearer ${AuthUtil.token}")
            try {
                _status.value = ApiStatus.LOADING

                val result = checkOutMachineDeferred.await()
                _status.value = ApiStatus.DONE
                if (result.success) {

                    if (keepEmpty) {
                        insertToSnackBarActionDatabase(arrayOf(DatabaseSnackBarAction(1, SNACK_BAR_ACTION_KEEP_EMPTY,true)))
                    }

                    if (remove) {
                        insertToSnackBarActionDatabase(arrayOf(DatabaseSnackBarAction(1, SNACK_BAR_ACTION_REMOVE_MACHINE,true)))
                    }

                    _machineCheckOutStatus.value = MachineCheckoutStatus.SUCCESS
                } else {
                    _machineCheckOutStatus.value = MachineCheckoutStatus.FAILED
                }

            } catch (e: Exception) {
                _status.value = ApiStatus.ERROR
                _machineCheckOutStatus.value = null
            }
        }
    }

    // updated to line api
    fun checkInNewMachine(machineCheckInRequest: MachineCheckInRequest, addAction: Boolean = false, insertAction: Boolean = false, status: String = "") {

        Log.d(TAG, "checkInMachine: machineCheckInRequest: $machineCheckInRequest")
        Log.d(TAG, "checkInMachine: MachineUtil.machineStatus: ${MachineUtil.machineStatus}")

        if (MachineUtil.machineStatus.toLowerCase().contains("inactive")) {
            _machineCheckInStatus.value = MachineCheckinStatus.MACHINE_NOT_WORKING
        } else {
            viewModelScope.launch {

                val checkInMachineDeferred = lineApi.checkInNewMachineAsync(machineCheckInRequest, accessToken = "Bearer ${AuthUtil.token}") // migrated from machineApi to lineApi
                try {
                    _status.value = ApiStatus.LOADING

                    val result = checkInMachineDeferred.await()

                    Log.d(TAG, "checkInMachine: result.success: ${result.success}")

                    _status.value = ApiStatus.DONE
                    if (result.success) {

                        if (insertAction) {
                            insertToSnackBarActionDatabase(arrayOf(DatabaseSnackBarAction(1, SNACK_BAR_ACTION_INSERT,true)))
                        }

                        if (addAction) {
                            insertToSnackBarActionDatabase(arrayOf(DatabaseSnackBarAction(1, SNACK_BAR_ACTION_ADD_MACHINE,true)))
                        }

                        _machineCheckInStatus.value = MachineCheckinStatus.SUCCESS
                    } else {
                        _machineCheckInStatus.value = MachineCheckinStatus.FAILED
                    }

                } catch (t: Throwable) {

                    Log.e(TAG, "checkInMachine: ", t)

                    if (t is HttpException) {

                        val error = t.response()?.errorBody()?.string()

                        try {
                            val gson = Gson()
                            val errorObj: String? = error
                            val errorJson = gson.fromJson(errorObj, Error::class.java)

                            Log.d(TAG, "checkInMachine: error: $error")
                            Log.d(TAG, "checkInMachine: errorObj: $errorJson")
//                    Log.d(TAG, "checkInMachine: errorObj: $errorObj")


                            if (errorJson.errors != null) {
                                Log.d(
                                    TAG,
                                    "checkInMachine: errorJson.errors[0].id: ${errorJson.errors[0].id}"
                                )
                                if (errorJson.errors[0].id?.contains("machine must be in working condition") == true ||
                                    errorJson.errors[0].id?.contains("machine is already retired") == true
                                ) {
                                    _machineCheckInStatus.value =
                                        MachineCheckinStatus.MACHINE_NOT_WORKING
                                } else if (errorJson.errors[0].id?.contains("currently in place") == true) {
                                    _machineCheckInStatus.value =
                                        MachineCheckinStatus.MACHINE_CURRENTLY_IN_PLACE
                                } else if (errorJson.errors[0].id?.contains("must be in Floating area") == true) {
                                    _machineCheckInStatus.value =
                                        MachineCheckinStatus.NOT_IN_FLOATING_AREA
                                } else if (errorJson.errors[0].id?.contains("machine has open tickets") == true) {
                                    _machineCheckInStatus.value =
                                        MachineCheckinStatus.HAS_OPEN_TICKETS
                                }
                            }
                        } catch (e: Exception) {
                            if (error != null && error.isNotBlank()) {
                                val json = JSONObject(error)
                                _message.emit(json.getString("error"))
                            }
                        }

                    } else {
                        _machineCheckInStatus.value = null
                    }

                    _status.value = ApiStatus.ERROR

                }
            }
        }
    }

    // updated to line api
    fun checkInMachine(machineCheckInRequest: MachineCheckInRequest, addAction: Boolean = false, insertAction: Boolean = false, status: String = "") {

        Log.d(TAG, "checkInMachine: machineCheckInRequest: $machineCheckInRequest")
        Log.d(TAG, "checkInMachine: MachineUtil.machineStatus: ${MachineUtil.machineStatus}")
        
        if (MachineUtil.machineStatus.toLowerCase().contains("inactive")) {
            _machineCheckInStatus.value = MachineCheckinStatus.MACHINE_NOT_WORKING
        } else {
            viewModelScope.launch {

                val checkInMachineDeferred = lineApi.checkInMachineAsync(machineCheckInRequest, accessToken = "Bearer ${AuthUtil.token}") // migrated from machineApi to lineApi
                try {
                    _status.value = ApiStatus.LOADING

                    val result = checkInMachineDeferred.await()

                    Log.d(TAG, "checkInMachine: result.success: ${result.success}")

                    _status.value = ApiStatus.DONE
                    if (result.success) {

//                    if (existingMachineInPlace.isNotBlank()) {
//                        checkOutMachine(existingMachineInPlaceId)
//                    }

                        if (insertAction) {
                            insertToSnackBarActionDatabase(arrayOf(DatabaseSnackBarAction(1, SNACK_BAR_ACTION_INSERT,true)))
                        }

                        if (addAction) {
                            insertToSnackBarActionDatabase(arrayOf(DatabaseSnackBarAction(1, SNACK_BAR_ACTION_ADD_MACHINE,true)))
                        }

                        _machineCheckInStatus.value = MachineCheckinStatus.SUCCESS
                    } else {
                        _machineCheckInStatus.value = MachineCheckinStatus.FAILED
                    }

                } catch (t: Throwable) {

                    Log.e(TAG, "checkInMachine: ", t)

                    if (t is HttpException) {

                        val error = t.response()?.errorBody()?.string()

                        try {
                            val gson = Gson()
                            val errorObj: String? = error
                            val errorJson = gson.fromJson(errorObj, Error::class.java)

                            Log.d(TAG, "checkInMachine: error: $error")
                            Log.d(TAG, "checkInMachine: errorObj: $errorJson")
//                    Log.d(TAG, "checkInMachine: errorObj: $errorObj")


                            if (errorJson.errors != null) {
                                Log.d(
                                    TAG,
                                    "checkInMachine: errorJson.errors[0].id: ${errorJson.errors[0].id}"
                                )
                                if (errorJson.errors[0].id?.contains("machine must be in working condition") == true ||
                                    errorJson.errors[0].id?.contains("machine is already retired") == true
                                ) {
                                    _machineCheckInStatus.value =
                                        MachineCheckinStatus.MACHINE_NOT_WORKING
                                } else if (errorJson.errors[0].id?.contains("currently in place") == true) {
                                    _machineCheckInStatus.value =
                                        MachineCheckinStatus.MACHINE_CURRENTLY_IN_PLACE
                                } else if (errorJson.errors[0].id?.contains("must be in Floating area") == true) {
                                    _machineCheckInStatus.value =
                                        MachineCheckinStatus.NOT_IN_FLOATING_AREA
                                } else if (errorJson.errors[0].id?.contains("machine has open tickets") == true) {
                                    _machineCheckInStatus.value =
                                        MachineCheckinStatus.HAS_OPEN_TICKETS
                                }
                            }
                        } catch (e: Exception) {
                            if (error != null && error.isNotBlank()) {
                                val json = JSONObject(error)
                                _message.emit(json.getString("error"))
                            }
                        }

                    } else {
                        _machineCheckInStatus.value = null
                    }

                    _status.value = ApiStatus.ERROR

                }
            }
        }
    }

    init {
        Log.d(TAG, "init: viewmodel is working")
    }

    fun getMachineByStation(stationNo: String, mfgLineId: Long) {
        _status.value = ApiStatus.LOADING

//        Log.d(TAG, "getMachineByStation: stationNo: $stationNo, mfgLineId: $mfgLineId, userId: $userId")

        viewModelScope.launch {
            val getMachinesByStationNumberDeferred = machineApi.getMachinesByStationNumberAsync(stationNo, mfgLineId, "Bearer ${AuthUtil.token}")

            try {
                val result = getMachinesByStationNumberDeferred.await()
                _status.value = ApiStatus.DONE

//                Log.d(TAG, "getMachineByStation: result.machines size: ${result.machines.size}")

                if (result.machines.isNotEmpty()) {
                    _machineInStationCount.value = result.machines.size
                    val machine = result.asMachineInStationDomainModel().filter { it.station == stationNo }[0]
                    _machine.value = machine
                } else {
                    _machineInStationCount.value = 0
                    _machine.value = MachineInStation(0, "", "", "", "")
                }

            } catch (e: Exception) {
                _status.value = ApiStatus.ERROR
                _machine.value = null
            }
        }
    }

    fun attachMachineNFC(machineId: Long, rfid: String) {

        viewModelScope.launch {

            val attachMachineRFIDDeferred = machineApi.attachMachineRFIDAsync(
                machineId,
                AttachMachineNFCRequest(rfid),
                "Bearer ${AuthUtil.token}"
            )

            try {
                _status.value = ApiStatus.LOADING

                val result = attachMachineRFIDDeferred.await()

                if (result.success) {
                    _attachNFCStatus.value = AttachNFCStatus.SUCCESS
                } else {
                    _attachNFCStatus.value = AttachNFCStatus.FAILED
                }

                _status.value = ApiStatus.DONE

            } catch (t: Throwable) {
                Log.e(TAG, "attachMachineNFC: ", t)

                _status.value = ApiStatus.ERROR

                if (t is HttpException) {

                    try {
                        val error = t.response()?.errorBody()?.string()

                        val gson = Gson()
                        val errorObj: String? = error
                        val errorJson = gson.fromJson(errorObj, Error3::class.java)

                        Log.d(TAG, "checkInMachine: error: $error")
                        Log.d(TAG, "checkInMachine: errorObj: $errorJson")
//                    Log.d(TAG, "checkInMachine: errorObj: $errorObj")


                        if (errorJson.errors != null) {
                            if (errorJson.errors[0].rfid?.toLowerCase().contains("duplicate entry")) {
                                _attachNFCStatus.value = AttachNFCStatus.DUPLICATE
                            } else if (errorJson.errors[0].rfid?.toLowerCase().contains("machine has existing nfc attachment")) {
                                _attachNFCStatus.value = AttachNFCStatus.ALREADY_ATTACHED
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "attachMachineNFC: ", e)
                    }


                } else {
                    _attachNFCStatus.value = AttachNFCStatus.FAILED
                }
            }

        }

    }

    private val _machinesInStation = MutableLiveData<MutableList<MachineInStation>>()
    val machinesInStation: LiveData<MutableList<MachineInStation>>
        get() = _machinesInStation

    fun getMachinesInStation(mfgLineId: Long, userId: Int = 1) {
        Log.d(TAG, "getMachinesInStation: mfgLineId: $mfgLineId")
        viewModelScope.launch {
            val getMachinesByLinePlacementDeferred = machineApi.getMachinesByLinePlacementAsync(mfgLineId, userId, "Bearer ${AuthUtil.token}")
            try {
                _status.value = ApiStatus.LOADING
                val result = getMachinesByLinePlacementDeferred.await()
                _status.value = ApiStatus.DONE

                Log.d(TAG, "getMachinesInStation: result.machine size: ${result.machine.size}")

                if (result.machine.isNotEmpty()) {

                    _machinesInStation.value = result.asMachineInStationDomainModel().toMutableList()

                } else {
                    _machinesInStation.value = null
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _status.value = ApiStatus.ERROR
            }
        }
    }

    private fun insertToSnackBarActionDatabase(snackBarActions: Array<DatabaseSnackBarAction>) {
        viewModelScope.launch {
            ltMechDatabaseRepository.insertSnackbarActions(snackBarActions)
        }
    }

    fun finishInsertToSnackBarActionDatabase() {
        viewModelScope.launch {
            ltMechDatabaseRepository.insertSnackbarActions((arrayOf(DatabaseSnackBarAction(1, SNACK_BAR_ACTION_NONE, false))))
        }
    }

    fun machineDetailsByMachineNoComplete() {
        _machineDetailsByMachineNo.value = null
    }

    fun machineDetailsByRfidComplete() {
        _machineDetailsByRfid.value = null
    }

    fun setSelectedMfgLine(mfgLine: MfgLine) {
        _selectedMfgLine.value = mfgLine
    }

    fun setMachineCheckInStatusComplete() {
        _machineCheckInStatus.value = null
    }

    fun setMachineCheckOutStatusComplete() {
        _machineCheckOutStatus.value = null
    }

    fun setAddMachineSuccessComplete() {
        _addMachineSuccess.value = null
    }

    fun machineStatusComplete() {
        _machineStatus.value = null
    }

    fun selectedMfgLineComplete() {
        _selectedMfgLine.value = null
    }

    override fun onCleared() {
        super.onCleared()

        viewModelJob.cancel()
    }
}