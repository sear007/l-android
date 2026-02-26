package co.ltlabs.ltmechanic.domain

import co.ltlabs.ltmechanic.util.SnackBarActions

data class SnackBarAction (
    val id: Int,
    val action: String,
    val show: Boolean
)