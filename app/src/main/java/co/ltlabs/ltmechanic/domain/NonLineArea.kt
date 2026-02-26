package co.ltlabs.ltmechanic.domain

class NonLineArea (
    val id: Long,
    val areaId: String,
    val areaTypeId: Int,
    val desc: String

) {
    override fun toString(): String {
        return desc
    }
}