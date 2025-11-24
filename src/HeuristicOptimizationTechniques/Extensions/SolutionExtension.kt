package HeuristicOptimizationTechniques.Extensions

import HeuristicOptimizationTechniques.Helper.Candidate
import HeuristicOptimizationTechniques.Helper.Instance
import HeuristicOptimizationTechniques.Helper.Solution

interface SolutionExtension {
    fun getExtensionCandidates(sol: Solution): List<Candidate>
}