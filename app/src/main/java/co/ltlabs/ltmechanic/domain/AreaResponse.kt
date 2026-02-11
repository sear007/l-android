package co.ltlabs.ltmechanic.domain

import android.os.Parcelable
import co.ltlabs.ltmechanic.database.DatabaseMfgArea
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

data class AreaResponse(

    @field:SerializedName("success")
    val success: Boolean? = null,

    @field:SerializedName("areas")
    val areas: List<Areas>? = null
)

@Parcelize
data class Areas(

    @field:SerializedName("floorId")
    val floorId: Int? = null,

    @field:SerializedName("area")
    val area: String? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("description")
    val description: String? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    var username: String? = "",

    @field:SerializedName("isSelected")
    var isSelected: Boolean = false,

) : Parcelable

fun Array<Areas>.asDatabaseModel(): Array<DatabaseMfgArea> {
    return map {
        DatabaseMfgArea(
            mfgFloorId = it.floorId,
            mfgArea = it.area,
            mfgName = it.name,
            mfgDescription = it.description,
            mfgAreaId = it.id,
            mfgUserName=it.username,
            isSelected = it.isSelected,
        )
    }.toTypedArray()
}
