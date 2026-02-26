package co.ltlabs.ltmechanic.viewmodels.shared

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.ltlabs.ltmechanic.domain.MaintenanceChecklist
import co.ltlabs.ltmechanic.domain.TicketChecklist
import co.ltlabs.ltmechanic.network.SubTaskRequest
import co.ltlabs.ltmechanic.network.TaskRequest
import co.ltlabs.ltmechanic.network.Tasks
import co.ltlabs.ltmechanic.network.UpdateChecklistRequest
import co.ltlabs.ltmechanic.network.main.TicketApi
import co.ltlabs.ltmechanic.util.ApiStatus
import co.ltlabs.ltmechanic.util.AuthUtil
import co.ltlabs.ltmechanic.util.ChecklistStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ChecklistViewModel"

class ChecklistViewModel @Inject constructor(
    private val ticketApi: TicketApi
) : ViewModel() {

    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _status = MutableLiveData<ApiStatus>()
    val status: LiveData<ApiStatus>
        get() = _status

    private val _checklistStatus = MutableLiveData<ChecklistStatus>()
    val checklistStatus: LiveData<ChecklistStatus>
        get() = _checklistStatus

    private val _completeChecklistStatus = MutableLiveData<ChecklistStatus>()
    val completeChecklistStatus: LiveData<ChecklistStatus>
        get() = _completeChecklistStatus

    private val _selectedTaskCount = MutableLiveData<Int>()
    val selectedTaskCount: LiveData<Int>
        get() = _selectedTaskCount

    var checklistsTemp = mutableListOf<TicketChecklist>()
    var maintenanceChecklistsTemp = mutableListOf<MaintenanceChecklist>()

    fun updateChecklist(ticketChecklists: List<TicketChecklist>) {

        Log.d(TAG, "updateChecklist: ticketChecklists: $ticketChecklists")

        _status.value = ApiStatus.LOADING

        val checklistSize = ticketChecklists.filter { !it.subtask }.size

        val checklists = arrayOfNulls<Tasks>(checklistSize)

        var taskRequest = TaskRequest(0, 0)

        val tasks = Tasks(TaskRequest(0, 0), null)
        val subTasks = mutableListOf<SubTaskRequest>()
        var subTasksTemp = mutableListOf<SubTaskRequest>()
        var count = 0

        ticketChecklists.forEachIndexed { index, ticketChecklist ->
            val isComplete = if (ticketChecklist.checked) {
                1
            } else {
                0
            }

            if (index != ticketChecklists.size - 1) {

                Log.d(TAG, "updateChecklist: !ticketChecklist.subtask: ${!ticketChecklist.subtask}")

                if (!ticketChecklist.subtask) {
                    taskRequest = TaskRequest(isComplete, ticketChecklist.id)
                } else {
                    subTasks.add(SubTaskRequest(isComplete, ticketChecklist.id))
                }

                Log.d(
                    TAG,
                    "updateChecklist: !ticketChecklists[index + 1].subtask ${!ticketChecklists[index + 1].subtask}"
                )

                if (!ticketChecklists[index + 1].subtask) {
                    Log.d(TAG, "updateChecklist: is task")
                    subTasksTemp = subTasks
                    checklists[count] = Tasks(taskRequest, MutableList(subTasksTemp.size) { index ->
                        subTasksTemp[index]
                    })

//                    subTasks.clear()
                    count++
                    subTasks.clear()
                    Log.d(TAG, "updateChecklist: subTasksTemp size: ${subTasksTemp.size}")
                }


                Log.d(TAG, "updateChecklist: index: $index")

            }

            if (index == ticketChecklists.size - 1) {
                if (!ticketChecklists[ticketChecklists.size - 1].subtask) {
                    taskRequest = TaskRequest(isComplete, ticketChecklist.id)
                    Log.d(TAG, "updateChecklist: count: $count")
                    subTasksTemp = subTasks
                    checklists[count] = Tasks(taskRequest, MutableList(subTasksTemp.size) { index ->
                        subTasksTemp[index]
                    })

                    count++
                    subTasks.clear()
                    Log.d(TAG, "updateChecklist: subTasksTemp size: ${subTasksTemp.size}")
                } else {
                    subTasks.add(
                        SubTaskRequest(
                            isComplete,
                            ticketChecklists[ticketChecklists.size - 1].id
                        )
                    )
                    subTasksTemp = subTasks
                    checklists[count] = Tasks(taskRequest, MutableList(subTasksTemp.size) { index ->
                        subTasksTemp[index]
                    })

                    count++
                    subTasks.clear()
                    Log.d(TAG, "updateChecklist: subTasksTemp size: ${subTasksTemp.size}")
                }
            }

        }


        viewModelScope.launch {


            val updateChecklistDeferred = ticketApi.updateChecklistAsync(
                UpdateChecklistRequest(
                    checklists.toList() as List<Tasks>
                ),
                "Bearer ${AuthUtil.token}"
            )

            try {

                val result = updateChecklistDeferred.await()

                if (result.success) {
                    _checklistStatus.value = ChecklistStatus.SUCCESS
                } else {
                    _checklistStatus.value = ChecklistStatus.FAILED
                }

                _status.value = ApiStatus.DONE

            } catch (t: Throwable) {
                Log.e(TAG, "updateChecklist: ", t)

                _status.value = ApiStatus.ERROR
            }

        }

    }

    fun updateMaintenanceChecklist(
        maintenanceChecklists: List<MaintenanceChecklist>,
        ticketNo: String? = null,
        remark: String? = null
    ) {

        Log.d(TAG, "updateChecklist: ticketChecklists: $maintenanceChecklists")

        _status.value = ApiStatus.LOADING

        val checklistSize = maintenanceChecklists.filter { !it.subtask }.size

        val checklists = arrayOfNulls<Tasks>(checklistSize)

        var taskRequest = TaskRequest(0, 0)

        val tasks = Tasks(TaskRequest(0, 0), null)
        val subTasks = mutableListOf<SubTaskRequest>()
        var subTasksTemp = mutableListOf<SubTaskRequest>()
        var count = 0

        maintenanceChecklists.forEachIndexed { index, ticketChecklist ->
            val isComplete = if (ticketChecklist.checked) {
                1
            } else {
                0
            }

            if (index != maintenanceChecklists.size - 1) {

                if (!ticketChecklist.subtask) {
                    taskRequest = TaskRequest(isComplete, ticketChecklist.id)
                } else {
                    subTasks.add(SubTaskRequest(isComplete, ticketChecklist.id))
                }

                if (!maintenanceChecklists[index + 1].subtask) {
                    Log.d(TAG, "updateChecklist: is task")
                    subTasksTemp = subTasks
                    checklists[count] = Tasks(taskRequest, MutableList(subTasksTemp.size) {index ->
                        subTasksTemp[index]
                    })

//                    subTasks.clear()
                    count++
                    subTasks.clear()
                    Log.d(TAG, "updateChecklist: subTasksTemp size: ${subTasksTemp.size}")
                }


                Log.d(TAG, "updateChecklist: index: $index")

            }

            if (index == maintenanceChecklists.size - 1) {
                if (!maintenanceChecklists[maintenanceChecklists.size - 1].subtask) {
                    taskRequest = TaskRequest(isComplete, ticketChecklist.id)
                    Log.d(TAG, "updateChecklist: count: $count")
                    subTasksTemp = subTasks
                    checklists[count] = Tasks(taskRequest, MutableList(subTasksTemp.size) { index ->
                        subTasksTemp[index]
                    })

                    count++
                    subTasks.clear()
                    Log.d(TAG, "updateChecklist: subTasksTemp size: ${subTasksTemp.size}")
                } else {
                    subTasks.add(SubTaskRequest(isComplete, maintenanceChecklists[maintenanceChecklists.size - 1].id))
                    subTasksTemp = subTasks
                    checklists[count] = Tasks(taskRequest, MutableList(subTasksTemp.size) {index ->
                        subTasksTemp[index]
                    })

                    count++
                    subTasks.clear()
                    Log.d(TAG, "updateChecklist: subTasksTemp size: ${subTasksTemp.size}")
                }
            }

        }


        viewModelScope.launch {


            val updateChecklistDeferred =
                ticketApi.updateChecklistAsync(
                    UpdateChecklistRequest(
                        checklists.toList() as List<Tasks>,
                        ticketNo,
                        remark
                    ),
                    "Bearer ${AuthUtil.token}"
                )

            try {

                val result = updateChecklistDeferred.await()

                if (result.success) {
                    _checklistStatus.value = ChecklistStatus.SUCCESS
                } else {
                    _checklistStatus.value = ChecklistStatus.FAILED
                }

                _status.value = ApiStatus.DONE

            } catch (t: Throwable) {
                Log.e(TAG, "updateChecklist: ", t)

                _status.value = ApiStatus.ERROR
            }

        }

    }

    fun completeMaintenanceChecklist(
        maintenanceChecklists: List<MaintenanceChecklist>,
        ticketNo: String? = null,
        remark: String? = null
    ) {

        Log.d(TAG, "updateChecklist: ticketChecklists: $maintenanceChecklists")

        _status.value = ApiStatus.LOADING

        val checklistSize = maintenanceChecklists.filter { !it.subtask }.size

        val checklists = arrayOfNulls<Tasks>(checklistSize)

        var taskRequest = TaskRequest(0, 0)

        val tasks = Tasks(TaskRequest(0, 0), null)
        val subTasks = mutableListOf<SubTaskRequest>()
        var subTasksTemp = mutableListOf<SubTaskRequest>()
        var count = 0

        maintenanceChecklists.forEachIndexed { index, ticketChecklist ->
            val isComplete = if (ticketChecklist.checked) {
                1
            } else {
                0
            }

            if (index != maintenanceChecklists.size - 1) {

                Log.d(TAG, "updateChecklist: !ticketChecklist.subtask: ${!ticketChecklist.subtask}")

                if (!ticketChecklist.subtask) {
                    taskRequest = TaskRequest(isComplete, ticketChecklist.id)
                } else {
                    subTasks.add(SubTaskRequest(isComplete, ticketChecklist.id))
                }

                if (!maintenanceChecklists[index + 1].subtask) {
                    Log.d(TAG, "updateChecklist: is task")
                    subTasksTemp = subTasks
                    checklists[count] = Tasks(taskRequest, MutableList(subTasksTemp.size) { index ->
                        subTasksTemp[index]
                    })

//                    subTasks.clear()
                    count++
                    subTasks.clear()
                    Log.d(TAG, "updateChecklist: subTasksTemp size: ${subTasksTemp.size}")
                }


                Log.d(TAG, "updateChecklist: index: $index")

            }

            if (index == maintenanceChecklists.size - 1) {
                if (!maintenanceChecklists[maintenanceChecklists.size - 1].subtask) {
                    taskRequest = TaskRequest(isComplete, ticketChecklist.id)
                    Log.d(TAG, "updateChecklist: count: $count")
                    subTasksTemp = subTasks
                    checklists[count] = Tasks(taskRequest, MutableList(subTasksTemp.size) { index ->
                        subTasksTemp[index]
                    })

                    count++
                    subTasks.clear()
                    Log.d(TAG, "updateChecklist: subTasksTemp size: ${subTasksTemp.size}")
                } else {
                    subTasks.add(
                        SubTaskRequest(
                            isComplete,
                            maintenanceChecklists[maintenanceChecklists.size - 1].id
                        )
                    )
                    subTasksTemp = subTasks
                    checklists[count] = Tasks(taskRequest, MutableList(subTasksTemp.size) { index ->
                        subTasksTemp[index]
                    })

                    count++
                    subTasks.clear()
                    Log.d(TAG, "updateChecklist: subTasksTemp size: ${subTasksTemp.size}")
                }
            }

        }


        viewModelScope.launch {


            val updateChecklistDeferred = ticketApi.updateChecklistAsync(
                UpdateChecklistRequest(
                    checklists.toList() as List<Tasks>,
                    ticketNo,
                    remark
                ),
                "Bearer ${AuthUtil.token}"
            )

            try {

                val result = updateChecklistDeferred.await()

                if (result.success) {
                    _completeChecklistStatus.value = ChecklistStatus.SUCCESS
                } else {
                    _completeChecklistStatus.value = ChecklistStatus.FAILED
                }

                _status.value = ApiStatus.DONE

            } catch (t: Throwable) {
                Log.e(TAG, "updateChecklist: ", t)

                _status.value = ApiStatus.ERROR
            }

        }

    }

    fun setChecklists(checklists: List<TicketChecklist>) {
        checklistsTemp.addAll(checklists)
    }

    fun setMaintenanceChecklists(checklists: List<MaintenanceChecklist>) {
        maintenanceChecklistsTemp.addAll(checklists)
    }

    fun updateChecklists(checklists: List<TicketChecklist>) {

        Log.d(TAG, "updateChecklists: checklists: $checklists")

        if (checklistsTemp.isNotEmpty()) {
            checklistsTemp = checklists.toMutableList()

        }

    }

    fun updateMaintenanceChecklists(checklists: List<MaintenanceChecklist>) {

        Log.d(TAG, "updateChecklists: checklists: $checklists")

        if (maintenanceChecklistsTemp.isNotEmpty()) {
            maintenanceChecklistsTemp = checklists.toMutableList()

        }

    }

    fun updateSelectedTaskCount(count: Int) {
        _selectedTaskCount.value = count
    }

    fun checklistStatusComplete() {
        _checklistStatus.value = null
    }

    fun completeChecklistStatusComplete() {
        _completeChecklistStatus.value = null
    }

    override fun onCleared() {
        super.onCleared()

        viewModelJob.cancel()
    }

}