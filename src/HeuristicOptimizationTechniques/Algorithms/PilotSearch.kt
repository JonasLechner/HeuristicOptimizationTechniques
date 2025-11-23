package HeuristicOptimizationTechniques.Algorithms

import HeuristicOptimizationTechniques.Helper.Instance2
import HeuristicOptimizationTechniques.Helper.Solution

class PilotSearch(private val instance: Instance2, private val rollout: Int? = null) :
    Heuristic {

    override fun run() {
        val initial = Solution(instance.nRequests)

        rollout(initial.clone())
    }

    fun rollout(solution: Solution) {
        //stop extension if > minRequests && next step would decrease quality


    }
}