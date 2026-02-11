package co.ltlabs.ltmechanic.domain

data class CompanyInfoResponse(
    val appName: String,
    val baseURL: String,
    val globalBaseURL: String,
    val company: String,
    val companyName: String,
    val id: Int,
    val socketURL: String
)
