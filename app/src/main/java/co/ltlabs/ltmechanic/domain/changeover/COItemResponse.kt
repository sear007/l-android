package co.ltlabs.ltmechanic.domain.changeover

import com.google.gson.annotations.SerializedName

data class COItemResponse(

	@field:SerializedName("corequest")
	val item: OperationItem? = null
)
