package HeuristicOptimizationTechniques.Algorithms.Neighborhoods

import HeuristicOptimizationTechniques.Helper.Instance
import HeuristicOptimizationTechniques.Helper.Solution

class TwoSwapNeighborhood(private val instance: Instance) : Neighborhood {

    override fun createNeighbors(solution: Solution): List<Solution> {
        val solutions = mutableListOf<Solution>()
        val longestRouteIndex = solution.getIndexOfLongestRoute()
        val longestRoute = solution.routes[longestRouteIndex]
        if (longestRoute.size <= 2) {
            return emptyList()
        }

        for (i in 0..<longestRoute.size - 1) {
            val first = longestRoute[i]
            val second = longestRoute[i + 1]
            // if first == Pickup_i and second == Dropoff_i -> can't swap
            if (instance.isPickupIndex(first) && second == first + instance.numberOfRequests) {
                continue
            }

            val neighbor = solution.clone()
            val newRoute = neighbor.routes[longestRouteIndex]

            val temp = newRoute[i]
            newRoute[i] = newRoute[i + 1]
            newRoute[i + 1] = temp

            val newLen = instance.computeRouteLength(newRoute)
            neighbor.sumsPerRoute[longestRouteIndex] = newLen

            neighbor.totalCost = instance.computeObjectiveFunction(neighbor)
            solutions.add(neighbor)
        }

        return solutions
    }
}