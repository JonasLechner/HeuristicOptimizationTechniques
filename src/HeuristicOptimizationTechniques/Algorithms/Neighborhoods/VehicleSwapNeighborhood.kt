package HeuristicOptimizationTechniques.Algorithms.Neighborhoods

import HeuristicOptimizationTechniques.Helper.Instance
import HeuristicOptimizationTechniques.Helper.Solution

class VehicleSwapNeighborhood(private val instance: Instance) : Neighborhood {
    override fun createNeighborhood(solution: Solution): List<Solution> {
        val solutions = mutableListOf<Solution>()
        val longestRouteIndex = solution.getIndexOfLongestRoute()
        val longestRoute = solution.routes[longestRouteIndex]
        if (longestRoute.size <= 2) {
            return solutions
        }

        var biggestDelta = Integer.MIN_VALUE
        var worstPickup = -1
        var worstDropoff = -1
        for (i in 0..<longestRoute.size step 2) {
            val pickup = longestRoute[i]
            val dropoff = longestRoute[i + 1]
            var previousDropoff = -1 //depot index
            var nextPickup = -1
            if (i != 0) {
                previousDropoff = longestRoute[i - 1]
            }
            if (i + 1 != longestRoute.size - 1) {
                nextPickup = longestRoute[i + 2]
            }
            var distanceDelta = instance.computeRouteLengthDelta(longestRoute, pickup, dropoff, previousDropoff, nextPickup)
            if (distanceDelta > biggestDelta) {
                worstPickup = i
                worstDropoff = i + 1
                biggestDelta = distanceDelta
            }
        }

        for (i in 0 ..<instance.numberOfVehicles) {
            if (i == longestRouteIndex) {
                continue
            }

            val neighbor = solution.clone()
            val fromRoute = neighbor.routes[longestRouteIndex]
            val toRoute = neighbor.routes[i]

            val pickupLoc = fromRoute[worstPickup]
            val dropoffLoc = fromRoute[worstDropoff]

            fromRoute.removeAt(worstDropoff)
            fromRoute.removeAt(worstPickup)

            toRoute.add(pickupLoc)
            toRoute.add(dropoffLoc)

            neighbor.sumsPerRoute[longestRouteIndex] = instance.computeRouteLength(fromRoute)
            neighbor.sumsPerRoute[i] = instance.computeRouteLength(toRoute)
            neighbor.totalCost = instance.computeObjectiveFunction(neighbor.routes)

            solutions.add(neighbor)
        }
        return solutions
    }
}