package HeuristicOptimizationTechniques.Algorithms

import HeuristicOptimizationTechniques.Helper.Candidate
import HeuristicOptimizationTechniques.Helper.Instance
import HeuristicOptimizationTechniques.Helper.Solution
import kotlin.random.Random

class CompletelyRandomConstruction(
    private val instance: Instance,
    private val rng: Random,
) : ConstructionHeuristic {
    override fun construct(): Solution {
        val random = (1..instance.numberOfRequests).shuffled(rng).take(instance.minNumberOfRequestsFulfilled)

        val solution = Solution(instance)

        for ((idx, request) in random.withIndex()) {
            if (idx < instance.numberOfVehicles) {
                solution.routes.add(mutableListOf())
                instance.applyCandidateToSolution(
                    solution,
                    Candidate(request, solution.routes.size - 1, 0, 1)
                )
                continue
            }
            val closestRouteIdx = solution.routes.withIndex().sortedBy { (_, route)
                ->
                val pickup = instance.getLocationOf(request)
                val lastDrop = instance.getLocationOf(route.last())
                pickup.distance(lastDrop)
            }.take(10).random(rng).index

            val route = solution.routes[closestRouteIdx]
            instance.applyCandidateToSolution(
                solution,
                Candidate(request, closestRouteIdx, route.size, route.size + 1)
            )
        }

        return solution
    }
}