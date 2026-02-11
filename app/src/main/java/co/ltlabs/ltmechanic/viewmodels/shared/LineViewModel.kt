package co.ltlabs.ltmechanic.viewmodels.shared

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import co.ltlabs.ltmechanic.database.DatabaseSnackBarAction
import co.ltlabs.ltmechanic.database.getDatabase
import co.ltlabs.ltmechanic.domain.MachineAvailable
import co.ltlabs.ltmechanic.domain.MachineLocation
import co.ltlabs.ltmechanic.domain.MfgLine
import co.ltlabs.ltmechanic.network.*
import co.ltlabs.ltmechanic.network.main.LineApi
import co.ltlabs.ltmechanic.network.main.dto.asDomainModel
import co.ltlabs.ltmechanic.network.main.dto.asLocationDomainModel
import co.ltlabs.ltmechanic.network.main.dto.asMachineAvailableDomainModel
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
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

private const val TAG = "LineViewModel";

class LineViewModel @Inject constructor(private val lineApi: LineApi, application: Application) : AndroidViewModel(application) {

    private val viewModelJob = SupervisorJob()

    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)
    private val _message: MutableSharedFlow<String> = MutableSharedFlow()
    val message: SharedFlow<String> = _message

    private val database = getDatabase(application)
    private val ltMechDatabaseRepository = LtMechDatabaseRepository(database)

    val snackBarActionsFromDatabase = ltMechDatabaseRepository.snackBarActions

    val mfgLinesFromDatabase = ltMechDatabaseRepository.mfgLines

    private val _status = MutableLiveData<ApiStatus>()
    val status: LiveData<ApiStatus>
        get() = _status

    private val _sendRequestStatus = MutableLiveData<SendRequestStatus>()
    val sendRequestStatus: LiveData<SendRequestStatus>
        get() = _sendRequestStatus

    private val _mfgLines = MutableLiveData<MutableList<MfgLine>>()
    val mfgLines: LiveData<MutableList<MfgLine>>
        get() = _mfgLines

    private val _mfgLinesAssignedByArea = MutableLiveData<MutableList<MfgLine>>()
    val mfgLinesAssignedByArea: LiveData<MutableList<MfgLine>>
        get() = _mfgLinesAssignedByArea

    private val _noAssignedLines = MutableLiveData<Boolean>()
    val noAssignedLines: LiveData<Boolean>
        get() = _noAssignedLines

    var mfgLinesTemp = mutableListOf<MfgLine>()

    private val _selectedMfgLines = MutableLiveData<List<MfgLine>>()
    val selectedMfgLines: LiveData<List<MfgLine>>
        get() = _selectedMfgLines

    var selectedMfgLinesTemp = mutableListOf<MfgLine>()

    private val _lineAssignStatus = MutableLiveData<LineAssignStatus>()
    val lineAssignStatus: LiveData<LineAssignStatus>
        get() = _lineAssignStatus

    private val _lineAssigned = MutableLiveData<List<MfgLine>>()
    val linesAssigned: LiveData<List<MfgLine>>
        get() = _lineAssigned

    private val _clearLineStatus = MutableLiveData<ClearLineStatus>()
    val clearLineStatus: LiveData<ClearLineStatus>
        get() = _clearLineStatus

    private val _machineInsertStatus = MutableLiveData<MachineInsertStatus>()
    val machineInsertStatus: LiveData<MachineInsertStatus>
        get() = _machineInsertStatus

    private val _machineLocations = MutableLiveData<List<MachineLocation>>()
    val machineLocations: LiveData<List<MachineLocation>>
        get() = _machineLocations

    private val _machinesAvailable = MutableLiveData<List<MachineAvailable>>()
    val machinesAvailable: LiveData<List<MachineAvailable>>
        get() = _machinesAvailable

    private val _machinesOtherBrandAvailable = MutableLiveData<List<MachineAvailable>>()
    val machinesOtherBrandAvailable: LiveData<List<MachineAvailable>>
        get() = _machinesOtherBrandAvailable

    private val _machineMoved = MutableLiveData<Boolean>()
    val machineMoved: LiveData<Boolean>
        get() = _machineMoved

    val assignedMfgLines = mutableListOf<MfgLine>()

    init {

    }

    fun clearLine(mfgLineId: Long) {

        val pattern = "yyyy-MM-dd HH:mm:ss.SSS"
        val simpleDateFormat = SimpleDateFormat(pattern)
        val date = simpleDateFormat.format(Date())

        Log.d(TAG, "clearLine: clearLineRequest: ${ClearLineRequest(mfgLineId, date)}")

        viewModelScope.launch {

            val clearLineDeferred = lineApi.clearLineAsync(ClearLineRequest(mfgLineId, date), "Bearer ${AuthUtil.token}")

            try {

                _status.value = ApiStatus.LOADING

                val result = clearLineDeferred.await()

                _status.value = ApiStatus.DONE

                if (result.success) {
                    _clearLineStatus.value = ClearLineStatus.CLEARED
                } else {
                    _clearLineStatus.value = ClearLineStatus.FAILED
                }

            } catch (t: Throwable) {
                Log.e(TAG, "clearLine: ", t)

                if (t is HttpException) {

                    val error = t.response()?.errorBody()?.string()



                    try {

                        val gson = Gson()
                        val errorObj: String? = error
                        val errorJson = gson.fromJson(errorObj, Error::class.java)

                        Log.d(TAG, "clearLine: error: $error")
                        Log.d(TAG, "clearLine: errorObj: $errorJson")

                        if (errorJson.errors[0].id?.contains("machine must be in working condition") == true) {
                        }  else if (errorJson.errors[0].id?.contains("currently in place") == true) {
                        } else if (errorJson.errors[0].id?.contains("must be in Floating area") == true) {
                        } else if (errorJson.errors[0].id?.contains("machine has open tickets") == true) {
                        } else if (errorJson.errors[0].mfgLineId?.message?.toLowerCase()?.contains("open ticket") == true) {

                            errorJson.errors[0].mfgLineId?.machines?.let {
                                LineUtil.machinesHasTickets.addAll(it)
                            }

                            _clearLineStatus.value = ClearLineStatus.WITH_TICKET
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "clearLine: ", e)
                    }

                } else {
                }

                _status.value = ApiStatus.ERROR
//                _clearLineStatus.value = ClearLineStatus.FAILED

            }

        }

    }

    fun clearLineValidate(mfgLineId: Long) {

        val pattern = "yyyy-MM-dd HH:mm:ss.SSS"
        val simpleDateFormat = SimpleDateFormat(pattern)
        val date = simpleDateFormat.format(Date())

        LineUtil.machinesHasTickets.clear()

        viewModelScope.launch {

            val clearLineValidateDeferred = lineApi.clearLineValidateAsync(ClearLineRequest(mfgLineId, date), "Bearer ${AuthUtil.token}")

            try {

                _status.value = ApiStatus.LOADING

                val result = clearLineValidateDeferred.await()

                _status.value = ApiStatus.DONE

                if (result.success) {
                    _clearLineStatus.value = ClearLineStatus.SUCCESS
                } else {
                    _clearLineStatus.value = ClearLineStatus.FAILED
                }

            } catch (t: Throwable) {
                Log.e(TAG, "clearLine: ", t)

                if (t is HttpException) {

                    val error = t.response()?.errorBody()?.string()

                    val gson = Gson()
                    val errorObj: String? = error
                    val errorJson = gson.fromJson(errorObj, Error::class.java)

                    Log.d(TAG, "clearLine: error: $error")
                    Log.d(TAG, "clearLine: errorObj: $errorJson")

                    try {
                        if (errorJson.errors[0].id?.contains("machine must be in working condition") == true) {
                        }  else if (errorJson.errors[0].id?.contains("currently in place") == true) {
                        } else if (errorJson.errors[0].id?.contains("must be in Floating area") == true) {
                        } else if (errorJson.errors[0].id?.contains("machine has open tickets") == true) {
                        } else if (errorJson.errors[0].mfgLineId?.message?.toLowerCase()?.contains("open ticket") == true) {

                            errorJson.errors[0].mfgLineId?.machines?.let {
                                LineUtil.machinesHasTickets.addAll(it)
                            }

                            _clearLineStatus.value = ClearLineStatus.WITH_TICKET
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "clearLine: ", e)
                    }

                } else {
                }

                _status.value = ApiStatus.ERROR
//                _clearLineStatus.value = ClearLineStatus.FAILED

            }

        }

    }

    fun assignLines(lines: List<LineRequest>) {
        viewModelScope.launch {

            val assignLinesDeferred = lineApi.assignLinesAsync(LineAssignRequest(lines))
            try {
                _status.value = ApiStatus.LOADING

                val result = assignLinesDeferred.await()
                _status.value = ApiStatus.DONE

                if (result.success) {

                    if (result.machine.isNotEmpty()) {
                        _lineAssignStatus.value = LineAssignStatus.SUCCESS
                        _mfgLines.value = result.asDomainModel().toMutableList()
                    } else {
                        _lineAssigned.value = null
                    }

                } else {
                    _lineAssignStatus.value = LineAssignStatus.FAILED
                }
            } catch (e: Exception) {
                _status.value = ApiStatus.ERROR
                _lineAssignStatus.value = LineAssignStatus.FAILED
            }
        }
    }

    fun getUserLinesByAssignedAreas(mfgLines: List<MfgLine>? = null) {

//        Log.d(TAG, "getUserLinesByAssignedAreas: mfgLines: $mfgLines")

        Log.d(TAG, "getUserLinesByAssignedAreas: AuthUtil.token: ${AuthUtil.token}")
//
        viewModelScope.launch {
            val getUserLinesByAssignedAreasDeferred = lineApi.getUserLinesByAssignedAreasAsync(
                "Bearer ${AuthUtil.token}"
            )

            try {
                _status.value = ApiStatus.LOADING
                val result = getUserLinesByAssignedAreasDeferred.await()
                _status.value = ApiStatus.DONE

                if (result.lines.isNotEmpty()) {
                    val linesResult = result.lines.asDomainModel().toMutableList()
                    linesResult.map { mfgLine ->

                        mfgLine.checked = mfgLines?.any { mfgLine.mfgLineId == it.mfgLineId }

                    }
                    Log.d(TAG, "getUserLinesByAssignedAreas: linesResult: $linesResult")

                    _mfgLinesAssignedByArea.value = linesResult
                } else {
                    Log.d(TAG, "getUserLinesByAssignedAreas: ")
                }
            } catch (e: Exception) {

                e.printStackTrace()
                _status.value = ApiStatus.ERROR
            }
        }

    }


    fun getAssignedLinesByArea() {
        viewModelScope.launch {
            val getUserAssignedLinesByAreaDeferred = lineApi.getAssignedLinesByAreasAsync()

            Log.d(TAG, "getAssignedLinesByArea: AuthUtil.token: ${AuthUtil.token}")

            try {
                _status.value = ApiStatus.LOADING
                val result = getUserAssignedLinesByAreaDeferred.await()

                Log.d(TAG, "getAssignedLinesByArea: result.lines: ${result.lines}")

                if (result.lines.isNotEmpty()) {
                    val linesResult = result.lines.asDomainModel().toMutableList()
                    Log.d(TAG, "getAssignedLinesByArea: linesResult: $linesResult")
                    linesResult.mapIndexed {index, mfgLine ->
//                        mfgLine.seq = index
                        mfgLine.checked = true
                    }
                    _mfgLines.value = linesResult
                } else {
                    getUserLinesByAssignedAreas()
                }

                _status.value = ApiStatus.DONE
            } catch (e: Exception) {

                e.printStackTrace()
                _status.value = ApiStatus.ERROR
            }
        }
    }

    fun getAssignedAndUnassignedLinesByArea() {
        viewModelScope.launch {
            val getUserAssignedLinesByAreaDeferred = lineApi.getAssignedLinesByAreasAsync()
            try {
                _status.value = ApiStatus.LOADING
                val result = getUserAssignedLinesByAreaDeferred.await()


                if (result.lines.isNotEmpty()) {
                    val linesResult = result.lines.asDomainModel().toMutableList()
                    linesResult.mapIndexed {index, mfgLine ->
//                        mfgLine.seq = index
                        mfgLine.checked = true
                    }

                    val getUserUnAssignedLinesByAreaDeferred = lineApi.getUserUnAssignedLinesByAreaAsync("Bearer ${AuthUtil.token}")

                    val resultUnassigned = getUserUnAssignedLinesByAreaDeferred.await()

                    Log.d(TAG, "getAssignedAndUnassignedLinesByArea: resultUnassigned: $resultUnassigned")

                    val linesResultUnassigned = resultUnassigned.lines.asDomainModel().toMutableList()
                    linesResultUnassigned.mapIndexed {index, mfgLine ->
                        mfgLine.seq = index
                        mfgLine.checked = false
                    }

                    linesResult.addAll(linesResultUnassigned)

                    _status.value = ApiStatus.DONE

                    _mfgLines.value = linesResult.sortedBy { it.mfgLine }.toMutableList()

                } else {
                    Log.d(TAG, "getAssignedLinesByArea: ")
                    getUnAssignedLinesByArea()
                }
            } catch (e: Exception) {

                e.printStackTrace()
                _status.value = ApiStatus.ERROR
            }
        }
    }

    fun updateMfgLine(mfgLines: List<MfgLine>) {
        _mfgLines.value = mfgLines.toMutableList()
    }

    fun updateMfgLineByUserArea(mfgLines: List<MfgLine>) {
        _mfgLinesAssignedByArea.value = mfgLines.toMutableList()
    }

    fun updateMfgLineTemp(position: Int, mfgLine: MfgLine) {
        if (mfgLinesTemp.isNotEmpty()) {
            mfgLinesTemp.removeAt(position)
            mfgLinesTemp.add(mfgLine)
            mfgLinesTemp.sortBy { it.seq }
        }
    }

    fun resetMfgLine(mfgLines: MutableList<MfgLine>) {
        _mfgLines.value = mfgLines
    }

    private fun getUnAssignedLinesByArea() {
        viewModelScope.launch {
            val getUserUnAssignedLinesByAreaDeferred = lineApi.getUserUnAssignedLinesByAreaAsync("Bearer ${AuthUtil.token}")
            try {
                _status.value = ApiStatus.LOADING
                val result = getUserUnAssignedLinesByAreaDeferred.await()
                _status.value = ApiStatus.DONE

                if (result.lines.isNotEmpty()) {
                    val linesResult = result.lines.asDomainModel().toMutableList()
                    linesResult.mapIndexed {index, mfgLine ->
                        mfgLine.seq = index
                        mfgLine.checked = false
                    }
                    _mfgLines.value = linesResult
//                _mfgLines.value = lineList
                    _noAssignedLines.value = true
                } else {
                    _mfgLines.value = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _status.value = ApiStatus.ERROR
            }
        }
    }

    fun insertBetweenMachines(id: Long? = null, station: String, mfgLineId: Long, keepEmpty: Boolean = false) {

        if (MachineUtil.machineStatus.toLowerCase().contains("inactive")) {
            _machineInsertStatus.value = MachineInsertStatus.MACHINE_NOT_WORKING
        } else {
            val machineInsertRequest = MachineInsertRequest(id, station, mfgLineId, DateTime(
                DateTimeZone.UTC).toString())

            Log.d(TAG, "insertBetweenMachines: machineInsertRequest: $machineInsertRequest")
            Log.d(TAG, "insertBetweenMachines: MachineUtil.machineStatus: ${MachineUtil.machineStatus}")

            viewModelScope.launch {

                val insertMachineBetweenDeferred = lineApi.insertMachineBetweenAsync(machineInsertRequest, "Bearer ${AuthUtil.token}")

                try {
                    _status.value = ApiStatus.LOADING
                    val result = insertMachineBetweenDeferred.await()

                    _status.value = ApiStatus.DONE
                    if (result.success) {

                        if (keepEmpty) {
                            insertToSnackBarActionDatabase(arrayOf(DatabaseSnackBarAction(1, SNACK_BAR_ACTION_KEEP_EMPTY_INSERT, true)))
                        } else {
                            insertToSnackBarActionDatabase(arrayOf(DatabaseSnackBarAction(1, SNACK_BAR_ACTION_INSERT, true)))
                        }

                        _machineInsertStatus.value = MachineInsertStatus.SUCCESS
                    } else {
                        _machineInsertStatus.value = MachineInsertStatus.FAILED
                    }
                } catch (t: Throwable) {

                    Log.e(TAG, "insertBetweenMachines: ", t)

                    if (t is HttpException) {

                        val error = t.response()?.errorBody()?.string()


//                    Log.d(TAG, "checkInMachine: errorObj: $errorObj")


//                    Log.d(TAG, "checkInMachine: errorJson.errors: ${errorJson.errors}")
                        try {

                            val gson = Gson()
                            val errorObj: String? = error
                            val errorJson = gson.fromJson(errorObj, Error::class.java)

                            Log.d(TAG, "checkInMachine: error: $error")
                            Log.d(TAG, "checkInMachine: errorObj: $errorJson")

                            if (errorJson != null && errorJson.errors != null) {
                                if (errorJson.errors.isNotEmpty()) {
                                    if (errorJson.errors[0].id?.contains("machine must be in working condition") == true ||
                                        errorJson.errors[0].id?.contains("machine is already retired") == true) {
                                        _machineInsertStatus.value = MachineInsertStatus.MACHINE_NOT_WORKING
                                    }  else if (errorJson.errors[0].id?.contains("currently in place") == true) {
                                        _machineInsertStatus.value = MachineInsertStatus.MACHINE_CURRENTLY_IN_PLACE
                                    } else if (errorJson.errors[0].id?.contains("must be in Floating area") == true) {
                                        _machineInsertStatus.value = MachineInsertStatus.NOT_IN_FLOATING_AREA
                                    } else if (errorJson.errors[0].id?.contains("machine has open tickets") == true) {
                                        _machineInsertStatus.value = MachineInsertStatus.HAS_OPEN_TICKETS
                                    }
                                }
                            }
                        } catch (e: Exception) {}

                    } else {
                        _machineInsertStatus.value = null
                    }

//                _machineInsertStatus.value = MachineInsertStatus.FAILED
                    _status.value = ApiStatus.ERROR
                }
            }
        }
    }

    fun getStorageAreasBySubType(macSubTypeId: Long) {

        viewModelScope.launch {

            val getStorageAreasBySubTypeDeferred = lineApi.getStorageAreasBySubTypeAsync(macSubTypeId, "Bearer ${AuthUtil.token}")

            _status.value = ApiStatus.LOADING

            try {

                val result = getStorageAreasBySubTypeDeferred.await()

                if (result.success) {
                    _machineLocations.value = result.data.asLocationDomainModel()
                } else {
                    _machineLocations.value = null
                }

                _status.value = ApiStatus.DONE

            } catch (t: Throwable) {
                Log.e(TAG, "getStorageAreasBySubType: ", t)

                _status.value = ApiStatus.ERROR
            }

        }

    }

    fun getMachinesAvailableByArea(areaId: Long, macSubTypeId: Long, brandId: Long) {

        viewModelScope.launch {

            val getAlternativeMachinesByAreaDeferred = lineApi.getAlternativeMachinesByAreaAsync(areaId, macSubTypeId, brandId.toString(),
                "Bearer ${AuthUtil.token}")

            _status.value = ApiStatus.LOADING

            try {

                val result = getAlternativeMachinesByAreaDeferred.await()

                if (result.success) {

                    val machinesAvailableResult = result.data.asMachineAvailableDomainModel()

                    _machinesAvailable.value = machinesAvailableResult

                } else {
                    _machinesAvailable.value = null
                }

                _status.value = ApiStatus.DONE

            } catch (t: Throwable) {

                Log.e(TAG, "getMachinesAvailableByArea: ", t)

                _machinesAvailable.value = null
                _status.value = ApiStatus.ERROR
            }


        }

    }

    fun getMachinesOtherBrandAvailableByArea(areaId: Long, macSubTypeId: Long, brand: String) {

        viewModelScope.launch {

            val getAlternativeMachinesByAreaDeferred = lineApi.getAlternativeMachinesByAreaAsync(areaId, macSubTypeId, "all",
                "Bearer ${AuthUtil.token}")

            _status.value = ApiStatus.LOADING

            try {

                val result = getAlternativeMachinesByAreaDeferred.await()

                if (result.success) {

                    val machinesAvailableResult = result.data.asMachineAvailableDomainModel()
                        .filter { it.username != brand}

                    _machinesOtherBrandAvailable.value = machinesAvailableResult

                } else {
                    _machinesOtherBrandAvailable.value = null
                }

                _status.value = ApiStatus.DONE

            } catch (t: Throwable) {

                Log.e(TAG, "getMachinesAvailableByArea: ", t)

                _machinesOtherBrandAvailable.value = null
                _status.value = ApiStatus.ERROR
            }


        }

    }

    fun moveMachine(machineId: Long, areaId: Long, buildingId: Int) {

        viewModelScope.launch {

            val moveMachineDeferred = lineApi.moveMachineAsync(
                "Bearer ${AuthUtil.token}",
                MoveMachineRequest(machineId, areaId, buildingId.toLong()))

            try {

                _status.value = ApiStatus.LOADING

                val result = moveMachineDeferred.await()

                Log.d(TAG, "moveMachine: result: $result")

                if (result.result.affectedRows > 0) {
                    insertToSnackBarActionDatabase(arrayOf(DatabaseSnackBarAction(1, SNACK_BAR_ACTION_MOVE_MACHINE, true)))
                }

                _machineMoved.value = result.result.affectedRows > 0

                _status.value = ApiStatus.DONE

            } catch (t: Throwable) {
                _status.value = ApiStatus.ERROR
                if (t is HttpException) {

                    val error = t.response()?.errorBody()?.string()

                    if (error != null && error.isNotBlank()) {
                        val json = JSONObject(error)
                        _message.emit(json.getString("error"))
                    }
                }
            }

        }
    }

    fun sendRequest(mfgLineId: Long, machineId: Long, request: String) {

        Log.d(TAG, "sendRequest: ${SendRequestRequest(
            mfgLineId,
            machineId,
            request
        )}")

        viewModelScope.launch {

            val sendRequestDeferred = lineApi.sendRequestAsync(
                SendRequestRequest(
                    mfgLineId,
                    machineId,
                    request
                ),
                "Bearer ${AuthUtil.token}"
            )

            try {

                _status.value = ApiStatus.LOADING

                val result = sendRequestDeferred.await()

                if (result.success) {
                    _sendRequestStatus.value = SendRequestStatus.SUCCESS
                } else {
                    _sendRequestStatus.value = SendRequestStatus.FAILED
                }

                _status.value = ApiStatus.DONE

            } catch (e: Exception) {
                Log.e(TAG, "sendRequest: ", e)
                _status.value = ApiStatus.ERROR
                _sendRequestStatus.value = SendRequestStatus.FAILED
            }

        }

    }

    fun machinesOtherBrandAvailableComplete() {
        _machinesOtherBrandAvailable.value = null
    }

    fun machinesAvailableComplete() {
        _machinesAvailable.value = null
    }

    fun insertBetweenMachinesComplete() {
        _machineInsertStatus.value = null
    }

    fun setSelectedMfgLines(mfgLines: List<MfgLine>) {
        Log.d(TAG, "setSelectedMfgLines: mfgLines size: ${mfgLines.size}")
        _selectedMfgLines.value = mfgLines
    }

    fun setClearLineStatusComplete() {
        _clearLineStatus.value = null
    }

    override fun onCleared() {
        super.onCleared()

        viewModelJob.cancel()
    }

    fun machineLocationsComplete() {
        _machineLocations.value = null
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

    fun sendRequestStatusComplete() {
        _sendRequestStatus.value = null
    }
}