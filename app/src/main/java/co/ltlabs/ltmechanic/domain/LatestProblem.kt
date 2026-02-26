package co.ltlabs.ltmechanic.domain

import java.util.*

data class LatestProblem (
    val problemTypeId: Long,
    val machineId: Long,
    val desc1: String,
    val reportedDt: Date?
)