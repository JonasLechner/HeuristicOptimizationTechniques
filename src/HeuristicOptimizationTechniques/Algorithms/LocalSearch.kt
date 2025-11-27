package HeuristicOptimizationTechniques.Algorithms

import HeuristicOptimizationTechniques.Algorithms.Neighborhoods.Neighborhood
import HeuristicOptimizationTechniques.Helper.StopConditionGuard
import HeuristicOptimizationTechniques.Helper.Solution
import HeuristicOptimizationTechniques.Helper.StepFunction
import HeuristicOptimizationTechniques.Helper.StopCondition


class LocalSearch(
    private val neighborhood: Neighborhood,
    private val stepFunction: StepFunction = StepFunction.BEST_IMPROVEMENT,
    private val stopCondition: StopCondition
) : ImprovementHeuristic {

    override fun improve(solution: Solution): Solution {
        var bestSolution: Solution = solution

        val conditionGuard = StopConditionGuard(stopCondition)

        while (conditionGuard.shouldContinue()) {
            val neighbors: List<Solution> =
                neighborhood.createNeighbors(bestSolution)

            if (neighbors.isEmpty()) {
                return bestSolution
            }

            when (stepFunction) {
                StepFunction.FIRST_IMPROVEMENT -> {
                    val firstBest = neighbors
                        .firstOrNull { solution -> solution.totalCost < bestSolution.totalCost }

                    if (firstBest != null) {
                        bestSolution = firstBest
                    } else {
                        //local max
                        return bestSolution
                    }
                }

                StepFunction.BEST_IMPROVEMENT -> {
                    val bestInNeighborhood =
                        neighbors.minBy { solution -> solution.totalCost }

                    if (bestInNeighborhood.totalCost < bestSolution.totalCost) {
                        bestSolution = bestInNeighborhood
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

        return bestSolution
    }
}