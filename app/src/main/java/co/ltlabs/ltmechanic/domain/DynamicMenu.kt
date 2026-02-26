package co.ltlabs.ltmechanic.domain

data class DynamicMenu(
    val groupId: Int,
    val itemId: Int,
    val order: Int,
    val titleRes: Int,
    val isCheckable: Boolean
)