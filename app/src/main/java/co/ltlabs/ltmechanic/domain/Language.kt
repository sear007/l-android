package co.ltlabs.ltmechanic.domain

class Language (
    val code: String,
    val language: String,
    val country: String,
    val factory: String
) {
    override fun toString(): String {
        return language
    }
}