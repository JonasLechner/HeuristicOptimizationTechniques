package HeuristicOptimizationTechniques.Algorithms

import HeuristicOptimizationTechniques.Algorithms.Neighborhoods.Neighborhood
import HeuristicOptimizationTechniques.Algorithms.Neighborhoods.TwoSwapNeighborhood
import HeuristicOptimizationTechniques.Helper.Instance
import HeuristicOptimizationTechniques.Helper.Logger
import HeuristicOptimizationTechniques.Helper.Solution
import HeuristicOptimizationTechniques.Helper.StepFunction

class GRASP(
    private val instance: Instance,
    private val iterations: Int,
    private val neighborhood: Neighborhood,
) : ConstructionHeuristic {
    private val logger = Logger.getLogger(GRASP::class.java.simpleName)

    override fun construct(): Solution {
        val randomizedConstruction = RandomizedConstruction(instance, 1, 4)
        val localSearch = LocalSearch(neighborhood, StepFunction.BEST_IMPROVEMENT, 15)
        var bestTotalCost = Double.MAX_VALUE
        var bestSolution: Solution? = null
        for (i in 1..iterations) {
            var solution = randomizedConstruction.construct()
            logger.info("randomized found solution with totalCost: ${solution.totalCost}")
            solution = localSearch.improve(solution)
            logger.info("local search improved to totalCost: ${solution.totalCost}")

            if (solution.totalCost < bestTotalCost) {
                logger.info("New better solution found!!!!!!!!!!!!!")
                bestTotalCost = solution.totalCost
                bestSolution = solution
            }
        }
        logger.info("bestsolution: ${bestSolution?.totalCost}")

        return bestSolution!!
    }


}