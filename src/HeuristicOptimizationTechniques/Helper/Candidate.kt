package HeuristicOptimizationTechniques.Helper

data class Candidate(
    val requestId: Int,
    val routeIndex: Int,
    val pickPos: Int,
    val dropPos: Int
)
