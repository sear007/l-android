package co.ltlabs.ltmechanic.network.main.dto

import co.ltlabs.ltmechanic.domain.MacSubType
import co.ltlabs.ltmechanic.domain.Machine
import co.ltlabs.ltmechanic.domain.MachineInStation
import co.ltlabs.ltmechanic.network.*
import co.ltlabs.ltmechanic.util.toYesNo
import java.util.*

data class Machine(
    val factoryId: Long?,
    val areaId: Long?,
    val machine: String = "",
    val station: String = "",
    val mfgLineId: Long?,
    val model: String = "",
    val macTypeId: Int?,
    val macSubTypeId: Long?,
    val assetNo: String? = "",
    val brandId: Long?,
    val motorTypeId: Long?,
    val serialNo: String = "",
    val supplierId: Int?,
    val isRental: Any?,
    val aquisitionDt: Date?,
    val pmFrequency: String? = "",
    val pmPlanDt: Date?,
    val lastPMDt: Date?,
    val lastReportedDt: Date?,
    val lastRepairedDt: Date?,
    val moduleId: Int?,
    val statusId: Int?,
    val conditionId: Int?,
    val attachmentId: Int?,
    val barcode: String?,
    val rfid: String?,
    val isActive: Any = 0,
    val id: Long?,
    val areaId_desc: String?,
    val mfgLineId_desc: String?,
    val updatedBy: String?,
    val updatedDt: Date?,
    var macSubTypeId_desc: String?,
    val brandId_desc: String?,
    val statusId_desc: String,
    val conditionId_desc: String?,
    val frequencyId_desc: String?,
    val building: String? = null,
    val buildingId: Int = 0
)

data class MacSubType(
    val macTypeId: Int,
    val subType: String,
    val desc1: String?,
    val desc2: String?,
    val isActive: Boolean,
    val id: Long,
    val createdBy: String,
    val createdDt: String,
    val updatedBy: String?,
    val updatedDt: String?
)

fun MachinesResult.asDomainModel(): List<Machine> {
    return result.map {
        Machine(
            machine = it.machine,
            station = it.station,
            rfid = it.rfid,
            areaId=it.areaId,
            id = it.id ?: 0,
            macSubTypeId = it.macSubTypeId,
            subtype = it.macSubTypeId_desc ?: "",
            area = it.areaId_desc ?: "",
            mfgLineId = it.mfgLineId,
            mfgLine = it.mfgLineId_desc,
            brandId = it.brandId ?: 0,
            status = it.statusId_desc,
            brand = it.brandId_desc ?: "",
            lastRepairedDt = it.lastRepairedDt,
            supplierDt = it.aquisitionDt,
            condition = it.conditionId_desc ?: "",
            lastPMDt = it.lastPMDt,
            maintenanceFreq = it.frequencyId_desc ?: "",
            rental = it.isRental?.toYesNo() ?: "NO",
            building = it.building,
            buildingId = it.buildingId
        )
    }
}

fun MachineResponse.asDomainModel(): List<Machine> {
    return machine.map {
        Machine(
            machine = it.machine,
            station = it.station,
            rfid = it.rfid,
            areaId=it.areaId,
            id = it.id ?: 0,
            macSubTypeId = it.macSubTypeId,
            subtype = it.macSubTypeId_desc,
            area = it.areaId_desc ?: "",
            mfgLineId = it.mfgLineId,
            mfgLine = it.mfgLineId_desc,
            brandId = it.brandId ?: 0,
            status = it.statusId_desc,
            brand = it.brandId_desc ?: "",
            lastRepairedDt = it.lastRepairedDt,
            supplierDt = it.aquisitionDt,
            condition = it.conditionId_desc ?: "",
            lastPMDt = it.lastPMDt,
            maintenanceFreq = it.frequencyId_desc ?: "",
            rental = it.isRental?.toYesNo() ?: "NO",
            building = it.building,
            buildingId = it.buildingId
        )
    }
}

fun MachinesStationResponse.asMachineInStationDomainModel(): List<MachineInStation> {
    return machine.map {
        MachineInStation(
            it.id ?: 0,
            it.station,
            it.machine,
            it.rfid ?: "",
            it.macSubTypeId_desc ?: "",
            building = it.building,
            buildingId = it.buildingId
        )
    }
}

fun MachinesInStationResponse.asMachineInStationDomainModel(): List<MachineInStation> {
    return machines.map {
        MachineInStation(
            it.id ?: 0,
            it.station,
            it.machine,
            it.rfid ?: "",
            it.macSubTypeId_desc ?: ""
        )
    }
}

fun MacSubTypeResult.asDomainModel(): List<MacSubType> {
    return result.map {
        MacSubType(it.subType)
    }
}



