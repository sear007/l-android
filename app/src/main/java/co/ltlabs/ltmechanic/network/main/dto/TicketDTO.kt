package co.ltlabs.ltmechanic.network.main.dto

import co.ltlabs.ltmechanic.domain.MachineAvailable
import co.ltlabs.ltmechanic.domain.MachineHistory
import co.ltlabs.ltmechanic.network.TicketAsset
import co.ltlabs.ltmechanic.util.toYesNo
import com.google.gson.annotations.SerializedName
import java.util.*
import co.ltlabs.ltmechanic.domain.ClosedTicket as ClosedTicketDomain
import co.ltlabs.ltmechanic.domain.CommonProblem as CommonProblemDomain
import co.ltlabs.ltmechanic.domain.InRepairTicket as InRepairicketDomain
import co.ltlabs.ltmechanic.domain.LatestProblem as LatestProblemDomain
import co.ltlabs.ltmechanic.domain.MachineMaintenance as MachineMaintenanceDomain
import co.ltlabs.ltmechanic.domain.Problem as ProblemDomain
import co.ltlabs.ltmechanic.domain.RepairedTicket as RepairedTicketDomain
import co.ltlabs.ltmechanic.domain.ReportedTicket as ReportedTicketDomain
import co.ltlabs.ltmechanic.domain.Ticket as TicketDomain
import co.ltlabs.ltmechanic.domain.TicketAsset as TicketAssetDomain
import co.ltlabs.ltmechanic.domain.TicketRepairHistory as TicketRepairHistoryDomain

data class CommonProblem(
    val problemTypeId: Long?,
    val machineId: Long?,
    val desc1: String?,
    val count: Long?
)

data class LatestProblem(
    val problemTypeId: Long?,
    val machineId: Long,
    val desc1: String?,
    val reportedDt: Date?
)

data class Problem(
    val problemTypeId: Long?,
    val desc1: String?
)

data class Ticket(
    val ticketNo: String,
    val machineId: Long,
    val ticketTypeId: Int,
    val moduleId: Int,
    val statusId: Int,
    val problemTypeId: Long?,
    val solutionTypeId: Long?,
    val remarks: String?,
    val checkListId: Int,
    val id: Long,
    val reportedBy: String,
    val reportedDt: Date?,
    val grabbedBy: String?,
    val grabbedDt: Date?,
    val repairedBy: String?,
    val repairedDt: Date?,
    val reopenedBy: String?,
    val reopenedDt: Date?,
    val closedBy: String?,
    val closedDt: Date?,
    val canceledBy: String?,
    val canceledDt: Date?,
    val machineNo: String,
    val place: String?,
    val reportedPlace: String?,
    val modelNo: String,
    val lr_date: String?,
    val lpm_date: Date?,
    val type: String?,
    val subType: String?,
    val brand: String,
    val motorType: String,
    val serialNo: String,
    val pmFrequency: String,
    val isRental: Any,
    val ticketType: String,
    val status: String,
    val problemType: String?,
    val solutionType: String?,
    val createdDt: Date?,
    val updatedDt: Date?,
    val elapsedDuration: String?,
    val inrepairDuration: String?,
    val nextMaintDate: Date?,
)

data class ReportedTicket (
    val ticketId: Long,
    val ticketNo: String,
    val machineNo: String,
    val problem_desc1: String?,
    val solution_desc1: String?,
    val status_desc1: String,
    val lastPMDt: String?,
    val reportedBy: String,
    val reportedDt: Date?,
    val grabbedDt: String,
    val closedDt: Date?,
    val repairedDt: Date?,
    val elapsedDuration: String,
    val reopenedBy: String?,
    val reopenedDt: Date?,
    var place: String?,
    var reportedPlace: String?
)

data class InRepairTicket (
    val ticketId: Long,
    val ticketNo: String,
    val machineNo: String,
    val problem_desc1: String?,
    val solution_desc1: String?,
    val status_desc1: String,
    val lastPMDt: Date?,
    val grabbedBy: String?,
    val grabbedDt: Date?,
    var place: String?,
    var reportedPlace: String?
)

data class RepairedTicket (
    val ticketId: Long,
    val ticketNo: String,
    val machineNo: String,
    val problem_desc1: String?,
    val solution_desc1: String?,
    val status_desc1: String,
    val lastPMDt: Date?,
    val repairedBy: String?,
    val repairedDt: Date?,
    var place: String?,
    var reportedPlace: String?
)

data class ClosedTicket (
    val ticketId: Long,
    val ticketNo: String,
    val machineNo: String,
    val problem_desc1: String?,
    val solution_desc1: String?,
    val status_desc1: String,
    val lastPMDt: Date?,
    val closedBy: String?,
    val closedDt: Date?,
    var place: String?,
    var reportedPlace: String?
)

data class TaskList (
    val task: Task?,
    val ticketSubTasks: List<SubTask>?
)

data class Task (
    val ticketId: Long,
    val checkListTaskId: Int,
    val isComplete: Any,
    val id: Long,
    val createdBy: String,
    val createdDt: Date?,
    val updatedBy: String?,
    val updatedDt: Date?,
    val task: String?,
    val desc1: String?
)

data class SubTask (
    val ticketTaskId: Long,
    val checkListSubTaskId: Long,
    val isComplete: Any,
    val id: Long,
    val createdBy: String,
    val createdDt: Date?,
    val updatedBy: String?,
    val updatedDt: Date?,
    val desc1: String?
)

data class MachineRepairedHistory (
    val ticketId: Long?,
    val ticketNo: String?,
    val machineNo: String?,
    val problem_desc1: String?,
    val solution_desc1: String?,
    val status_desc1: String?,
    val lastPMDt: Date?,
    val repairedBy: String?,
    val remarks: String?,
    val repairedDt: Date?,
    val closedDt: Date?
)

//data class TicketRepairHistory (
//    val ticketId: Int?,
//    val ticketNo: String?,
//    val machineNo: String?,
//    val problem_desc1: String?,
//    val solution_desc1: String?,
//    val status_desc1: String?,
//    val lastPMDt: String?,
//    val repairedBy: String?,
//    val remarks: String?,
//    val repairedDt: String?,
//    val closedDt: String?
//)

data class TicketRepairHistory (
    val ticketNo: String?,
    val machineId: Int?,
    val ticketTypeId: Int?,
    val moduleId: Long?,
    val statusId: Long?,
    val problemTypeId: Int?,
    val solutionTypeId: Int?,
    val remarks: String?,
    val checkListId: Long?,
    val areaId: Long?,
    val mfgLineId: Int?,
    val station: String?,
    val id: Long?,
    val reportedBy: String?,
    val reportedDt: Date?,
    val grabbedBy: String?,
    val grabbedDt: Date?,
    val repairedBy: String?,
    val repairedDt: Date?,
    val closedBy: String?,
    val closedDt: Date?,
    val canceledBy: String?,
    val canceledDt: Date?,
    val machineNo: String?,
    val place: String?,
    val modelNo: String?,
    val lr_date: String?,
    val lpm_date: String?,
    val type: String?,
    val subType: String?,
    val brand: String?,
    val motorType: String?,
    val serialNo: String?,
    val pmFrequency: String?,
    val ticketType: String?,
    val status: String?,
    val aquisitionDt: Date?,
    val assetNo: String?,
    val isRental: Any,
    val isActive: Any,
    val rfid: String?,
    val supplierId_desc: String?,
    val conditionId_desc: String?,
    val attachmentId_desc: String?,
    val problemType: String?,
    val solutionType: String?,
    val checkListNo: String?
)

data class MachineAreaAvailable (
    val area: String,
    val machineNo: String,
    val lastPMDt: Date?,
    val status_desc1: String,
    val subType: String,
    val brand_desc1: String,
    val attachment: String?
)

data class MachineMaintenance (
    val ticketId: Long,
    val ticketNo: String,
    val machineId: Int,
    val machineNo: String,
    val place: String,
    val pmPlanDt: Date,
    val status: String,
    val updatedBy: String,
    val updatedDt: Date
)

data class ReopenTag (
    val value: String
)

fun List<MachineMaintenance>.asMachineMaintenanceDomainModel(): List<MachineMaintenanceDomain> {
    return map {
        MachineMaintenanceDomain (
            id = it.ticketId,
            ticketNo = it.ticketNo,
            machineID = it.machineId,
            machineNo = it.machineNo,
            machineLocation = it.place,
            npmDate = it.pmPlanDt,
            ticketStatus = it.status,
            date = it.updatedDt,
            lastUpdatedBy = it.updatedBy
        )
    }
}

fun List<InRepairTicket>.asInRepairTicketDomainModel(): List<InRepairicketDomain> {
    return map {
        InRepairicketDomain (
            it.ticketId,
            it.ticketNo,
            it.machineNo,
            it.grabbedBy ?: "",
            it.status_desc1,
            it.grabbedDt,
            "",
            it.place,
            it.reportedPlace
        )
    }
}

fun List<RepairedTicket>.asRepairedTicketDomainModel(): List<RepairedTicketDomain> {
    return map {
        RepairedTicketDomain (
            it.ticketId,
            it.ticketNo,
            it.machineNo,
            it.repairedBy ?: "",
            it.status_desc1,
            it.repairedDt,
            "",
            it.place,
            it.reportedPlace
        )
    }
}

fun List<ClosedTicket>.asClosedTicketDomainModel(): List<ClosedTicketDomain> {
    return map {
        ClosedTicketDomain (
            it.ticketId,
            it.ticketNo,
            it.machineNo,
            it.closedBy ?: "",
            it.status_desc1,
            it.closedDt,
            "",
            it.place,
            it.reportedPlace
        )
    }
}

fun List<MachineAreaAvailable>.asMachineAvailableDomainModel(): List<MachineAvailable> {
    return map {
        MachineAvailable (
            it.machineNo,
            it.lastPMDt,
            it.status_desc1,
            it.subType,
            it.brand_desc1,
            it.attachment ?: ""
        )
    }
}

fun List<ReportedTicket>.asReportedTicketDomainModel(): List<ReportedTicketDomain> {
    return map {
        ReportedTicketDomain (
            it.ticketId,
            it.ticketNo,
            it.machineNo,
            it.reopenedBy ?: it.reportedBy,
            it.status_desc1,
            it.reopenedDt ?: it.reportedDt,
            "",
            it.place,
            it.reportedPlace
        )
    }
}

fun List<CommonProblem>.asCommonProblemDomainModel(): List<CommonProblemDomain> {
    return map {
        CommonProblemDomain(
            it.problemTypeId ?: 0L,
            it.machineId ?: 0L,
            it.desc1 ?: "",
            it.count ?: 0
        )
    }
}

fun List<LatestProblem>.asLatestProblemDomainModel(): List<LatestProblemDomain> {
    return map {
        LatestProblemDomain(
            it.problemTypeId ?: 0,
            it.machineId,
            it.desc1 ?: "",
            it.reportedDt
        )
    }
}

fun List<Problem>.asProblemDomainModel(): List<ProblemDomain> {
    return map {
        ProblemDomain(
            it.problemTypeId ?: 0,
            it.desc1 ?: ""
        )
    }
}

fun List<Ticket>.asTicketDomainModel(): List<TicketDomain> {
    return map {
        TicketDomain (
            id = it.id,
            machineId = it.machineId,
            ticketNo = it.ticketNo,
            machineNo = it.machineNo,
            problem = it.problemType ?: "",
            solution = it.solutionType ?: "",
            subType = it.subType ?: "",
            lpmDate = it.lpm_date,
            maintenanceFreq = it.pmFrequency,
            rental = it.isRental.toYesNo(),
            problemTypeId = it.problemTypeId ?: 0,
            solutionTypeId = it.solutionTypeId ?: 0,
            remarks = it.remarks ?: "",
            status = it.status,
            place = it.place ?: "-",
            reportedPlace = it.reportedPlace ?: "-",
            brand = it.brand,
            closedDt = it.closedDt,
            closedBy = it.closedBy ?: "",
            createdDt = it.reportedDt,
            createdBy = it.reportedBy ?: "",
            grabbedDt = it.grabbedDt,
            grabbedBy = it.grabbedBy ?: "",
            updatedDt = it.updatedDt,
            repairedDt = it.repairedDt,
            elapsedDuration = it.elapsedDuration ?: "",
            reported = it.reportedDt,
            repairedBy = it.repairedBy,
            inrepairDuration = it.inrepairDuration,
            nextMaintDate = it.nextMaintDate
        )
    }
}

fun List<TicketAsset>.asTicketAssetDomainModel(): List<TicketAssetDomain> {
    return map {
        TicketAssetDomain (
            it.id,
            it.assetLink
        )
    }
}

fun List<MachineRepairedHistory>.asMachineHistoryDomainModel(): List<MachineHistory> {
    return map {
        MachineHistory (
            it.ticketNo ?: "",
            it.problem_desc1 ?: "",
            it.solution_desc1 ?: "",
            it.remarks ?: "",
            it.status_desc1 ?: "",
            if (it.status_desc1 == "REPAIRED") it.repairedDt else it.closedDt,
            it.repairedBy ?: ""
        )
    }
}

fun List<TicketRepairHistory>.asTicketRepairHistoryDomainModel(): List<TicketRepairHistoryDomain> {
    return map {
        TicketRepairHistoryDomain (
            it.ticketNo ?: "",
            it.problemType ?: "",
            it.solutionType ?: "",
            it.remarks ?: "",
            it.status ?: "",
            date = when (it.status) {
                "REPAIRED" -> it.repairedDt
                "CLOSED" -> it.closedDt
                "IN-REPAIR", "IN REPAIR" -> it.grabbedDt
                else -> it.reportedDt
            },
            username = when (it.status) {
                "REPAIRED" -> it.repairedBy
                "CLOSED" -> it.closedBy
                "IN-REPAIR", "IN REPAIR" ->  it.grabbedBy
                else -> it.reportedBy
            } ?: ""
        )
    }
}