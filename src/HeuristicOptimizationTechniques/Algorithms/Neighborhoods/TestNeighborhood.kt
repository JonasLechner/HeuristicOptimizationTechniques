package HeuristicOptimizationTechniques.Algorithms.Neighborhoods

import HeuristicOptimizationTechniques.Helper.Candidate
import HeuristicOptimizationTechniques.Helper.Instance
import HeuristicOptimizationTechniques.Helper.Location
import HeuristicOptimizationTechniques.Helper.Logger
import HeuristicOptimizationTechniques.Helper.Route
import HeuristicOptimizationTechniques.Helper.Solution
import kotlin.time.Duration.Companion.milliseconds

@Deprecated("Only for Testing")
class TestNeighborhood(private val instance: Instance, private val k: Int) : Neighborhood {
    private val logger = Logger.getLogger(TwoSwapNeighborhood::class.java.simpleName)

    fun removeDelta(route: Route, a: Int, b: Int): Int {
        fun prevOf(index: Int) =
            if (index == 0) instance.depotLocation else instance.getLocationOf(route[index - 1])
        fun nextOf(index: Int) =
            if (index == route.indices.last) instance.depotLocation else instance.getLocationOf(route[index + 1])

        val prevA = prevOf(a)
        val locA = instance.getLocationOf(route[a])
        val nextA = nextOf(a)

        val prevB = prevOf(b)
        val locB = instance.getLocationOf(route[b])
        val nextB = nextOf(b)

        return if (b == a + 1) {
            val before = prevA.distance(locA) + locA.distance(locB) + locB.distance(nextB)
            val after = prevA.distance(nextB)
            after - before
        } else {
            // non-adjacent: two independent removals
            val beforeA = prevA.distance(locA) + locA.distance(nextA)
            val afterA = prevA.distance(nextA)
            val deltaA = afterA - beforeA

            val beforeB = prevB.distance(locB) + locB.distance(nextB)
            val afterB = prevB.distance(nextB)
            val deltaB = afterB - beforeB

            deltaA + deltaB
        }
    }

    override fun createNeighbors(solution: Solution): List<Solution> {
        val solutions: MutableList<Solution> = mutableListOf()
        val sorted = solution.routes.indices.sortedBy { idx -> solution.sumsPerRoute[idx] }

        val kLongestRoutes = sorted.takeLast(k)
        val kShortestRoutes = sorted.take(k)

        val removalCandidates: MutableList<RemovalCandidate> = mutableListOf()

        for (id in kLongestRoutes) {
            val route = solution.routes[id]
            var delta: Int? = null
            var worst: Int? = null

            for ((i, location) in route.withIndex()) {
                if (instance.isDropIndex(location)) continue

                val dropIdx =
                    route.indices.first { i -> route[i] == location + instance.numberOfRequests }

                val currentDelta = removeDelta(route, i, dropIdx)

                if (delta == null || currentDelta > delta) {
                    delta = currentDelta
                    worst = i
                }
            }

            if (worst != null && delta != null) {
                removalCandidates.add(RemovalCandidate(id, worst, delta))
            }
        }

        for (id in kShortestRoutes) {
            for (candidate in removalCandidates) {
                val copy = solution.clone()
                val removeRoute = copy.routes[candidate.routeIdx]
                val addRoute = copy.routes[id]
                //remove from
                copy.addDeltaToRouteCost(candidate.routeIdx, -candidate.delta)
                val locationId = removeRoute[candidate.pickupIdx]

                removeRoute.removeAt(candidate.pickupIdx + 1)
                removeRoute.removeAt(candidate.pickupIdx)

                //append to
                val addCandidate = Candidate(locationId, id, addRoute.size, addRoute.size + 1)

                instance.applyCandidateToSolution(copy, addCandidate)

                solutions.add(copy)
            }

        }

        return solutions
    }

    class RemovalCandidate(val routeIdx: Int, val pickupIdx: Int, val delta: Int)

}