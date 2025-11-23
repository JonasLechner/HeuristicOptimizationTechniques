package HeuristicOptimizationTechniques.Helper

import kotlin.math.hypot
import kotlin.math.ceil

object RouteUtils {
    fun ceilEuc(a: Location, b: Location): Int =
        ceil(hypot((a.x - b.x).toDouble(), (a.y - b.y).toDouble())).toInt()

    fun routeCost(route: Route, inst: InstanceWrapper): Int {
        if (route.isEmpty()) return 0
        val depot = inst.depot
        var cost = 0

        // depot -> first
        cost += ceilEuc(depot, inst.locationOf(route[0]))
        for (i in 0..<route.size - 1) {
            cost += ceilEuc(inst.locationOf(route[i]), inst.locationOf(route[i + 1]))
        }
        // last -> depot
        cost += ceilEuc(inst.locationOf(route.last()), depot)
        return cost
    }

    // total cost
    fun totalCost(sol: Solution, inst: InstanceWrapper): Int {
        var sum = 0
        for (r in sol.routes) sum += routeCost(r, inst)
        return sum
    }

    fun isCapacityFeasible(route: Route, inst: InstanceWrapper): Boolean {
        var capacity = 0

        for (stopId in route) {
            val request = inst.requestById(inst.requestIdOfIndex(stopId))

            if (inst.isPickupIndex(stopId)) {
                capacity += request.demand
            } else {
                capacity -= request.demand
            }

            if (capacity !in 0..inst.capacity)
                return false
        }

        return true
    }
}
