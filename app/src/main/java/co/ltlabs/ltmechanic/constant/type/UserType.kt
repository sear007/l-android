package co.ltlabs.ltmechanic.constant.type

sealed class UserType(val code: String) {

    companion object {
        const val LINE_LEADER = "Line Leader"
        const val ADMIN = "admin"
        const val MECHANIC = "mechanic"

        fun convertToType(code: String): UserType {
            return when (code) {
                LINE_LEADER -> LineLeader
                ADMIN -> Admin
                else -> Mechanic
            }
        }
    }

    object LineLeader : UserType(LINE_LEADER)
    object Admin : UserType(ADMIN)
    object Mechanic : UserType(MECHANIC)

}