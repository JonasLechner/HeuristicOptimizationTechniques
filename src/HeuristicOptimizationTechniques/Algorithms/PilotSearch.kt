package HeuristicOptimizationTechniques.Algorithms

import HeuristicOptimizationTechniques.Helper.Candidate
import HeuristicOptimizationTechniques.Helper.Instance
import HeuristicOptimizationTechniques.Helper.Logger
import HeuristicOptimizationTechniques.Helper.Solution
import kotlin.system.measureTimeMillis

class PilotSearch(
    private val instance: Instance,
    private val maxRolloutDepth: Int,
    private val maxCandidateCount: Int
) :
    Heuristic {
    private val logger = Logger.getLogger(PilotSearch::class.java.simpleName)
    private val n: Int = instance.numberOfRequests

    override fun solve(): Solution {
        val currentSolution = Solution(n) //solution where best candidate is added in each iteration
        var bestCompleteSolution: Solution? = null //solution with best greedy extension

        val time = measureTimeMillis {
            logger.info("Running...")

            while (currentSolution.fulfilledCount() < n) {
                var bestCost = Double.MAX_VALUE
                var bestRollout: Solution? = null
                var bestCandidate: Candidate? = null

                //create one-step extensions (candidates) and take best ones
                val candidates = instance.createAllValidCandidates(currentSolution)
                    .sortedBy { instance.calculateDelta(currentSolution, it) }
                    .take(maxCandidateCount)

                logger.info("Iteration ${currentSolution.fulfilledCount()}, found ${candidates.size} candidates.")
                if (candidates.isEmpty()) {
                    break
                }

                //perform rollout for each candidate
                for (candidate in candidates) {
                    val tmp = currentSolution.clone()
                    instance.applyCandidateToSolution(tmp, candidate)

                    val rolloutSolution = rollout(tmp)

                    val cost = instance.computeObjectiveFunction(rolloutSolution.routes)
                    if (cost < bestCost) {
                        bestCandidate = candidate
                        bestCost = cost
                        bestRollout = rolloutSolution
                    }
                }

                if (bestCandidate == null) {
                    break
                }

                instance.applyCandidateToSolution(currentSolution, bestCandidate)
                currentSolution.totalCost =
                    instance.computeObjectiveFunction(currentSolution.routes)

                val bestCompleteCount = bestCompleteSolution?.fulfilledCount() ?: 0
                val bestCompleteCost = bestCompleteSolution?.totalCost ?: Double.MAX_VALUE

                val hasEnoughRequests = bestCompleteCount >= instance.minNumberOfRequestsFulfilled

                //always take if not enough requests
                //if enough requests, only if improved
                if (!hasEnoughRequests || (bestCost < bestCompleteCost)) {
                    bestCompleteSolution = bestRollout
                }
            }
        }

        logger.info("Found solution with cost ${bestCompleteSolution?.totalCost} in ${time / 1000.0} seconds.")
        return bestCompleteSolution ?: currentSolution //(partial) current solution fallback
    }

    //generate rollout for a particular solution
    private fun rollout(solution: Solution, breakOffIfLocalMax: Boolean = true): Solution {
        for (i in 1..maxRolloutDepth) {
            val candidates = instance.createAllValidCandidates(solution)

            if (candidates.isEmpty()) {
                return solution
            }

            val bestCandidate: Pair<Candidate, Double> = candidates.map { candidate ->
                val cost = instance.calculateDelta(solution, candidate)
                candidate to cost
            }.minBy { (_, cost) -> cost }

            //stop creating if next insertion does not improve total cost
            //and min amount of requests fulfilled
            if (breakOffIfLocalMax && solution.fulfilledCount() >= instance.minNumberOfRequestsFulfilled && bestCandidate.second >= 0) {
                return solution
            }

            instance.applyCandidateToSolution(solution, bestCandidate.first)
        }

        solution.totalCost = instance.computeObjectiveFunction(solution.routes)
        return solution
    }
}