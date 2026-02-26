package co.ltlabs.ltmechanic.domain

import com.google.gson.annotations.SerializedName

data class BuildingResponse(

	@field:SerializedName("data")
	val data: List<BuildingItem>? = null,

	@field:SerializedName("success")
	val success: Boolean? = null
)

data class BuildingItem(

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("isActive")
	val isActive: Boolean? = null,

	@field:SerializedName("building")
	val buildingCode: String? = null,

	@field:SerializedName("buildingDesc")
	val buildingName: String? = null,

	var isChecked: Boolean = false
)
