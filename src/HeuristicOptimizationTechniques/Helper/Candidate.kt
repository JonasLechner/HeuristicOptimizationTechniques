package HeuristicOptimizationTechniques.Helper

//candidate for insertion, will be applied to solution
data class Candidate(
    val requestId: Int,
    val routeIndex: Int, //index OF the route
    val pickPos: Int, //index IN the route, where pickup will be inserted
    val dropPos: Int  //index IN the route, where dropoff will be inserted
)
