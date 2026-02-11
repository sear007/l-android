package co.ltlabs.ltmechanic.constant.type

import android.os.Parcelable
import co.ltlabs.ltmechanic.R
import kotlinx.android.parcel.Parcelize


sealed class IsCriticalMachineType(
    val code: Int,
    val imageRes: Int
) : Parcelable {

    companion object {
        const val YES = 1
        const val NO = 0

        fun fromCodeToType(code: Int): IsCriticalMachineType {
            return when (code) {
                YES -> Yes
                else -> No
            }
        }
    }

    @Parcelize
    object Yes : IsCriticalMachineType(YES, R.drawable.ic_critical)

    @Parcelize
    object No : IsCriticalMachineType(NO, R.drawable.ic_un_critical)

}
