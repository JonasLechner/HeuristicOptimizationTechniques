package HeuristicOptimizationTechniques.Algorithms.Neighborhoods

import HeuristicOptimizationTechniques.Helper.Instance
import HeuristicOptimizationTechniques.Helper.Logger
import HeuristicOptimizationTechniques.Helper.Solution

class TwoSwapNeighborhood(private val instance: Instance) : Neighborhood {
    private val logger = Logger.getLogger(TwoSwapNeighborhood::class.java.simpleName)

    override fun createNeighbors(solution: Solution): List<Solution> {
        val solutions = mutableListOf<Solution>()

        for ((idx, route) in solution.routes.withIndex()) {
            if (route.size <= 2) {
                continue
            }

            for (i in 0..<route.size - 1) {
                val first = route[i]
                val second = route[i + 1]

                // if first == Pickup_i and second == Dropoff_i -> can't swap
                if (instance.isPickupIndex(first) && second == first + instance.numberOfRequests) {
                    continue
                }

                val neighbor = solution.clone()
                val newRoute = neighbor.routes[idx]

                //only swap adjacent locations
                val temp = newRoute[i]
                newRoute[i] = newRoute[i + 1]
                newRoute[i + 1] = temp

                if (!instance.isCapacityWithinBounds(newRoute)) {
                    continue
                }
                neighbor.routes[idx] = newRoute

                val newLen = instance.computeRouteLength(newRoute)
                neighbor.sumsPerRoute[idx] = newLen

                neighbor.totalCost = instance.computeObjectiveFunction(neighbor)
                solutions.add(neighbor)
            }
        }

        return solutions
    }


    fun createNeighbors2(solution: Solution): List<Solution> {
        val solutions = mutableListOf<Solution>()

        for ((idx, route) in solution.routes.withIndex()) {
            if (route.size <= 2) {
                continue
            }

            for (i in 0..route.size - 4 step 2) {
                val j = i + 2

                val neighbor = solution.clone()
                val newRoute = neighbor.routes[idx]

                newRoute[i] = route[j]
                newRoute[i] = route[j]
                newRoute[j + 1] = route[i + 1]
                newRoute[i + 1] = route[j + 1]

                if (!instance.isCapacityWithinBounds(newRoute)) {
                    continue
                }

                val newLen = instance.computeRouteLength(newRoute)
                neighbor.sumsPerRoute[idx] = newLen

                neighbor.totalCost = instance.computeObjectiveFunction(neighbor)
                solutions.add(neighbor)
            }
        }

        return solutions
    }
}