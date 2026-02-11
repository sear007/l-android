package co.ltlabs.ltmechanic.network

import com.google.gson.annotations.SerializedName

data class SwitchFactoryResponse(

	@field:SerializedName("role")
	val role: String? = null,

	@field:SerializedName("accessToken")
	val accessToken: String? = null,

	@field:SerializedName("userId")
	val userId: Int? = null,

	@field:SerializedName("status")
	val status: Int? = null,

	@field:SerializedName("refreshToken")
	val refreshToken: String? = null
)
