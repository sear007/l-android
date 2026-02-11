package co.ltlabs.ltmechanic.domain

data class CommonProblem (
    val problemTypeId: Long,
    val machineId: Long,
    val desc1: String,
    val count: Long
)