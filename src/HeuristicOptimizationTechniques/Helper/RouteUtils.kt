package HeuristicOptimizationTechniques.Helper

import kotlin.math.hypot
import kotlin.math.ceil

object RouteUtils {
    fun ceilEuc(a: Location, b: Location): Int =
        ceil(hypot((a.x - b.x).toDouble(), (a.y - b.y).toDouble())).toInt()

    fun routeCost(route: Route, inst: Instance2): Int {
        if (route.isEmpty()) return 0
        val depot = inst.depot
        var cost = 0

        // depot -> first
        cost += ceilEuc(depot, inst.locationOf(route[0]))
        for (i in 0 until route.size - 1) {
            cost += ceilEuc(inst.locationOf(route[i]), inst.locationOf(route[i + 1]))
        }
        // last -> depot
        cost += ceilEuc(inst.locationOf(route.last()), depot)
        return cost
    }

    // total cost
    fun totalCost(sol: Solution, inst: Instance2): Int {
        var sum = 0
        for (r in sol.routes) sum += routeCost(r, inst)
        return sum
    }

    // check if route is valid (do we have enough capacity?)
    fun isCapacityFeasible(route: Route, inst: Instance2, capacity: Int): Boolean {
        var load = 0
        for (idx in route) {
            if (inst.isPickupIndex(idx)) {
                val req = inst.requestById(inst.requestIdOfIndex(idx))
                load += req.demand
            } else {
                val req = inst.requestById(inst.requestIdOfIndex(idx))
                load -= req.demand
            }
            if (load !in 0..capacity) return false
        }
        return true
    }
}
