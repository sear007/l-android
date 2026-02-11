package co.ltlabs.ltmechanic.domain

import com.google.gson.annotations.SerializedName

data class AreasByBuildingResponse(

    @field:SerializedName("data")
    val data: List<AreaItem>? = null,

    @field:SerializedName("success")
    val success: Boolean? = null
)

data class AreaItem(

    @field:SerializedName("area")
    val area: String? = null,

    @field:SerializedName("areaName")
    val areaName: String? = null,

    @field:SerializedName("areaDesc")
    val areaDesc: String? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    var isChecked: Boolean = false
)
