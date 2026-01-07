package HeuristicOptimizationTechniques.Algorithms

import HeuristicOptimizationTechniques.Helper.Instance
import HeuristicOptimizationTechniques.Helper.Logger
import HeuristicOptimizationTechniques.Helper.Route
import HeuristicOptimizationTechniques.Helper.Solution
import kotlin.random.Random
import kotlin.math.pow

class AntColonyOptimization(
    private val instance: Instance,
    private val numberOfAnts: Int = 30,
    private val iterations: Int = 200,
    private val alpha: Double = 1.0,     // pheromone influence
    private val beta: Double = 2.0,      // heuristic influence
    private val rho: Double = 0.5,       // evaporation rate in [0,1]
    private val q: Double = 1.0,         // pheromone deposit factor
    private val seed: Int? = null
) : ConstructionHeuristic {
    private val logger = Logger.getLogger(AntColonyOptimization::class.java.simpleName)
    private val rnd: Random = seed?.let { Random(it) } ?: Random.Default

    private val tau: Array<DoubleArray> = Array(instance.numberOfRequests + 1) {
        DoubleArray(instance.numberOfRequests + 1) { 1.0 } // initial pheromone
    }

    override fun construct(): Solution {
        var globalBest: Solution? = null

        for (t in 1..iterations) {
            val ants = ArrayList<Solution>(numberOfAnts)

            repeat(numberOfAnts) {
                val s = constructSolution()
                ants.add(s)

                if (globalBest == null || s.totalCost < globalBest.totalCost) {
                    globalBest = s.clone()
                }
            }

            evaporate()
            depositAll(ants)

            if (t % 10 == 0 && globalBest != null) {
                logger.info("AntColonyOptimization iteration=$t, with bestCost=${globalBest.totalCost} and ${globalBest.fulfilledCount()} requests fulfilled")
            }
        }
        return globalBest!!
    }

    // Creates one solution for one ant
    private fun constructSolution(): Solution {
        val sol = Solution(instance)

        val unvisitedRequestIds = (1..instance.numberOfRequests).toMutableList()

        sol.routes.clear()

        if (instance.numberOfVehicles <= 0) {
            sol.totalCost = instance.computeObjectiveFunction(sol)
            return sol
        }

        var currentRouteIndex = 0
        var currentRoute: Route = mutableListOf()
        sol.routes.add(currentRoute)

        // start with 0 (no request)
        var prevReq = 0

        // tries to append a request to the end of the current route
        fun tryAppendRequest(route: Route, reqId: Int): Boolean {
            val (pIdx, dIdx) = instance.getIndexPairForRequest(reqId)
            val candidate = ArrayList(route)
            candidate.add(pIdx)
            candidate.add(dIdx)
            if (!instance.isCapacityWithinBounds(candidate)) return false

            route.add(pIdx)
            route.add(dIdx)
            sol.setFulfilled(reqId)
            unvisitedRequestIds.remove(reqId)
            prevReq = reqId
            return true
        }

        //randomly appends request to end
        fun startRouteWithRandomRequest(): Boolean {
            if (unvisitedRequestIds.isEmpty()) return false
            val maxTries = unvisitedRequestIds.size
            repeat(maxTries) {
                val req = unvisitedRequestIds[rnd.nextInt(unvisitedRequestIds.size)]
                if (tryAppendRequest(currentRoute, req)) return true
            }
            return false
        }

        startRouteWithRandomRequest() //initialize route with random request

        while (unvisitedRequestIds.isNotEmpty() && currentRouteIndex < instance.numberOfVehicles) {
            // If current route is empty do random start
            if (currentRoute.isEmpty()) {
                val ok = startRouteWithRandomRequest()
                if (!ok) {
                    currentRouteIndex += 1
                    if (currentRouteIndex >= instance.numberOfVehicles) break
                    currentRoute = mutableListOf()
                    sol.routes.add(currentRoute)
                    prevReq = 0
                    continue
                }
            }

            // Choose next request based on tau
            val nextReq = chooseNextRequest(prevReq, currentRoute, unvisitedRequestIds)

            if (tryAppendRequest(currentRoute, nextReq)) {
                if (instance.numberOfVehicles > 1) {
                    currentRouteIndex = (currentRouteIndex + 1) % instance.numberOfVehicles
                    while (sol.routes.size <= currentRouteIndex) sol.routes.add(mutableListOf())
                    currentRoute = sol.routes[currentRouteIndex]
                    prevReq = 0
                }
                continue
            }

            // If doesn't fit go to next vehicle and reset prevReq
            currentRouteIndex += 1
            if (currentRouteIndex >= instance.numberOfVehicles) break

            currentRoute = mutableListOf()
            sol.routes.add(currentRoute)
            prevReq = 0
        }

        // Update route sums + objective
        sol.sumsPerRoute = MutableList(instance.numberOfVehicles) { 0 }
        for (i in sol.routes.indices) {
            sol.sumsPerRoute[i] = instance.computeRouteLength(sol.routes[i])
        }
        sol.totalCost = instance.computeObjectiveFunction(sol)

        return sol
    }



    //Uses roulette wheel selection on desirability(req) = tau[prevReq][req]^alpha * eta(req)^beta
    private fun chooseNextRequest(prevReq: Int, currentRoute: Route, candidates: List<Int>): Int {
        val desir = DoubleArray(candidates.size)
        var sum = 0.0

        for ((idx, req) in candidates.withIndex()) {
            val delta = instance.computeRouteLengthDelta(currentRoute, req).toDouble().coerceAtLeast(1.0)
            val eta = 1.0 / delta // smaller additional cost = higher heuristic desirability

            val value = tau[prevReq][req].pow(alpha) * eta.pow(beta)
            desir[idx] = value
            sum += value
        }

        // fallback random
        if (sum <= 0.0) return candidates[rnd.nextInt(candidates.size)]

        var r = rnd.nextDouble() * sum
        for (i in candidates.indices) {
            r -= desir[i]
            if (r <= 0.0) return candidates[i]
        }
        return candidates.last()
    }

    //evaporation: tau = (1-rho) * tau
    private fun evaporate() {
        val factor = (1.0 - rho).coerceIn(0.0, 1.0)
        for (i in 0..instance.numberOfRequests) {
            for (j in 0..instance.numberOfRequests) {
                tau[i][j] *= factor
                if (tau[i][j] < 1e-12) tau[i][j] = 1e-12
            }
        }
    }


    //Better solutions(smaller l) get more pheromones. Whenever request a is followed by request b, we increase the pheromone on that path: tau[a][b] += Q / L
    private fun depositAll(ants: List<Solution>) {
        for (ant in ants) {
            val l = ant.totalCost.coerceAtLeast(1e-9)
            val deltaTau = q / l

            for (route in ant.routes) {
                if (route.isEmpty()) continue

                // Since always pickup dropoff pairs we only need pickup for request order
                val reqSeq = ArrayList<Int>(route.size / 2)
                var idx = 0
                while (idx < route.size) {
                    val pickupIndex = route[idx]
                    val reqId = instance.requestIdOfIndex(pickupIndex)
                    reqSeq.add(reqId)
                    idx += 2
                }

                // start -> first
                tau[0][reqSeq[0]] += deltaTau

                // consecutive
                for (k in 0 until reqSeq.size - 1) {
                    val a = reqSeq[k]
                    val b = reqSeq[k + 1]
                    tau[a][b] += deltaTau
                }
            }
        }
    }
}