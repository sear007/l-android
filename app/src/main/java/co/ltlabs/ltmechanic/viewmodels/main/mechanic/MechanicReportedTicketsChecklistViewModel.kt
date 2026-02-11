package co.ltlabs.ltmechanic.viewmodels.main.mechanic

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import co.ltlabs.ltmechanic.database.getDatabase
import co.ltlabs.ltmechanic.domain.TicketChecklist
import co.ltlabs.ltmechanic.network.FCMData
import co.ltlabs.ltmechanic.network.FCMMessage
import co.ltlabs.ltmechanic.network.FirebaseSendNotificationRequest
import co.ltlabs.ltmechanic.network.main.FirebaseCloudMessagingApi
import co.ltlabs.ltmechanic.repository.LtMechDatabaseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MRTChecklistViewModel";

class MechanicReportedTicketsChecklistViewModel @Inject constructor(
                                                                    application: Application
) : AndroidViewModel(application) {

    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val database = getDatabase(application)
    private val mfgLinesDatabaseRepository = LtMechDatabaseRepository(database)

//    val ticketCheckList = mutableListOf(
//        TicketChecklist (
//            "Thread stand.",
//            true
//        ),
//        TicketChecklist (
//            "Thread con holders.",
//            subtask = true
//        ),
//        TicketChecklist (
//            "Thread tention adjustment system.",
//            subtask = true
//        ),
//        TicketChecklist (
//            "Machine head.",
//            subtask = true
//        ),
//        TicketChecklist (
//            "Thread stand."
//        ),
//        TicketChecklist (
//            "Thread corn holder."
//        ),
//        TicketChecklist (
//            "Thread tention adjustment system."
//        ),
//        TicketChecklist (
//            "Machine head"
//        ),
//        TicketChecklist (
//            "Machine head"
//        )
//    )
//
//    fun updateTicketChecklist(ticketChecklists: List<TicketChecklist>) {
//        ticketCheckList.clear()
//        ticketCheckList.addAll(ticketChecklists)
//    }

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
}