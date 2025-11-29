package HeuristicOptimizationTechniques.Algorithms.Neighborhoods

import HeuristicOptimizationTechniques.Helper.Instance
import HeuristicOptimizationTechniques.Helper.Logger
import HeuristicOptimizationTechniques.Helper.Route
import HeuristicOptimizationTechniques.Helper.Solution

class VehicleMoveNeighborhood(private val instance: Instance) : Neighborhood {
    private val logger = Logger.getLogger(VehicleMoveNeighborhood::class.java.simpleName)

    override fun createNeighbors(solution: Solution): List<Solution> {
        val solutions = mutableListOf<Solution>()
        val longestRouteIndex = solution.getIndexOfLongestRoute()
        val longestRoute = solution.routes[longestRouteIndex]
        if (longestRoute.size <= 2) {
            return solutions
        }

        var biggestDelta = Integer.MIN_VALUE
        var worstPickup = -1
        var worstDropoff = -1
        for (i in 0..<longestRoute.size) {
            var pickup = -1;
            var dropoff = -1
            var pickupIndex = -1
            var dropoffIndex = -1
            if (instance.isPickupIndex(longestRoute[i])) {
                pickup = longestRoute[i]
                pickupIndex = i
                dropoff = longestRoute[i] + instance.numberOfRequests
                //find corresponding dropoffindex
                for (i_2 in 0..<longestRoute.size) {
                    if (longestRoute[i_2] == dropoff) {
                        dropoffIndex = i_2
                        break
                    }
                }

            } else {
                continue
            }

            if (pickupIndex + 1 == dropoffIndex) {
                var previousDropoff = -1 //depot index
                var nextPickup = -1
                if (i != 0) {
                    previousDropoff = longestRoute[i - 1]
                }
                if (i + 1 != longestRoute.size - 1) {
                    nextPickup = longestRoute[i + 2]
                }
                val distanceDelta = instance.computeRouteLengthDelta(
                    longestRoute,
                    pickup,
                    dropoff,
                    previousDropoff,
                    nextPickup
                )
                if (distanceDelta > biggestDelta) {
                    worstPickup = i
                    worstDropoff = i + 1
                    biggestDelta = distanceDelta
                }
            } else {
                var previousPointForPickup = -1 //depot index
                var nextPointForPickup = -1
                if (i != 0) {
                    previousPointForPickup = longestRoute[pickupIndex - 1]
                }
                if (i + 1 < longestRoute.size) {
                    nextPointForPickup = longestRoute[pickupIndex + 1]
                }

                var previousPointForDropoff = -1 //depot index
                var nextPointForDropoff = -1
                if (dropoffIndex != 0) {
                    previousPointForDropoff = longestRoute[dropoffIndex - 1]
                }
                if (dropoffIndex + 1 < longestRoute.size) {
                    nextPointForDropoff = longestRoute[dropoffIndex + 1]
                }
                val distanceDelta = instance.computeRouteLengthDelta(
                    longestRoute, pickup, pickup,
                    previousPointForPickup, nextPointForPickup
                ) +
                        instance.computeRouteLengthDelta(
                            longestRoute, dropoff, dropoff,
                            previousPointForDropoff, nextPointForDropoff
                        )
                if (distanceDelta > biggestDelta) {
                    worstPickup = pickupIndex
                    worstDropoff = dropoffIndex
                    biggestDelta = distanceDelta
                }
            }
        }

        for (i in 0..<instance.numberOfVehicles) {
            //move route to all vehicles except itself and add to the start
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

            toRoute.add(0, pickupLoc)
            toRoute.add(1, dropoffLoc)
            if (!instance.isCapacityWithinBounds(toRoute)) {
                continue
            }

            neighbor.sumsPerRoute[longestRouteIndex] = instance.computeRouteLength(fromRoute)
            neighbor.sumsPerRoute[i] = instance.computeRouteLength(toRoute)
            neighbor.totalCost = instance.computeObjectiveFunction(neighbor.routes)

            solutions.add(neighbor)
        }
        return solutions
    }
}