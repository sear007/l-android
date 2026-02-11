package co.ltlabs.ltmechanic.domain

import android.os.Parcelable
import co.ltlabs.ltmechanic.database.DatabaseMfgLine
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MfgLine(
    val mfgLineId: Long,
    val mfgLine: String,
    @SerializedName("desc1")
    val mfgLineName: String,
    var seq: Int?,
    var checked: Boolean? = true,
    var username: String = ""
) : Parcelable

fun Array<MfgLine>.asDatabaseModel(): Array<DatabaseMfgLine> {
    return map {
        DatabaseMfgLine(
            mfgLineId = it.mfgLineId,
            mfgLine = it.mfgLine,
            mfgLineName = it.mfgLineName,
            seq = it.seq ?: 0,
            checked = it.checked ?: false,
            username = it.username
        )
    }.toTypedArray()
}