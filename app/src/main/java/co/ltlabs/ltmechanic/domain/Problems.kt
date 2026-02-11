package co.ltlabs.ltmechanic.domain

data class Problems (
    val commonProblems: List<CommonProblem>,
    val latestProblem: List<LatestProblem>,
    val problem: List<Problem>
)