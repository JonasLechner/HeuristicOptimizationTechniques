package HeuristicOptimizationTechniques.Algorithms.Neighborhoods

import HeuristicOptimizationTechniques.Helper.Instance
import HeuristicOptimizationTechniques.Helper.Logger
import HeuristicOptimizationTechniques.Helper.Solution
import kotlin.math.min

class VehicleSwapNeighborhood(private val instance: Instance) : Neighborhood {
    private val logger = Logger.getLogger(VehicleSwapNeighborhood::class.java.simpleName)
    override fun createNeighbors(solution: Solution): List<Solution> {
        val solutions = mutableListOf<Solution>()

        var biggestDeltaFrom = Int.MIN_VALUE
        var worstPickupFrom = -1
        var worstDropoffFrom = -1
        var worstRouteIndexFrom = -1
        for (i in 0..<solution.routes.size) {
            val currentRoute = solution.routes[i]
            if (currentRoute.size < 2) continue

            //every pickup droppoff pair
            for (j in 0..<currentRoute.size step 2) {
                val pickup = currentRoute[j]
                val dropoff = currentRoute[j + 1]
                var previousDropoff = -1 //depot index
                var nextPickup = -1 //depot index
                if (j != 0) {
                    previousDropoff = currentRoute[j - 1]
                }
                if (j + 2 < currentRoute.size) {
                    nextPickup = currentRoute[j + 2]
                }
                val distanceDelta = instance.computeRouteLengthDelta(
                    currentRoute,
                    pickup,
                    dropoff,
                    previousDropoff,
                    nextPickup
                )
                if (distanceDelta > biggestDeltaFrom) {
                    worstPickupFrom = j
                    worstDropoffFrom = j + 1
                    biggestDeltaFrom = distanceDelta
                    worstRouteIndexFrom = i
                }
            }
        }

        if (worstRouteIndexFrom == -1) {
            return emptyList()
        }

        //swap route with all others vehicles except itself
        for (i in 0..<solution.routes.size) {
            if (i == worstRouteIndexFrom) continue

            val neighbor = solution.clone()
            val fromRoute = neighbor.routes[worstRouteIndexFrom]
            val toRoute = neighbor.routes[i]

            val pickupLoc = fromRoute[worstPickupFrom]
            val dropoffLoc = fromRoute[worstDropoffFrom]

            fromRoute.removeAt(worstDropoffFrom)
            fromRoute.removeAt(worstPickupFrom)

            var smallestDeltaTo = Int.MAX_VALUE
            var bestPos = -1
            for (j in 0..toRoute.size step 2) {
                var previousDropoff = -1 //depot index
                var nextPickup = -1 //depot index
                if (j != 0) {
                    previousDropoff = toRoute[j - 1]
                }
                if (j != toRoute.size) {
                    nextPickup = toRoute[j]
                }
                val distanceDelta = instance.computeRouteLengthDelta(
                    toRoute,
                    pickupLoc,
                    dropoffLoc,
                    previousDropoff,
                    nextPickup
                )
                if (distanceDelta < smallestDeltaTo) {
                    bestPos = j
                    smallestDeltaTo = distanceDelta
                }
            }

            toRoute.add(bestPos, pickupLoc)
            toRoute.add(bestPos + 1, dropoffLoc)
            if (!instance.isCapacityWithinBounds(toRoute)) {
                continue
            }


            neighbor.sumsPerRoute[worstRouteIndexFrom] = instance.computeRouteLength(fromRoute)
            neighbor.sumsPerRoute[i] = instance.computeRouteLength(toRoute)
            neighbor.totalCost = instance.computeObjectiveFunction(neighbor.routes)

            solutions.add(neighbor)
            //logger.info("neighbor with cost: ${neighbor.totalCost}")
        }


        return solutions
    }
}