package HeuristicOptimizationTechniques.Algorithms

import HeuristicOptimizationTechniques.Algorithms.Neighborhoods.Neighborhood
import HeuristicOptimizationTechniques.Helper.Logger
import HeuristicOptimizationTechniques.Helper.Solution
import HeuristicOptimizationTechniques.Helper.StepFunction


class LocalSearch(
    private val neighborhood: Neighborhood,
    private val stepFunction: StepFunction = StepFunction.BEST_IMPROVEMENT,
    private val maxIterations: Int = 15
) : ImprovementHeuristic {
    private val logger = Logger.getLogger(LocalSearch::class.java.simpleName)

    override fun improve(solution: Solution): Solution {
        var bestSolution: Solution = solution

        for (i in 1..maxIterations) {
            val neighbors: List<Solution> =
                neighborhood.createNeighborhood(bestSolution)

            if (neighbors.isEmpty()) {
                return bestSolution
            }

            when (stepFunction) {
                StepFunction.FIRST_IMPROVEMENT -> {
                    val best = neighbors
                        .firstOrNull { solution -> solution.totalCost < bestSolution.totalCost }

                    if (best != null) {
                        bestSolution = best
                    } else {
                        //local max
                        return bestSolution
                    }
                }

                StepFunction.BEST_IMPROVEMENT -> {
                    val best =
                        neighbors.minBy { solution -> solution.totalCost }

                    if (best.totalCost < bestSolution.totalCost) {
                        bestSolution = best
                    } else {
                        //local max
                        return bestSolution
                    }
                }

                StepFunction.RANDOM -> {
                    bestSolution = neighbors.random()
                }
            }
        }
        logger.info("Found solution with cost ${bestSolution.totalCost}.")
        return bestSolution
    }
}