package HeuristicOptimizationTechniques.Algorithms

import HeuristicOptimizationTechniques.Helper.CombinedStopConditionGuard
import HeuristicOptimizationTechniques.Helper.Instance
import HeuristicOptimizationTechniques.Helper.Solution
import HeuristicOptimizationTechniques.Helper.StopCondition
import kotlin.random.Random

/**
 * Tuning:
 * - populationSize: 10-100
 * - eliteCount: 1-3
 * - mutationRate: .2 - 1
 * - iterationsNoChange: 3 - 10
 */

class GeneticAlgorithm(
    private val heuristic: ConstructionHeuristic,
    private val instance: Instance,
    private val populationSize: Int,
    private val eliteCount: Int,
    private val rng: Random,
    private val stopConditions: List<StopCondition>,
    private val mutationRate: Double
) : ConstructionHeuristic {

    override fun construct(): Solution {
        var population = List(populationSize) { heuristic.construct() }

        for ((idx, solution) in population.withIndex()) {
            println("generated $idx: " + solution.totalCost)
        }

        var bestObj = Double.MAX_VALUE

        val guard = CombinedStopConditionGuard(stopConditions)
        while (guard.shouldContinue(bestObj)) {
            val newPopulation = ArrayList<Solution>(populationSize)
            // SELECT
            val sortedPopulation = population.sortedBy { it.totalCost }

            bestObj = sortedPopulation.first().totalCost
            println(bestObj)

            // add elites
            newPopulation.addAll(sortedPopulation.take(eliteCount))

            // RECOMBINE
            var parentsNeeded = (populationSize - eliteCount) * 2

            rankSelection(sortedPopulation, parentsNeeded)
                .chunked(2)
                .forEach { (a, b) -> newPopulation.add(combine(a, b)) }

            // MUTATE
            newPopulation.withIndex().forEach { (idx, individual) ->
                if (idx >= eliteCount && rng.nextDouble() < mutationRate) {
                    repeat(rng.nextInt(1, 3)) {
                        mutateRandom(individual)
                    }
                }
            }

            population = newPopulation
        }

        return population.minBy { it.totalCost }
    }


    fun mutateRandom(solution: Solution) {
        val (first, second) = solution.routes.indices.shuffled().take(2)

        repeat(rng.nextInt(1, 3)) {
            moveFromTo(first, second, solution)
        }
    }

    fun mutate(solution: Solution) {
        val idxSortedByRouteLength = solution.sumsPerRoute.withIndex().sortedBy { it.value }.map { it.index }

        val mid = instance.numberOfVehicles / 5 + 1
        val lowerHalf = idxSortedByRouteLength.take(mid)
        val upperHalf = idxSortedByRouteLength.takeLast(mid)

        val smallestIdx = lowerHalf.random(rng)
        val largestIdx = upperHalf.random(rng)

        moveFromTo(largestIdx, smallestIdx, solution)
    }

    fun moveFromTo(fromRouteIdx: Int, toRouteIdx: Int, solution: Solution) {
        val toRoute = solution.routes[toRouteIdx]
        val fromRoute = solution.routes[fromRouteIdx]

        if (fromRoute.isEmpty()) {
            return
        }
        val randomRequest = fromRoute.random(rng).let {
            if (instance.isDropIndex(it)) {
                it - instance.numberOfRequests
            } else {
                it
            }
        }

        solution.removeRequest(randomRequest)

        val bestCandidate = instance.createAllCandidatesPerRoute(toRoute, randomRequest, toRouteIdx)
            .random(rng)

        instance.applyCandidateToSolution(solution, bestCandidate)
    }

    fun combine(first: Solution, second: Solution): Solution {
        val child = second.clone()
        val route = first.routes.random(rng)
        val pickups = route.filter { instance.isPickupIndex(it) }

        val fraction = 0.4
        val m = maxOf(1, (pickups.size * fraction).toInt())
        val requests = pickups.shuffled(rng).take(m)

        for (r in requests) {
            child.removeRequest(r)
        }

        for (r in requests) {
            val candidates = instance.createAllInsertionCandidatesPerRequest(child, r)
                .map { c ->
                    val delta = instance.routeLengthDeltaCalculation(child, c)
                    val score = instance.calculateObjectiveFromSolution(child, c, delta)
                    c to score
                }.sortedBy { it.second }

            val candidate =
                if (rng.nextDouble() < 0.85) {
                    candidates.take(10).random(rng).first
                } else {
                    candidates.random(rng).first
                }

            instance.applyCandidateToSolution(child, candidate)
        }
        return child
    }

    fun tournamentSelection(
        sortedPopulation: List<Solution>,
        numToSelect: Int,
        tournamentSize: Int = 3
    ): List<Solution> {
        require(tournamentSize >= 2) { "tournamentSize must be >= 2" }

        val n = sortedPopulation.size

        fun selectOne(): Solution {
            // Pick k random individuals
            val candidates = List(tournamentSize) {
                sortedPopulation[rng.nextInt(n)]
            }
            // Return the best among them (lowest cost)
            return candidates.minBy { it.totalCost }
        }

        return List(numToSelect) { selectOne() }
    }

    fun rankSelection(
        sortedPopulation: List<Solution>,
        numToSelect: Int,
    ): List<Solution> {
        val n = sortedPopulation.size

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
                    return sortedPopulation[i]
                }
            }
            return sortedPopulation.last()
        }

        // 4. Select required number of parents (with replacement)
        return List(numToSelect) { selectOne() }
    }
}