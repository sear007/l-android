package co.ltlabs.ltmechanic.network

data class MachineCheckInRequest (
    val id: Long,
    val station: String,
    val mfgLineId: Long?,
    val updatedDt: String
)

data class MachineCheckOutRequest (
    val updatedDt: String
)

data class MachineLineCheckOutRequest (
    val id: Long,
    val updatedDt: String,
    val areaId: Int? = null
)

data class LineAssignRequest (
    val lines: List<LineRequest>
)

data class ClearLineRequest (
    val mfgLineId: Long,
    val updatedDt: String
)

data class LineRequest (
    val mfgLineId: Long,
    val createdDt: String
)

data class MachineInsertRequest (
    val id: Long? = null,
    val station: String,
    val mfgLineId: Long,
    val updatedDt: String
)

data class CreateTicketRequest (
    val machineId: String,
    val problemTypeId: String?,
    val solutionTypeId: String?,
    val remarks: String,
    val reportedDt: String,
    val assets: List<Asset>? = null
)

data class UpdateTicketStatusRequest (
    val tickets: List<TicketNo>,
    val ticketType: String,
    val statusId: String,
    val problemTypeId: String?,
    val solutionTypeId: String?,
    val remarks: String,
    val assets: List<Asset>?
)

data class ReopenTicketRequest (
    val tickets: List<TicketNo>
//    val remarks: String = ""
)

data class UpdateChecklistRequest (
    val checklists: List<Tasks>,
    val ticketNo: String? = null,
    val remarks: String? = null
)

data class Tasks (
    var task: TaskRequest,
    val ticketSubTasks: List<SubTaskRequest>?
)

data class TaskRequest (
    var isComplete: Int,
    var id: Long
)

data class SubTaskRequest (
    var isComplete: Int,
    var id: Long
)

data class TicketNo (
    val no: String
)

data class Asset (
    val link: String
)

data class FirebaseSendNotificationRequest (
    val message: FCMMessage
)

data class FCMMessage (
    val topic: String,
    val data: FCMData
)

data class FCMData (
    val title: String,
    val content: String
)

data class TicketStatisticsRequest(
    val linesSelected: List<String>
)

data class DashboardStatisticsRequest(
    val linesSelected: List<String>? = null,
    val areasSelected: List<String>? = null
)

data class LoginRequest (
    val username: String,
    val password: String,
    val factoryId: Long? = null
)

data class MoveMachineRequest (
    val id: Long,
    val areaId: Long,
    val buildingId: Long
)

data class AttachMachineNFCRequest (
    val rfid: String
)

data class SendRequestRequest (
    val mfgLineId: Long,
    val machineId: Long,
    val requestMsg: String
)

data class ChangePasswordRequest (
    val currentPassword: String,
    val password: String
)

data class AppStore(
    val appName: String
)

class SaveAreasNoLines(
    val areas: List<Int>
)