package co.ltlabs.ltmechanic.domain

class LoginDetails (
    val username: String,
    val role: String,
    val token: String,
    val loggedIn: Boolean,
    val tokenP: String
)