package co.ltlabs.ltmechanic.constant.type

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class COStatusType(
    val code: Int,
    val status: String,
    val colorCode: String
) : Parcelable {

    companion object {
        const val NEW = "NEW"
        const val IN_PROGRESS = "IN PROGRESS"
        const val READY = "READY"
        const val CLOSED = "CLOSED"

        const val NEW_CODE = 1
        const val IN_PROGRESS_CODE = 2
        const val READY_CODE = 3
        const val CLOSED_CODE = 0

        fun fromStringToType(statusDesc: String?): COStatusType {
            return when (statusDesc) {
                NEW -> New
                IN_PROGRESS -> InProgress
                READY -> Ready
                else -> Closed
            }
        }

        fun fromCodeToType(status: Int?): COStatusType {
            return when (status) {
                NEW_CODE -> New
                IN_PROGRESS_CODE -> InProgress
                READY_CODE -> Ready
                else -> Closed
            }
        }
    }

    @Parcelize
    object New : COStatusType(NEW_CODE, NEW, "#4CAF50")
    @Parcelize
    object InProgress : COStatusType(IN_PROGRESS_CODE, IN_PROGRESS, "#F57C00")
    @Parcelize
    object Ready : COStatusType(READY_CODE, READY, "#D167FF")
    @Parcelize
    object Closed : COStatusType(CLOSED_CODE, CLOSED, "#FFFFFF")

}
