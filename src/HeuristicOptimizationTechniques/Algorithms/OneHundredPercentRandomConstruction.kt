package HeuristicOptimizationTechniques.Algorithms

import HeuristicOptimizationTechniques.Helper.Candidate
import HeuristicOptimizationTechniques.Helper.Instance
import HeuristicOptimizationTechniques.Helper.Solution
import kotlin.random.Random

class OneHundredPercentRandomConstruction(
    private val instance: Instance,
    private val rng: Random,
) : ConstructionHeuristic {
    override fun construct(): Solution {
        val solution = Solution(instance)

        val random = (1..instance.numberOfRequests).shuffled(rng).take(instance.minNumberOfRequestsFulfilled)

        for ((idx, request) in random.withIndex()) {
            if (idx < instance.numberOfVehicles) {
                solution.routes.add(mutableListOf())
                instance.applyCandidateToSolution(
                    solution,
                    Candidate(request, solution.routes.size - 1, 0, 1)
                )
                continue
            }

            instance.applyCandidateToSolution(
                solution,
                Candidate(request, solution.routes.indices.random(rng), 0, 1)
            )
        }

        return solution
    }

}