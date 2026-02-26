package co.ltlabs.ltmechanic.network.main.dto

import co.ltlabs.ltmechanic.domain.RequestType
import co.ltlabs.ltmechanic.domain.StatusId
import co.ltlabs.ltmechanic.network.StatusResponse
import java.util.*
import co.ltlabs.ltmechanic.domain.SolutionType as SolutionTypeDomain
import co.ltlabs.ltmechanic.domain.NonLineArea as NonLineAreaDomain
import co.ltlabs.ltmechanic.domain.Factory as FactoryDomain

data class SolutionType (
    val factoryId: Int,
    val solutionType: String,
    val desc1: String?,
    val desc2: String?,
    val ticketTypeId: Int?,
    val macTypeId: Int?,
    val macSubTypeId: Int?,
    val problemTypeId: Int?,
    val isActive: Any,
    val id: Long,
    val createdBy: String?,
    val createdDt: Date?,
    val updatedBy: String?,
    val updatedDt: Date?
)

data class NonLineArea (
    val floorId: Int,
    val area: String,
    val seq: Int,
    val desc1: String?,
    val desc2: String?,
    val areaTypeId: Int,
    val isLineReq: Any,
    val isActive: Any,
    val id: Long,
    val createdBy: String?,
    val createdDt: Date?,
    val updatedBy: String?,
    val updatedDt: Date?
)

data class ProductConfig(
    val id: Long,
    val value: String,
    val config: String?
)

data class Factory (
    val factoryId: Long,
    val name: String
)

fun List<Factory>.asFactoryDomainModel(): List<FactoryDomain> {
    return map {
        FactoryDomain (
            it.factoryId,
            it.name
        )
    }
}

fun List<ProductConfig>.asRequestTypeDomainMode(): List<RequestType> {
    return map {
        RequestType(
            it.id,
            it.value,
            it.value
        )
    }
}

fun List<NonLineArea>.asNonLineAreaDomainModel(): List<NonLineAreaDomain> {
    return map {
        NonLineAreaDomain (
            it.id,
            it.area,
            it.areaTypeId,
            it.desc1 ?: ""
        )
    }
}

fun List<SolutionType>.asSolutionTypeDomainModel(): List<SolutionTypeDomain> {
    return map {
        SolutionTypeDomain (
            it.id,
            if (it.desc1 == null || it.desc1.isBlank()) it.solutionType else it.desc1,
            it.desc1 ?: ""
        )
    }
}

fun StatusResponse.asStatusIdDomainModel(): StatusId {
    return StatusId(
        statusId = this.statusId
    )
}