package co.ltlabs.ltmechanic.network

import co.ltlabs.ltmechanic.domain.TicketLogs
import co.ltlabs.ltmechanic.network.auth.dto.LoginAccess
import co.ltlabs.ltmechanic.network.main.dto.*

data class MachineResponse(
    val success: Boolean,
    val machine: List<Machine>,
    val hasOpenTicket: String?,
    val ticketNos: List<MachineTicketNo>?,
    val message: String?
)

data class MachineTicketNo(
    val ticketNo: String
)

data class MachinesResult(
    val result: List<Machine>
)

data class MachinesResponse(
    val success: Boolean,
    val machines: MachinesResult
)

data class MachinesStationResponse(
    val success: Boolean,
    val machine: List<Machine>
)

data class MachinesInStationResponse(
    val success: Boolean,
    val machines: List<Machine>
)

data class MachineCheckInResponse(
    val success: Boolean,
    val machine: UpdatedRow
)

data class MachineCheckOutResponse(
    val success: Boolean,
    val machine: UpdatedRow
)

data class LinesResponse(
    val success: Boolean,
    val lines: List<MfgLine>
)

//data class LinesResult (
//    val result:
//)

data class ClearLineResponse(
    val success: Boolean,
    val machine: UpdatedRow,
    val errors: List<ClearLineError>?
)

data class ClearLineValidateResponse(
    val success: Boolean,
    val machine: UpdatedRow?,
    val errors: List<ClearLineError>?
)

data class ClearLineError(
    val mfgLineId: ClearLineErrorMfgLine
)

data class ClearLineErrorMfgLine(
    val message: String,
    val machines: List<ClearLineErrorMachine>
)

data class ClearLineErrorMachine(
    val machine: String,
    var station: String
)

data class LinesAssignResponse(
    val success: Boolean,
    val machine: List<MfgLine>
)

data class MacSubTypeResponse(
    val success: Boolean,
    val machine: List<MacSubType>
)

data class MacSubTypeResult(
    val result: List<MacSubType>
)

data class MachineInsertResponse(
    val success: Boolean,
    val machine: UpdatedRow
)

data class UpdatedRow(
    val affectedRows: Long?,
    val insertId: Long?,
    val warningStatus: Long?
)

data class MachineTicketResponse(
    val success: Boolean,
    val tickets: MachineTicketResult
)

data class MachineTicketResult(
    val Reported: Long?,
    val Reopen: Long?,
    val InRepair: Long?,
    val Repaired: Long?,
    val InProgress: Long?,
    val Scheduled: Long?,
    val Maintenance: Long?,
    val count: Long?,
    val totalMachine: Long?,
    val CORequest: Long? = 0
)

data class MachineProblemResponse(
    val success: Boolean,
    val problems: Problems
)

data class Problems(
    val commonProblems: List<CommonProblem>,
    val latestProblem: List<LatestProblem>,
    val allProblems: List<Problem>
)

data class FileUploadResponse(
    val success: Boolean,
    val files: List<FileResult>
)

data class UpdateChecklistResponse(
    val success: Boolean
)

data class TicketResponse(
    val success: Boolean,
    val tickets: List<Ticket>,
    val assets: List<TicketAsset>,
    val checkist: List<TaskList>?,
    val reopenTag: ReopenTag?,
    val logs: List<TicketLogs>?
)

data class TicketAsset(
    val ticketId: Long,
    val assetLink: String,
    val id: Long,
    val createdBy: String,
    val createdDt: String,
    val updatedBy: String?,
    val updatedDt: String?
)

data class ReportedTicketResponse(
    val success: Boolean,
    val data: List<ReportedTicket>
)

data class InRepairTicketResponse(
    val success: Boolean,
    val data: List<InRepairTicket>
)

data class RepairedTicketResponse(
    val success: Boolean,
    val data: List<RepairedTicket>
)

data class ClosedTicketResponse(
    val success: Boolean,
    val data: List<ClosedTicket>
)

data class SolutionTypeResponse(
    val success: Boolean,
    val solutions: SolutionTypeResult
)

data class SolutionTypeByProblemIdResponse(
    val success: Boolean,
    val solutions: List<SolutionType>
)

data class NonLineAreasResponse(
    val success: Boolean,
    val machine: List<NonLineArea>
)

data class FirebaseSendNotificationResponse(
    val name: String?,
    val error: FCMError?
)

data class FCMError(
    val code: Long,
    val message: String,
    val status: String
)

data class SolutionTypeResult(
    val result: List<SolutionType>
)

data class UpdateTicketStatusResponse(
    val success: Boolean,
    val result: List<TicketResult>
)

data class ReopenTicketResponse(
    val success: Boolean,
    val result: List<TicketResult>
)

data class MachineRepairedHistoryResponse(
    val success: Boolean,
    val data: List<MachineRepairedHistory>
)

data class StorageAreasResponse(
    val success: Boolean,
    val data: List<Area>
)

data class MachineAreaAvailableResponse(
    val success: Boolean,
    val data: List<MachineAreaAvailable>
)

data class TicketResult(
    val ticket: String
)

data class Error(
    val errors: List<ErrorObj>,
    val error: ErrorObj2?
)

data class Error2(
    val error: ErrorObj2?
)

data class Error3(
    val errors: List<ErrorObj3>?
)

data class Error4(
    val success: Boolean?,
    val error: String?
)

data class ErrorObj(
    val id: String?,
    val machineId: String?,
    val remarks: String?,
    val mfgLineId: ClearLineErrorMfgLine?
)

data class ErrorObj2(
    val name: String,
    val message: String,
    val expiredAt: String
)

data class ErrorObj3(
    val rfid: String
)

data class LoginResponse(
    val token: String?,
    val role: String,
    val refresh_token: String?,
    val access: List<LoginAccess>?,
    val error: String?
)

data class LanguagesResponse(
    val success: Boolean,
    val language: List<Language>
)

data class MoveMachineResponse(
    val result: MoveMachineResult
)

data class MoveMachineResult(
    val affectedRows: Long,
    val insertId: Long,
    val warningStatus: Long
)

data class MachineMaintenanceResponse(
    val success: Boolean,
    val data: List<MachineMaintenance>
)

data class MaintenanceHistoryResponse(
    val success: Boolean,
    val tickets: List<Ticket>
)

data class TicketAllHistoryResponse(
    val success: Boolean,
    val tickets: TicketAllHistoryResult
)

data class TicketAllHistoryResult(
    val result: Ticket
)

data class AttachMachineNFCResponse(
    val success: Boolean
)

data class ProductConfigResponse(
    val success: Boolean,
    val data: List<ProductConfig>
)

data class SendRequestResponse(
    val success: Boolean
)

data class TicketRepairHistoryResponse(
    val success: Boolean,
    val tickets: List<TicketRepairHistory>
)

data class ChangePasswordResponse(
    val result: String
)

data class FactoryResponse(
    val success: Boolean,
    val data: List<Factory>
)

data class StatusResponse(
    val success: Boolean,
    val statusId: Long
)

data class ErrorResponse(
    val code: String,
    val errno: Int,
    val message: String
)

data class AppStoreInfo(
    val desc1: String,
    val downloadLink: String,
    val fileName: String,
    val latestVersion: String,
    val serverLink: String
)