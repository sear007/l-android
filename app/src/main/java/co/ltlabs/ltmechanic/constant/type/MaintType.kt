package co.ltlabs.ltmechanic.constant.type

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class MaintType(
    val code: Int,
    val status: String,
    val colorCode: String
) : Parcelable {

    companion object {
        const val SCHEDULED = "SCHEDULED"
        const val COMPLETED = "COMPLETED"
        const val CANCELLED = "CANCELLED"
        const val WIP = "IN PROGRESS"
        const val CLOSED = "CLOSED"
        const val OVERDUE = "OVERDUE"

        const val SCHEDULED_CODE = 1
        const val WIP_CODE = 2
        const val CANCELLED_CODE = 3
        const val COMPLETED_CODE = 4

        fun fromStringToType(statusDesc: String?): MaintType {
            return when (statusDesc) {
                WIP -> Wip
                SCHEDULED -> Schedule
                COMPLETED -> Completed
                else -> Cancelled
            }
        }

        fun fromCodeToType(status: Int?): MaintType {
            return when (status) {
                WIP_CODE -> Wip
                SCHEDULED_CODE -> Schedule
                COMPLETED_CODE -> Completed
                else -> Cancelled
            }
        }
    }

    @Parcelize
    object Wip : MaintType(WIP_CODE, WIP, "#F57C00")
    @Parcelize
    object Schedule : MaintType(SCHEDULED_CODE, SCHEDULED, "#4CAF50")
    @Parcelize
    object Completed : MaintType(COMPLETED_CODE, COMPLETED, "#019AE8")
    @Parcelize
    object Cancelled : MaintType(CANCELLED_CODE, CANCELLED, "#B9C0C5")

}
