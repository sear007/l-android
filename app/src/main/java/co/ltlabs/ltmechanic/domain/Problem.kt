package co.ltlabs.ltmechanic.domain

class Problem (
    val problemTypeId: Long,
    val desc1: String,
    var checked: Boolean? = true
) {
    override fun toString(): String {
        return desc1
    }
}