package co.ltlabs.ltmechanic.domain

class SolutionType (
    val id: Long,
    val solutionType: String,
    val desc: String
) {
    override fun toString(): String {
        return solutionType
    }
}