package co.ltlabs.ltmechanic.domain

import co.ltlabs.ltmechanic.constant.type.AccessType
import com.google.gson.annotations.SerializedName

data class PerAccessResponse(

	@field:SerializedName("data")
	val data: Data? = null,

	@field:SerializedName("success")
	val success: Boolean? = null
)

data class Data(

	@field:SerializedName("factory")
	val factory: String? = null,

	@field:SerializedName("role")
	val role: String? = null,

	@field:SerializedName("access")
	val access: List<AccessItem?>? = null,

	@field:SerializedName("isSuperAdmin")
	val isSuperAdmin: String? = null
)

data class AccessItem(

	@field:SerializedName("Action")
	val action: String? = null,

	@field:SerializedName("role")
	val role: String? = null,

	@field:SerializedName("pageAccess")
	val access: AccessType? = null,

	@field:SerializedName("page")
	val page: String? = null,

	@field:SerializedName("buttonAccess")
	val buttonAccess: String? = null,

	@field:SerializedName("pageId")
	val pageId: Int? = null,

	@field:SerializedName("isActive")
	val isActive: String? = null,

	@field:SerializedName("pageName")
	val pageName: String? = null
)
