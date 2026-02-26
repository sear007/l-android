package co.ltlabs.ltmechanic.domain

class AuthDetails (
    val token: String,
    val role: String,
    val loginSucces: Boolean,
    val username: String
)