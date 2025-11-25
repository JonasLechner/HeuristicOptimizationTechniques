package HeuristicOptimizationTechniques.Neighborhoods

import HeuristicOptimizationTechniques.Helper.Solution

interface Neighborhood {
    fun getNeighborhood(solution: Solution): List<Solution> //or neighboring
}