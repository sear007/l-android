package co.ltlabs.ltmechanic.domain

data class EmployeeResponse(
    val accessToken: String,
    val refreshToken: String,
    val status: Int,
    val userId: Int,
    val role: String,
    val factory: String,
    val factoryId: Int? = null,
    val username: String
)