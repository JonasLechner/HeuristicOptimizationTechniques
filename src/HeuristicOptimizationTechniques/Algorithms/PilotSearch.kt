package HeuristicOptimizationTechniques.Algorithms

import HeuristicOptimizationTechniques.Helper.Candidate
import HeuristicOptimizationTechniques.Helper.InstanceWrapper
import HeuristicOptimizationTechniques.Helper.RouteUtils
import HeuristicOptimizationTechniques.Helper.Solution
import java.util.logging.Logger

class PilotSearch(private val instance: InstanceWrapper, private val rollout: Int? = null) :
    Heuristic {
    private val logger = Logger.getLogger(PilotSearch::class.java.name)
    val n: Int = instance.nRequests

    override fun solve(): Solution {
        val currentSolution = Solution(n)
        var bestCompleteSolution: Solution? = null

        logger.info("Running...")

        while (currentSolution.assignedCount() <= n) {
            var bestCost = Int.MAX_VALUE
            var bestRollout: Solution? = null
            var bestCandidate: Candidate? = null

            val candidates = createAllValidCandidates(currentSolution)
            logger.info("Iteration ${currentSolution.assignedCount()}, found ${candidates.size} candidates")
            if (candidates.isEmpty()) {
                break
            }

            for (candidate in candidates) {
                val tmp = currentSolution.clone()
                applyCandidate(tmp, candidate)

                val rolloutSolution = rollout(tmp)

                val cost = RouteUtils.totalCost(rolloutSolution, instance)
                if (cost < bestCost) {
                    bestCandidate = candidate
                    bestCost = cost
                    bestRollout = rolloutSolution
                }
            }

            if (bestCandidate == null || bestRollout == null) {
                break
            }

            applyCandidate(currentSolution, bestCandidate)
            currentSolution.totalCost = RouteUtils.totalCost(currentSolution, instance)

            if (bestCompleteSolution == null
                || (bestRollout.totalCost < bestCompleteSolution.totalCost)
            ) {
                bestCompleteSolution = bestRollout
            }
        }
        return bestCompleteSolution ?: currentSolution //(partial) current solution fallback
    }

    fun rollout(solution: Solution, breakOffIfLocalMax: Boolean = true): Solution {
        while (solution.assignedCount() <= n) {
            val candidates = createAllValidCandidates(solution)

            if (candidates.isEmpty()) {
                return solution
            }

            val bestCandidate: Pair<Candidate, Int> =
                candidates
                    .map { candidate ->
                        val cost = calculateDelta(solution, candidate)
                        candidate to cost
                    }
                    .minBy { (_, cost) -> cost }

            //stop creating if next insertion does not improve total cost
            //and min amount of requests fulfilled
            if (breakOffIfLocalMax
                && solution.assignedCount() >= instance.minRequests
                && bestCandidate.second >= 0
            ) {
                return solution
            }

            applyCandidate(solution, bestCandidate.first)
        }

        solution.totalCost = RouteUtils.totalCost(solution, instance)
        return solution
    }

    fun applyCandidate(solution: Solution, candidate: Candidate) {
        assert(candidate.routeIndex <= solution.routes.size)

        val (pickup, dropOff) = instance.indecesForRequest(candidate.requestId)
        if (candidate.routeIndex == solution.routes.size) {
            solution.routes.add(mutableListOf(pickup, dropOff))
        } else {
            val toModify = solution.routes[candidate.routeIndex]
            toModify.add(candidate.pickPos, pickup)
            toModify.add(candidate.dropPos, dropOff)
        }
        solution.setAssigned(candidate.requestId)
    }

    fun calculateDelta(solution: Solution, candidate: Candidate): Int {
        //new route
        val (pickup, dropOff) = instance.indecesForRequest(candidate.requestId)

        if (candidate.routeIndex == solution.routes.size) {
            return RouteUtils.routeCost(
                mutableListOf(pickup, dropOff), instance
            )
        }

        //modified route
        val oldRoute = solution.routes[candidate.routeIndex]
        val newRoute = ArrayList(oldRoute)
        newRoute.add(candidate.pickPos, pickup)
        newRoute.add(candidate.dropPos, dropOff)

        return RouteUtils.routeCost(newRoute, instance) - RouteUtils.routeCost(oldRoute, instance)
    }

    fun createAllValidCandidatesPerRequest(sol: Solution, requestId: Int): List<Candidate> {
        val candidates = ArrayList<Candidate>()

        val (pickup, dropOff) = instance.indecesForRequest(requestId)

        for ((routeIdx, route) in sol.routes.withIndex()) {
            for (i in 0..route.size) {
                for (j in i + 1..route.size + 1) {
                    val tmp = ArrayList(route)
                    tmp.add(i, pickup)
                    tmp.add(j, dropOff)

                    if (RouteUtils.isCapacityFeasible(tmp, instance)) {
                        candidates.add(Candidate(requestId, routeIdx, i, j))
                    }
                }
            }
        }

        val withNewRoute = mutableListOf(pickup, dropOff)
        if (RouteUtils.isCapacityFeasible(withNewRoute, instance)) {
            candidates.add(Candidate(requestId, sol.routes.size, 0, 1))
        }

        return candidates
    }

    private fun createAllValidCandidates(sol: Solution): List<Candidate> {
        val all = ArrayList<Candidate>()
        for (rId in 1..instance.nRequests) {
            if (sol.isAssigned(rId)) continue
            val cands = createAllValidCandidatesPerRequest(sol, rId)
            all.addAll(cands)
        }

        return all
    }
}