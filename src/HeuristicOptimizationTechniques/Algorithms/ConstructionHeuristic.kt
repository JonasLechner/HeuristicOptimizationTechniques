package HeuristicOptimizationTechniques.Algorithms

import HeuristicOptimizationTechniques.Helper.Solution

interface ConstructionHeuristic {
    fun construct(): Solution
}
