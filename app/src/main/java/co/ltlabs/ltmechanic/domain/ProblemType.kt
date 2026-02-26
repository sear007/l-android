package co.ltlabs.ltmechanic.domain

class ProblemType (
    val id: Int,
    val solutionType: String,
    val desc: String
) {
    override fun toString(): String {
        return desc
    }
}