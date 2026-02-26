package co.ltlabs.ltmechanic.network.main.dto

import co.ltlabs.ltmechanic.domain.MachineLocation
import co.ltlabs.ltmechanic.domain.MfgLine
import co.ltlabs.ltmechanic.network.LinesAssignResponse
import com.google.gson.annotations.SerializedName
import java.util.*

data class MfgLine (
    val areaId: Int?,
    val mfgLine: String?,
    val seq: Int?,
    val desc1: String?,
    val desc2: String?,
    val isActive: Any?,
    val id: Long?,
    val createdBy: String?,
    val createdDt: Date?,
    val updatedBy: String?,
    val updatedDt: Date?,
    val areaId_desc: String?,
    val isSelected: Boolean? = false
)

data class Area (
    val areaId: Long,
    val area: String,
    val totalMachines: Int
)

fun List<Area>.asLocationDomainModel(): List<MachineLocation> {
    return map {
        MachineLocation(
            it.areaId,
            it.area,
            it.totalMachines.toString()
        )
    }
}

fun List<co.ltlabs.ltmechanic.network.main.dto.MfgLine>.asDomainModel(): List<MfgLine> {
    return map {
        MfgLine(
            it.id ?: 0,
            it.mfgLine ?: "",
            it.desc1 ?: "",
            it.seq,
            checked = it.isSelected
        )
    }
}

fun LinesAssignResponse.asDomainModel(): List<MfgLine> {
    return machine.map {
        MfgLine(
            it.id ?: 0,
            it.mfgLine ?: "",
            it.desc1 ?: "",
            null,
            null
        )
    }
}


