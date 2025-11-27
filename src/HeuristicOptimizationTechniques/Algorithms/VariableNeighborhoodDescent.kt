package HeuristicOptimizationTechniques.Algorithms

import HeuristicOptimizationTechniques.Algorithms.Neighborhoods.Neighborhood
import HeuristicOptimizationTechniques.Helper.Solution

class VariableNeighborhoodDescent(val neighborhoods: List<Neighborhood>) : ImprovementHeuristic {

    override fun improve(solution: Solution): Solution {
        var i = 0;
        var bestSolution = solution

        while (i < neighborhoods.size) {
            val neighborhood = neighborhoods[i]

            val neighbors = neighborhood
                .createNeighbors(bestSolution)

            if (neighbors.isEmpty()) {
                return bestSolution
            }
            val bestNeighbor = neighbors.minBy { s -> s.totalCost } //best improvement

            if (bestNeighbor.totalCost < bestSolution.totalCost) {
                bestSolution = bestNeighbor
                i = 0
            } else {
                i += 1
            }
        }

        return bestSolution
    }
}