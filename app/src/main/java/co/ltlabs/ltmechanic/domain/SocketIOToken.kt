package co.ltlabs.ltmechanic.domain

data class SocketIOToken (
    val userId: Int = 1,
    val token: String = "sample_token",
    val username: String = "clare"
)