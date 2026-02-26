package co.ltlabs.ltmechanic.viewmodels.shared

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import co.ltlabs.ltmechanic.database.DatabaseSnackBarAction
import co.ltlabs.ltmechanic.database.getDatabase
import co.ltlabs.ltmechanic.domain.*
import co.ltlabs.ltmechanic.domain.ClosedTicket
import co.ltlabs.ltmechanic.domain.InRepairTicket
import co.ltlabs.ltmechanic.domain.Problems
import co.ltlabs.ltmechanic.domain.RepairedTicket
import co.ltlabs.ltmechanic.domain.ReportedTicket
import co.ltlabs.ltmechanic.domain.TicketRepairHistory
import co.ltlabs.ltmechanic.network.*
import co.ltlabs.ltmechanic.network.main.ReferenceApi
import co.ltlabs.ltmechanic.network.main.TicketApi
import co.ltlabs.ltmechanic.network.main.dto.*
import co.ltlabs.ltmechanic.repository.LtMechDatabaseRepository
import co.ltlabs.ltmechanic.util.*
import com.google.gson.Gson
import kotlinx.coroutines.*
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

import co.ltlabs.ltmechanic.domain.CommonProblem as CommonProblemDomain
import co.ltlabs.ltmechanic.domain.LatestProblem as LatestProblemDomain
import co.ltlabs.ltmechanic.domain.Problem as ProblemDomain
import co.ltlabs.ltmechanic.domain.Ticket as TicketDomain

private const val TAG = "TicketViewModel";

class TicketViewModel @Inject constructor(
    private val ticketApi: TicketApi,
    private val referenceApi: ReferenceApi,
    application: Application
) : AndroidViewModel(application) {

    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)
    private var createTicketJob: Job? = null

    private val database = getDatabase(application)
    private val ltMechDatabaseRepository = LtMechDatabaseRepository(database)

    val snackBarActionsFromDatabase = ltMechDatabaseRepository.snackBarActions

    private val _status = MutableLiveData<ApiStatus>()
    val status: LiveData<ApiStatus>
        get() = _status

    private val _commonProblems = MutableLiveData<List<CommonProblemDomain>>()
    val commonProblems: LiveData<List<CommonProblemDomain>>
        get() = _commonProblems

    private val _commonProblems2 = MutableLiveData<List<CommonProblemDomain>>()
    val commonProblems2: LiveData<List<CommonProblemDomain>>
        get() = _commonProblems2

    private val _latestProblems = MutableLiveData<List<LatestProblemDomain>>()
    val latestProblems: LiveData<List<LatestProblemDomain>>
        get() = _latestProblems

    private val _problems = MutableLiveData<List<ProblemDomain>>()
    val problems: LiveData<List<ProblemDomain>>
        get() = _problems

    private val _problemsList = MutableLiveData<Problems>()
    val problemsList: LiveData<Problems>
        get() = _problemsList

    private val _ticket = MutableLiveData<TicketDomain>()
    val ticket: LiveData<TicketDomain>
        get() = _ticket

    private val _createTicketStatus = MutableLiveData<CreateTicketStatus>()
    val createTicketStatus: LiveData<CreateTicketStatus>
        get() = _createTicketStatus

    private val _ticketUpdateStatus = MutableLiveData<TicketUpdateStatus>()
    val ticketUpdateStatus: LiveData<TicketUpdateStatus>
        get() = _ticketUpdateStatus

    private val _ticketReopenStatus = MutableLiveData<TicketReopenStatus>()
    val ticketReopenStatus: LiveData<TicketReopenStatus>
        get() = _ticketReopenStatus

    private val _checkList = MutableLiveData<List<TicketChecklist>>()
    val checkList: LiveData<List<TicketChecklist>>
        get() = _checkList

    private val _maintenanceCheckList = MutableLiveData<List<MaintenanceChecklist>>()
    val maintenanceCheckList: LiveData<List<MaintenanceChecklist>>
        get() = _maintenanceCheckList

    private val _ticketStatus = MutableLiveData<TicketStatus>()
    val ticketStatus: LiveData<TicketStatus>
        get() = _ticketStatus

    private val _machineHistory = MutableLiveData<List<MachineHistory>>()
    val machineHistory: LiveData<List<MachineHistory>>
        get() = _machineHistory

    private val _ticketRepairHistory = MutableLiveData<List<TicketRepairHistory>>()
    val ticketRepairHistory: LiveData<List<TicketRepairHistory>>
        get() = _ticketRepairHistory

    private val _reportedTickets = MutableLiveData<List<ReportedTicket>>()
    val reportedTickets: LiveData<List<ReportedTicket>>
        get() = _reportedTickets

    private val _inRepairTickets = MutableLiveData<List<InRepairTicket>>()
    val inRepairTickets: LiveData<List<InRepairTicket>>
        get() = _inRepairTickets

    private val _repairedTickets = MutableLiveData<List<RepairedTicket>>()
    val repairedTickets: LiveData<List<RepairedTicket>>
        get() = _repairedTickets

    private val _closedTickets = MutableLiveData<List<ClosedTicket>>()
    val closedTickets: LiveData<List<ClosedTicket>>
        get() = _closedTickets

    val checklistsTemp = mutableListOf<TicketChecklist>()

    var selectedProblemTemp = ProblemDomain(0, "")
    var problemTemp = mutableListOf<ProblemDomain>()

    fun getMachineProblems(machineId: Long) {

        viewModelScope.launch {

            _status.value = ApiStatus.LOADING
            val getTicketProblemsDeferred =
                ticketApi.getTicketProblemsAsync(machineId, "Bearer ${AuthUtil.token}")

            try {
                val result = getTicketProblemsDeferred.await()
                _status.value = ApiStatus.DONE

                if (result.success) {
                    val problems = result.problems
                    Log.d(TAG, "getMachineProblems: problems: $problems")
//                    val problemsList = Problems(
//                        problems.commonProblems.asCommonProblemDomainModel(),
//                        problems.latestProblem.asLatestProblemDomainModel(),
//                        problems.allProblems.asProblemDomainModel()
//                    )
                    val commonProblems =
                        problems.commonProblems.asCommonProblemDomainModel().toMutableList()
                    val removeCommon = commonProblems.filter { it.problemTypeId == 4L }
                    if (removeCommon.isNotEmpty()) {
                        commonProblems.remove(removeCommon[0])
                    }

                    val allProblems = problems.allProblems.asProblemDomainModel().toMutableList()
                    allProblems.map {
                        it.checked = false
                    }
                    val removeProblem = allProblems.filter { it.problemTypeId == 4L }
                    if (removeProblem.isNotEmpty()) {
                        allProblems.remove(removeProblem[0])
                    }

                    _problems.value = allProblems
                    _latestProblems.value = problems.latestProblem.asLatestProblemDomainModel()
                    _commonProblems.value = commonProblems

//                    _problemsList.value = problemsList
                } else {
                    _commonProblems.value = null
                    _latestProblems.value = null
                    _problems.value = null
//                    _problemsList.value = null
                }

            } catch (t: Throwable) {

                Log.e(TAG, "getMachineProblems: ", t)
//                _problemsList.value = null
                _commonProblems.value = null
                _latestProblems.value = null
                _problems.value = null
                _status.value = ApiStatus.ERROR
            }
        }

    }

    fun getMachineProblems2(machineId: Long) {

        viewModelScope.launch {

            _status.value = ApiStatus.LOADING
            val getTicketProblemsDeferred =
                ticketApi.getTicketProblemsAsync(machineId, "Bearer ${AuthUtil.token}")

            try {
                val result = getTicketProblemsDeferred.await()
                _status.value = ApiStatus.DONE

                if (result.success) {
                    val problems = result.problems
                    Log.d(TAG, "getMachineProblems: problems: $problems")
//                    val problemsList = Problems(
//                        problems.commonProblems.asCommonProblemDomainModel(),
//                        problems.latestProblem.asLatestProblemDomainModel(),
//                        problems.allProblems.asProblemDomainModel()
//                    )
                    val commonProblems =
                        problems.commonProblems.asCommonProblemDomainModel().toMutableList()
                    val removeCommon = commonProblems.filter { it.problemTypeId == 4L }
                    if (removeCommon.isNotEmpty()) {
                        commonProblems.remove(removeCommon[0])
                    }
                    _commonProblems2.value = commonProblems
                    val allProblems = problems.allProblems.asProblemDomainModel().toMutableList()
                    allProblems.map {
                        it.checked = false
                    }
                    val removeProblem = allProblems.filter { it.problemTypeId == 4L }
                    if (removeProblem.isNotEmpty()) {
                        allProblems.remove(removeProblem[0])
                    }
                    _problems.value = allProblems

//                    _problemsList.value = problemsList
                } else {
                    _commonProblems2.value = null
//                    _problemsList.value = null
                }

            } catch (t: Throwable) {

                Log.e(TAG, "getMachineProblems: ", t)
//                _problemsList.value = null
                _commonProblems.value = null
                _latestProblems.value = null
                _problems.value = null
                _status.value = ApiStatus.ERROR
            }
        }

    }

    fun createTicket(
        machineId: Long,
        problemTypeId: String?,
        remarks: String,
        reportedDt: String,
        assets: List<Asset>? = null,
        solutionTypeId: String? = null
    ) {


        val createTicketRequest = CreateTicketRequest(
            machineId.toString(),
            problemTypeId,
            solutionTypeId,
            remarks,
            reportedDt,
            assets
        )

        createTicketJob = viewModelScope.launch {

            try {

                _status.value = ApiStatus.LOADING
                val result =
                    ticketApi.createTicketAsync(createTicketRequest, "Bearer ${AuthUtil.token}")
                        .await()
                if (result.success) {
                    val ticketResult = result.tickets.asTicketDomainModel()[0]
                    ticketResult.ticketAsset = result.assets.asTicketAssetDomainModel()

                    _ticket.value = ticketResult

                    insertToSnackBarActionDatabase(
                        arrayOf(
                            DatabaseSnackBarAction(
                                1,
                                SNACK_BAR_ACTION_CREATE_TICKET,
                                true
                            )
                        )
                    )

                    _createTicketStatus.value = CreateTicketStatus.SUCCESS

                } else {
                    _createTicketStatus.value = CreateTicketStatus.FAILED
                }

                _status.value = ApiStatus.DONE

            } catch (t: Throwable) {

                if (t is HttpException) {

                    val error = t.response()?.errorBody()?.string()
                    try {

                        val gson = Gson()
                        val errorObj: String? = error
                        val errorJson = gson.fromJson(errorObj, Error::class.java)
                        if (errorJson.errors[0].machineId?.lowercase()
                                ?.contains("no attached repair checklist") == true
                        ) {
                            _createTicketStatus.value = CreateTicketStatus.NO_ATTACHED_CHECKLIST
                        }

                        if (errorJson.errors[0].remarks?.contains("is 300") == true) {
                            _createTicketStatus.value = CreateTicketStatus.REACHED_REMARKS_LIMIT
                        }

                        if (errorJson.errors[0].machineId?.contains("has open") == true) {
                            _createTicketStatus.value = CreateTicketStatus.HAS_OPEN_TICKETS
                        }

                    } catch (e: Exception) {
                        Log.e(TAG, "createTicket: ", e)
                    }

                } else {
                    _createTicketStatus.value = null
                }

                _status.value = ApiStatus.ERROR
                _createTicketStatus.value = CreateTicketStatus.ERROR
            }

        }

    }

    fun getTicketDetailsById(ticketId: Long) {

        viewModelScope.launch {

            val getTicketDetailsDeferred =
                ticketApi.getTicketDetailsAsync(ticketId, "Bearer ${AuthUtil.token}")

            _status.value = ApiStatus.LOADING

            try {

                val result = getTicketDetailsDeferred.await()

                if (result.success) {
                    TicketUtil.reopenTicketEnabled = result.reopenTag?.value == "Y"
                    val ticketResult = result.tickets.asTicketDomainModel()[0]
                    ticketResult.ticketAsset = result.assets.asTicketAssetDomainModel()
                    val ticketCheckList = result.checkist
                    val checklistTemp = mutableListOf<TicketChecklist>()

                    var parentId = 1

                    ticketCheckList?.forEach { checklist ->

                        checklist.task?.let { task ->
                            checklistTemp.add(
                                TicketChecklist(
                                    task.id,
                                    task.desc1 ?: "",
                                    task.isComplete.toBoolean(),
                                    false,
                                    "$parentId"
                                )
                            )
                        }



                        checklist.ticketSubTasks?.forEachIndexed { index, subTask ->
                            checklistTemp.add(
                                TicketChecklist(
                                    subTask.id,
                                    subTask.desc1 ?: "",
                                    subTask.isComplete.toBoolean(),
                                    true,
                                    "$parentId.${index + 1}"
                                )
                            )
                        }

                        parentId++

                    }

                    var parentTag = 1

                    checklistTemp.forEachIndexed { index, checklist ->
//                        if (index != checklistTemp.size - 1) {
//                            if (!checklist.subtask) {
//                                checklist.tag = "parent$parentTag"
//                            } else {
//                                checklist.tag = "child-parent$parentTag"
//                            }
//
//                            if (!checklistTemp[index + 1].subtask) {
//                                parentTag++
//                            }
//                        }

//                        if (index == checklistTemp.size - 1) {
//
//
//                            if (!checklistTemp[checklistTemp.size - 1].subtask) {
//                                checklistTemp[checklistTemp.size - 1].tag = "parent$parentTag"
//                            }
//
//                        }
                    }

                    Log.d(TAG, "getTicketDetailsById: checklistTemp: $checklistTemp")

                    _checkList.value = checklistTemp
                    _maintenanceCheckList.value = checklistTemp.asMaintenanceChecklistDomainModel()


                    Log.d(TAG, "getTicketDetailsById: ticketResult: $ticketResult")

                    _ticket.value = ticketResult

                } else {
                    _ticket.value = null
                }

                _status.value = ApiStatus.DONE

            } catch (t: Throwable) {

                Log.e(TAG, "getTicketDetailsById: ", t)

                _status.value = ApiStatus.ERROR
            }
        }
    }

    fun getTicketDetailsByTicketNo(ticketNo: String) {

        viewModelScope.launch {

            val getTicketDetailsDeferred =
                ticketApi.getTicketDetailsByTicketNoAsync(ticketNo, "Bearer ${AuthUtil.token}")

            _status.value = ApiStatus.LOADING

            try {

                val result = getTicketDetailsDeferred.await()

                if (result.success) {
                    TicketUtil.reopenTicketEnabled = result.reopenTag?.value == "Y"
                    val ticketResult = result.tickets.asTicketDomainModel()[0]
                    ticketResult.ticketAsset = result.assets.asTicketAssetDomainModel()
                    val ticketCheckList = result.checkist
                    val checklistTemp = mutableListOf<TicketChecklist>()

                    var parentId = 1

                    ticketCheckList?.forEach { checklist ->

                        checklist.task?.let { task ->
                            checklistTemp.add(
                                TicketChecklist(
                                    task.id,
                                    task.desc1 ?: "",
                                    task.isComplete.toBoolean(),
                                    false,
                                    "$parentId"
                                )
                            )
                        }



                        checklist.ticketSubTasks?.forEachIndexed { index, subTask ->
                            checklistTemp.add(
                                TicketChecklist(
                                    subTask.id,
                                    subTask.desc1 ?: "",
                                    subTask.isComplete.toBoolean(),
                                    true,
                                    "$parentId.${index + 1}"
                                )
                            )
                        }

                        parentId++

                    }

                    var parentTag = 1

                    checklistTemp.forEachIndexed { index, checklist ->
//                        if (index != checklistTemp.size - 1) {
//                            if (!checklist.subtask) {
//                                checklist.tag = "parent$parentTag"
//                            } else {
//                                checklist.tag = "child-parent$parentTag"
//                            }
//
//                            if (!checklistTemp[index + 1].subtask) {
//                                parentTag++
//                            }
//                        }

//                        if (index == checklistTemp.size - 1) {
//
//
//                            if (!checklistTemp[checklistTemp.size - 1].subtask) {
//                                checklistTemp[checklistTemp.size - 1].tag = "parent$parentTag"
//                            }
//
//                        }
                    }

                    Log.d(TAG, "getTicketDetailsById: checklistTemp: $checklistTemp")

                    _checkList.value = checklistTemp
                    _maintenanceCheckList.value = checklistTemp.asMaintenanceChecklistDomainModel()


                    Log.d(TAG, "getTicketDetailsById: ticketResult: $ticketResult")

                    _ticket.value = ticketResult

                } else {
                    _ticket.value = null
                }

                _status.value = ApiStatus.DONE

            } catch (t: Throwable) {

                Log.e(TAG, "getTicketDetailsById: ", t)

                _status.value = ApiStatus.ERROR
            }
        }
    }

    fun getStatusIdAndUpdateTicketStatus(
        status: String,
        module: String,
        ticketNo: String,
        remarks: String = "",
        solutionTypeId: String? = null,
        problemTypeId: String? = null,
        assets: List<Asset>? = null,
        type: String = "R"
    ) {

        viewModelScope.launch {

            val getStatusByDescDeferred = referenceApi.getStatusByDescAsync(
                status,
                module,
                accessToken = "Bearer ${AuthUtil.token}"
            )

            try {

                _status.value = ApiStatus.LOADING

                val result = getStatusByDescDeferred.await()

                if (result.success) {

                    val statusIdFetched = result.asStatusIdDomainModel()
                    statusIdFetched.type = status
                    statusIdFetched.module = module

                    when (statusIdFetched.type) {

                        TicketsStatus.IN_REPAIR -> {
                            StatusIdUtil.RT_IN_REPAIR = statusIdFetched.statusId
                        }

                        TicketsStatus.REPAIRED -> {
                            StatusIdUtil.RT_REPAIRED = statusIdFetched.statusId
                        }

                        TicketsStatus.CANCELLED -> {

                            if (statusIdFetched.module == TicketModule.MAINTENANCE) {
                                StatusIdUtil.MT_CANCELLED = statusIdFetched.statusId
                            } else {
                                StatusIdUtil.RT_CANCELLED = statusIdFetched.statusId
                            }

                        }

                        TicketsStatus.CLOSED -> {
                            StatusIdUtil.RT_CLOSED = statusIdFetched.statusId
                        }

                        TicketsStatus.IN_PROGRESS -> {
                            StatusIdUtil.MT_IN_PROGRESS = statusIdFetched.statusId
                        }

                        TicketsStatus.COMPLETED -> {
                            StatusIdUtil.MT_COMPLETED = statusIdFetched.statusId
                        }

                    }

//                    updateTicketStatus(ticketNo, statusIdFetched.statusId.toString(), remarks = remarks)
                    updateTicketStatus(
                        ticketNo,
                        statusIdFetched.statusId.toString(),
                        remarks,
                        solutionTypeId,
                        problemTypeId,
                        assets,
                        type
                    )

//                    _statusId.value = statusIdFetched

                } else {
//                    _statusId.value = null
                }

            } catch (t: Throwable) {


                Log.e(TAG, "getStatusByDesc: ", t)

            }

        }

    }

    fun updateTicketStatus(
        ticketNo: String,
        statusId: String,
        remarks: String = "",
        solutionTypeId: String? = null,
        problemTypeId: String? = null,
        assets: List<Asset>? = null,
        type: String = "R"
    ) {

        Log.d(TAG, "updateTicketStatus: statusId: $statusId")

        val sTypeId = if (solutionTypeId == "0") {
            null
        } else {
            solutionTypeId
        }

        val pTypeId = if (problemTypeId == "0") {
            null
        } else {
            problemTypeId
        }

        val ticketUpdateStatusRequest = UpdateTicketStatusRequest(
            listOf(TicketNo(ticketNo)),
            type,
            statusId,
            pTypeId,
            sTypeId,
            remarks,
            assets
        )

        Log.d(TAG, "updateTicketStatus: ticketUpdateStatusRequest: $ticketUpdateStatusRequest")

        val updateTicketStatusDeferred =
            ticketApi.updateTicketStatusAsync(ticketUpdateStatusRequest, "Bearer ${AuthUtil.token}")

        viewModelScope.launch {


            try {

                val result = updateTicketStatusDeferred.await()

                _status.value = ApiStatus.DONE

                Log.d(TAG, "updateTicketStatus: result.result: ${result.result[0].ticket}")

                if (result.result != null) {
                    if (result.result[0].ticket.contains("successfully updated")) {


                        when (statusId.toLong()) {

                            StatusIdUtil.RT_IN_REPAIR -> {
                                _ticketStatus.value = TicketStatus.IN_REPAIR
                            }

                            StatusIdUtil.RT_REPAIRED -> {
                                _ticketStatus.value = TicketStatus.REPAIRED
                            }

                            // TODO uncomment
                            StatusIdUtil.RT_CANCELLED -> {
                                // TODO remove
//                            1404L -> {
                                insertToSnackBarActionDatabase(
                                    arrayOf(
                                        DatabaseSnackBarAction(
                                            1,
                                            SNACK_BAR_ACTION_CANCEL_TICKET,
                                            true
                                        )
                                    )
                                )
                                _ticketStatus.value = TicketStatus.CANCELLED
                            }

                            StatusIdUtil.RT_CLOSED -> {
                                insertToSnackBarActionDatabase(
                                    arrayOf(
                                        DatabaseSnackBarAction(
                                            1,
                                            SNACK_BAR_ACTION_CLOSE_TICKET,
                                            true
                                        )
                                    )
                                )
                                _ticketStatus.value = TicketStatus.CLOSED
                            }

                            StatusIdUtil.MT_IN_PROGRESS -> {
                                insertToSnackBarActionDatabase(
                                    arrayOf(
                                        DatabaseSnackBarAction(
                                            1,
                                            SNACK_BAR_ACTION_IN_PROGRESS_TICKET,
                                            true
                                        )
                                    )
                                )
                                _ticketStatus.value = TicketStatus.IN_PROGRESS
                            }

                            StatusIdUtil.MT_COMPLETED -> {
                                insertToSnackBarActionDatabase(
                                    arrayOf(
                                        DatabaseSnackBarAction(
                                            1,
                                            SNACK_BAR_ACTION_COMPLETED_TICKET,
                                            true
                                        )
                                    )
                                )
                                _ticketStatus.value = TicketStatus.COMPLETED
                            }

                        }

                        _ticketUpdateStatus.value = TicketUpdateStatus.SUCCESS

                    } else {
                        _ticketUpdateStatus.value = TicketUpdateStatus.FAILED
                    }
                }

            } catch (t: Throwable) {

                Log.e(TAG, "updateTicketStatus: ", t)

                if (t is HttpException) {

                    val error = t.response()?.errorBody()?.string()

                    val gson = Gson()
                    val errorObj: String? = error
                    val errorJson = gson.fromJson(errorObj, Error::class.java)

                    Log.d(TAG, "checkInMachine: error: $error")
                    Log.d(TAG, "checkInMachine: errorObj: $errorJson")

                    if (error?.toLowerCase()?.contains("open repair ticket") == true) {
                        _ticketUpdateStatus.value = TicketUpdateStatus.HAS_OPEN_TICKETS
                    }
                }

                _status.value = ApiStatus.ERROR
            }

        }

    }

    fun reopenTicket(ticketNo: String) {

        val reopenTicketRequest = ReopenTicketRequest(
            listOf(TicketNo(ticketNo))
        )

        val reopenTicketDeferred =
            ticketApi.reopenTicketAsync(reopenTicketRequest, "Bearer ${AuthUtil.token}")

        viewModelScope.launch {

            _status.value = ApiStatus.LOADING


            try {

                val result = reopenTicketDeferred.await()

                _status.value = ApiStatus.DONE

                Log.d(TAG, "updateTicketStatus: result.result: ${result.result[0].ticket}")

                if (result.result != null) {
                    if (result.result[0].ticket.contains("successfully updated")) {

                        insertToSnackBarActionDatabase(
                            arrayOf(
                                DatabaseSnackBarAction(
                                    1,
                                    SNACK_BAR_ACTION_REOPEN_TICKET,
                                    true
                                )
                            )
                        )
                        _ticketReopenStatus.value = TicketReopenStatus.SUCCESS

                    } else {
                        _ticketReopenStatus.value = TicketReopenStatus.FAILED
                    }
                }

            } catch (t: Throwable) {

                Log.e(TAG, "updateTicketStatus: ", t)

                if (t is HttpException) {

                    val error = t.response()?.errorBody()?.string()

                    val gson = Gson()
                    val errorObj: String? = error
                    val errorJson = gson.fromJson(errorObj, Error::class.java)

                    Log.d(TAG, "checkInMachine: error: $error")
                    Log.d(TAG, "checkInMachine: errorObj: $errorJson")

                    if (error?.toLowerCase()?.contains("open ticket") == true) {
                        _ticketReopenStatus.value = TicketReopenStatus.HAS_OPEN_TICKETS
                    } else if (error?.toLowerCase()?.contains("current time exceeded") == true) {
                        _ticketReopenStatus.value = TicketReopenStatus.TIME_EXCEEDED
                    } else if (error?.toLowerCase()?.contains("currently disabled") == true) {
                        _ticketReopenStatus.value = TicketReopenStatus.NOT_ALLOWED
                    }
                } else {
                    _ticketReopenStatus.value = TicketReopenStatus.FAILED
                }

                _status.value = ApiStatus.ERROR
            }

        }

    }

    fun getMachineRepairedHistoryByMachineId(machineId: Long) {

        viewModelScope.launch {

            val getMachineRepairedHistoryDeferred =
                ticketApi.getMachineRepairedHistoryAsync(machineId, "Bearer ${AuthUtil.token}")

            _status.value = ApiStatus.LOADING

            try {

                val result = getMachineRepairedHistoryDeferred.await()


                if (result.success) {

                    if (result.data.isNotEmpty()) {

                        val machineHistoryResult = if (result.data.size > 5) {
                            result.data.asMachineHistoryDomainModel().take(5)
                        } else {
                            result.data.asMachineHistoryDomainModel()
                        }

                        _machineHistory.value = machineHistoryResult

                    } else {
                        _machineHistory.value = result.data.asMachineHistoryDomainModel()
                    }

                } else {
                    _machineHistory.value = null
                }

                _status.value = ApiStatus.DONE

            } catch (t: Throwable) {

                Log.e(TAG, "getMachineRepairedHistoryByMachineId: ", t)

                _status.value = ApiStatus.ERROR
            }
        }
    }

    fun getTicketRepairHistoryByMachineId(machineId: Long) {

        viewModelScope.launch {

            val getTicketRepairHistoryDeferred = ticketApi.getTicketRepairHistoryAsync(
                machineId,
                accessToken = "Bearer ${AuthUtil.token}"
            )

            _status.value = ApiStatus.LOADING

            try {

                val result = getTicketRepairHistoryDeferred.await()


                if (result.success) {

                    if (result.tickets.isNotEmpty()) {

//                        val machineHistoryResult = if (result.tickets.result.size > 5) {
                        val ticketRepairHistoryResult =
                            result.tickets.asTicketRepairHistoryDomainModel()
//                        } else {
//                            result.tickets.result.asTicketRepairHistoryDomainModel()
//                        }

                        _ticketRepairHistory.value = ticketRepairHistoryResult

                    } else {
                        _ticketRepairHistory.value =
                            result.tickets.asTicketRepairHistoryDomainModel()
                    }

                } else {
                    _ticketRepairHistory.value = null
                }

                _status.value = ApiStatus.DONE

            } catch (t: Throwable) {

                Log.e(TAG, "getTicketRepairHistoryByMachineId: ", t)

                _status.value = ApiStatus.ERROR
            }
        }
    }

    fun getReportedTickets(lineSelected: String, areaSelected: String = "") {

        Log.d(TAG, "getReportedTickets: lineSelected: $lineSelected \n areaSelected: $areaSelected")

        viewModelScope.launch {

            val getReportedTicketsDeferred =
                ticketApi.getReportedTicketsAsync(lineSelected, areaSelected)

            _status.value = ApiStatus.LOADING

            try {
                val result = getReportedTicketsDeferred.await()

                Log.d(TAG, "getReportedTickets: result.success: ${result.success}")

                if (result.success) {

                    Log.d(TAG, "getReportedTickets: result.data size: ${result.data.size}")

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

    fun getInRepairTickets(lineSelected: String, areaSelected: String = "") {

        viewModelScope.launch {

            val getInRepairTicketsDeferred =
                ticketApi.getInRepairTicketsAsync(lineSelected, areaSelected)

            _status.value = ApiStatus.LOADING

            try {
                val result = getInRepairTicketsDeferred.await()
                if (result.success) {

                    val inRepairTicketResult = result.data.asInRepairTicketDomainModel()
                    _inRepairTickets.value = inRepairTicketResult

                    _status.value = ApiStatus.DONE

                } else {
                    _inRepairTickets.value = null
                }

            } catch (t: Throwable) {
                _status.value = ApiStatus.ERROR
            }
        }

    }

    fun getRepairedTickets(lineSelected: String, areaSelected: String = "") {

        viewModelScope.launch {

            val getRepairedTicketsDeferred =
                ticketApi.getRepairedTicketsAsync(lineSelected, areaSelected)

            _status.value = ApiStatus.LOADING

            try {
                val result = getRepairedTicketsDeferred.await()


                if (result.success) {

                    val repairedTicketResult = result.data.asRepairedTicketDomainModel()
                    _repairedTickets.value = repairedTicketResult

                    _status.value = ApiStatus.DONE

                } else {
                    _repairedTickets.value = null
                }

            } catch (t: Throwable) {

                Log.e(TAG, "getReportedTickets: ", t)
                _status.value = ApiStatus.ERROR
            }
        }


    }

    fun getClosedTickets(lineSelected: String, areaSelected: String = "") {

        viewModelScope.launch {

            val getClosedTicketsDeferred =
                ticketApi.getClosedTicketsAsync(lineSelected, areaSelected)

            _status.value = ApiStatus.LOADING

            try {
                val result = getClosedTicketsDeferred.await()


                if (result.success) {

                    val closedTicketResult = result.data.asClosedTicketDomainModel()
                    _closedTickets.value = closedTicketResult

                    _status.value = ApiStatus.DONE

                } else {
                    _closedTickets.value = null
                }

            } catch (t: Throwable) {

                Log.e(TAG, "getClosedTickets: ", t)
                _status.value = ApiStatus.ERROR
            }
        }


    }

    fun setChecklists(checklists: List<TicketChecklist>) {
        checklistsTemp.addAll(checklists)
    }


    fun updateChecklists(checklist: TicketChecklist, position: Int) {

        if (checklistsTemp.isNotEmpty()) {
            checklistsTemp[position] = checklist

            val parents = checklistsTemp.filter { !it.subtask }
            val children = checklistsTemp.filter { it.subtask }

            parents.forEachIndexed { index, parent ->
                if (parent.checked) {
                    children.filter { it.tag.contains("$index.") }.forEach {
                        it.checked = true
                    }
                }
            }

            children.forEachIndexed { index, child ->

            }

            checklistsTemp.clear()
            checklistsTemp.addAll(parents)
            checklistsTemp.addAll(children)

            _checkList.value = checklistsTemp.sortedBy { it.tag }
        }

    }

    private fun insertToSnackBarActionDatabase(snackBarActions: Array<DatabaseSnackBarAction>) {
        viewModelScope.launch {
            Log.d(TAG, "insertToSnackBarActionDatabase: snackBarActions: $snackBarActions")
            ltMechDatabaseRepository.insertSnackbarActions(snackBarActions)
        }
    }

    fun finishInsertToSnackBarActionDatabase() {
        viewModelScope.launch {
            ltMechDatabaseRepository.insertSnackbarActions(
                (arrayOf(
                    DatabaseSnackBarAction(
                        1,
                        SNACK_BAR_ACTION_NONE,
                        false
                    )
                ))
            )
        }
    }

    fun ticketUpdateComplete() {
        _ticketUpdateStatus.value = null
    }

    fun ticketComplete() {
        _ticket.value = null
    }

    fun checklistComplete() {
        _checkList.value = null
    }

    fun maintenanceChecklistComplete() {
        _maintenanceCheckList.value = null
    }

    fun resetProblems(problems: MutableList<ProblemDomain>) {
        _problems.value = problems
    }

    fun updateProblem(problems: List<ProblemDomain>) {
        _problems.value = problems
    }

    fun commonProblemsComplete() {
        _commonProblems.value = null
    }

    fun commonProblems2Complete() {
        _commonProblems2.value = null
    }

    fun ticketStatusComplete() {
        _ticketStatus.value = null
    }

    fun ticketReopenStatusComplete() {
        _ticketReopenStatus.value = null
    }

    override fun onCleared() {
        super.onCleared()

        viewModelJob.cancel()
    }

    fun cancelTicketJob() {
        createTicketJob?.cancel()
        createTicketJob = null
    }
}