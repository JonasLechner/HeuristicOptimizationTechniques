package HeuristicOptimizationTechniques.Algorithms

import HeuristicOptimizationTechniques.Helper.Candidate
import HeuristicOptimizationTechniques.Helper.Instance
import HeuristicOptimizationTechniques.Helper.Solution

class NewGreedyConstruction(
    private val instance: Instance,
    private val isRandomized: Boolean,
    private val reducedCandidateCount: Int,
) : ConstructionHeuristic {
    override fun construct(): Solution {
        val currentSolution =
            Solution(instance) //solution where best candidate is added in each iteration

        while (currentSolution.fulfilledCount() < instance.minNumberOfRequestsFulfilled) {
            if (currentSolution.fulfilledCount() % 100 == 0)
                println(currentSolution.fulfilledCount())

            val candidateList = instance.createCandidates(currentSolution, isRandomized)
                .let { if (isRandomized) it else it.take(reducedCandidateCount) }
                .map { c ->
                    val delta = instance.routeLengthDeltaCalculation(currentSolution, c)
                    val score = instance.calculateObjectiveFromSolution(currentSolution, c, delta)
                    c to score
                }
                .sortedBy { it.second }
                .map { it.first }

            if (candidateList.isEmpty()) {
                break
            }

            val bestCandidate = if (isRandomized) {
                //take random candidate from RCL
                candidateList.take(reducedCandidateCount).random()
            } else {
                //take best
                candidateList.first()
            }

            instance.applyCandidateToSolution(currentSolution, bestCandidate)
        }

        return currentSolution
    }

}