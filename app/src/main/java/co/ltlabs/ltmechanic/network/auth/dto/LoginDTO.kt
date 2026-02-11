package co.ltlabs.ltmechanic.network.auth.dto

import co.ltlabs.ltmechanic.domain.LoginAccess as LoginAccessDomain

data class LoginAccess (
    val role: String?,
    val page: String?,
//    val pageId: String,
    val Action: String?,
    val isActive: String?
)

fun List<LoginAccess>.asLoginAccessDomain(): List<LoginAccessDomain> {
    return map {
        LoginAccessDomain (
            it.role ?: ""
        )
    }
}