package HeuristicOptimizationTechniques.Algorithms

import HeuristicOptimizationTechniques.Algorithms.Neighborhoods.Neighborhood
import HeuristicOptimizationTechniques.Helper.StopConditionGuard
import HeuristicOptimizationTechniques.Helper.Solution
import HeuristicOptimizationTechniques.Helper.StopCondition
import java.util.ArrayDeque
import java.util.Deque
import java.util.HashSet

class TabuSearch(
    val neighborhood: Neighborhood,
    val stopCondition: StopCondition,
    val tabuListSize: Int
) :
    ImprovementHeuristic {
    lateinit var tabuQueue: Deque<Int>
    lateinit var tabuSet: MutableSet<Int>

    override fun improve(solution: Solution): Solution {
        tabuQueue = ArrayDeque(tabuListSize) //could be extended by hashset
        tabuSet = HashSet(tabuListSize)

        var bestSolution: Solution = solution

        val conditionGuard = StopConditionGuard(stopCondition)

        while (conditionGuard.shouldContinue()) {
            val bestNeighbor = neighborhood
                .createNeighbors(bestSolution)
                .filter { s -> !isTabu(s) }
                .minByOrNull { s -> s.totalCost }

            if (bestNeighbor == null) {
                return bestSolution
            }

            insertNewTabu(bestNeighbor)

            if (bestNeighbor.totalCost < bestSolution.totalCost) {
                bestSolution = bestNeighbor
            } else {
                //local max
                return bestNeighbor
            }
        }

        return bestSolution
    }

    private fun isTabu(
        solution: Solution
    ): Boolean {
        return tabuSet.contains(solution.hashCode())
    }

    private fun insertNewTabu(
        solution: Solution
    ) {
        val hash = solution.hashCode()
        tabuQueue.addLast(hash)
        tabuSet.add(hash)

        if (tabuQueue.size > tabuListSize) {
            val first = tabuQueue.removeFirst()
            tabuSet.remove(first)
        }
    }
}