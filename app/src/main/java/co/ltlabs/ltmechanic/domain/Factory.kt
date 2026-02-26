package co.ltlabs.ltmechanic.domain

class Factory (
    val factoryId: Long,
    val name: String
) {
    override fun toString(): String {
        return name
    }
}