package co.ltlabs.ltmechanic.domain.request

class DeviceTokenRequest(
	val uuid: String? = null,
	val token: String? = null,
	val region: String? = null,
	val os: String = "android"
)
