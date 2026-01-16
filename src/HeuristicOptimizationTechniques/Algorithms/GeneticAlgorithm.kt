package HeuristicOptimizationTechniques.Algorithms

import HeuristicOptimizationTechniques.Helper.Instance
import HeuristicOptimizationTechniques.Helper.Solution
import kotlin.random.Random

class GeneticAlgorithm(
    private val heuristic: ConstructionHeuristic,
    private val instance: Instance,
    private val populationSize: Int,
    private val eliteCount: Int,
    private val rng: Random,
) : ConstructionHeuristic {

    fun combine(first: Solution, second: Solution): Solution {
        val child = second.clone()
        val route = first.routes.random()
        val requests = route.filter { instance.isPickupIndex(it) }.shuffled()

        for (r in requests) {
            child.removeRequest(r)
        }

        for (r in requests) {
            val bestCandidate = instance.createAllInsertionCandidatesPerRequest(child, r)
                .minByOrNull { c ->
                    val delta = instance.routeLengthDeltaCalculation(child, c)
                    val score = instance.calculateObjectiveFromSolution(child, c, delta)
                    score
                }

            if (bestCandidate != null) {
                instance.applyCandidateToSolution(child, bestCandidate)
            }
        }
        return child
    }


    fun rankSelection(
        population: List<Solution>,
        numToSelect: Int,
        rng: Random = Random.Default
    ): List<Solution> {

        // 1. Sort by fitness (best first)
        val sorted = population.sortedBy { it.totalCost }
        val n = sorted.size

        // 2. Build cumulative probability table (CDF)
        val cdf = DoubleArray(n)
        var cumulative = 0.0

        for (i in 0 until n) {
            val rank = i + 1
            val probability = 2.0 * (n - rank + 1) / (n * (n + 1).toDouble())
            cumulative += probability
            cdf[i] = cumulative
        }
        cdf[n - 1] = 1.0   // safety against floating point errors

        // 3. Select individuals using roulette wheel on ranks
        fun selectOne(): Solution {
            val r = rng.nextDouble()
            for (i in cdf.indices) {
                if (r <= cdf[i]) {
                    return sorted[i]
                }
            }
            return sorted.last()
        }

        // 4. Select required number of parents (with replacement)
        return List(numToSelect) { selectOne() }
    }


    override fun construct(): Solution {
        var population = List(populationSize) { heuristic.construct() }

        for (solution in population) {
            println(solution.totalCost)
        }

        repeat(250) {
            val newPopulation = ArrayList<Solution>(populationSize)
            // SELECT
            val sortedPopulation = population.sortedBy { it.totalCost }

            println(sortedPopulation.first().totalCost)

            // add elites
            newPopulation.addAll(sortedPopulation.take(eliteCount))

            // RECOMBINE
            val parentsNeeded = (populationSize - eliteCount) * 2
            rankSelection(sortedPopulation, parentsNeeded, rng)
                .chunked(2)
                .forEach { (a, b) ->
                    newPopulation.add(combine(a, b))
                }
            // MUTATE

            population = newPopulation
        }

        return population.minBy { it.totalCost }
    }
}