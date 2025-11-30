package HeuristicOptimizationTechniques.Algorithms

import HeuristicOptimizationTechniques.Algorithms.Neighborhoods.Neighborhood
import HeuristicOptimizationTechniques.Algorithms.Neighborhoods.TwoSwapNeighborhood
import HeuristicOptimizationTechniques.Helper.Instance
import HeuristicOptimizationTechniques.Helper.Logger
import HeuristicOptimizationTechniques.Helper.Solution
import HeuristicOptimizationTechniques.Helper.StepFunction
import HeuristicOptimizationTechniques.Helper.StopCondition
import HeuristicOptimizationTechniques.Helper.StopConditionGuard
import kotlin.math.min

class GRASP(
    private val instance: Instance,
    private val neighborhood: Neighborhood,
    private val stopCondition: StopCondition
) : ConstructionHeuristic {
    private val logger = Logger.getLogger(GRASP::class.java.simpleName)

    override fun construct(): Solution {
        val onePercent = min(20, instance.numberOfRequests / 50)
        val randomizedConstruction = NewGreedyConstruction(instance, true, onePercent)

        val localSearch =
            LocalSearch(neighborhood, StepFunction.BEST_IMPROVEMENT, StopCondition.Iterations(15))
        var bestTotalCost = Double.MAX_VALUE
        var bestSolution: Solution? = null

        val guard = StopConditionGuard(stopCondition)

        while (guard.shouldContinue()) {
            var solution = randomizedConstruction.construct()
            //logger.info("randomized found solution with totalCost: ${solution.totalCost}")
            solution = localSearch.improve(solution)
            //logger.info("local search improved to totalCost: ${solution.totalCost}")

            if (solution.totalCost < bestTotalCost) {
                //logger.info("New better solution found!!!!!!!!!!!!!")
                bestTotalCost = solution.totalCost
                bestSolution = solution
            }
        }
        //logger.info("bestsolution: ${bestSolution?.totalCost}")

        return bestSolution!!
    }


}