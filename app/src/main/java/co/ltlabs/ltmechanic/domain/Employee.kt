package co.ltlabs.ltmechanic.domain

data class Employee(
    val companyCode: String,
    val username: String,
    val password: String
)

data class RfidRequest(
    val companyCode: String,
    val rfidNo: String
)