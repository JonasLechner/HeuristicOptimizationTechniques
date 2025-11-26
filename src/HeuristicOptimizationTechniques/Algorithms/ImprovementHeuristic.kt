package HeuristicOptimizationTechniques.Algorithms

import HeuristicOptimizationTechniques.Helper.Solution

interface ImprovementHeuristic {
    fun improve(solution: Solution): Solution
}