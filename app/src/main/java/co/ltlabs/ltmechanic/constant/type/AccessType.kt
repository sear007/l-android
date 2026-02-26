package co.ltlabs.ltmechanic.constant.type

sealed class AccessType(val code: String) {

    companion object {
        const val CANCEL_TICKET = "cancel_ticket"
        const val FULL_ACCESS = "FULL ACCESS"
        const val EDIT = "EDIT"
        const val VIEW = "VIEW"
        const val NONE = "NONE"

        fun convertCodeToType(code: String): AccessType {
            return when (code) {
                FULL_ACCESS -> FullAccess
                EDIT -> Edit
                VIEW -> View
                else -> None
            }
        }

    }

    object FullAccess : AccessType(FULL_ACCESS)
    object Edit : AccessType(EDIT)
    object View : AccessType(VIEW)
    object None : AccessType(NONE)

}