package HeuristicOptimizationTechniques.Helper

import java.util.ArrayList
import java.util.BitSet

typealias Route = MutableList<Int> //Tour without depot at start and end
typealias Routes = MutableList<Route>

class Solution(val nRequests: Int, val routesSize: Int) {
    val routes: Routes = ArrayList(routesSize)
    val fulfilledRequests: BitSet =
        BitSet(nRequests + 1) //n+ bits, bit==1 if request already fulfilled
    var totalCost: Double = 0.0 //total cost of the partial/full solution

    var sumsPerRoute: MutableList<Int> = MutableList(routesSize) { 0 }

    //copy constructor
    constructor(other: Solution) : this(other.nRequests, other.routesSize) {
        other.routes.forEach { r -> routes.add(r.toMutableList()) }
        fulfilledRequests.or(other.fulfilledRequests)
        totalCost = other.totalCost
        sumsPerRoute = other.sumsPerRoute.toMutableList()
    }

    fun setFulfilled(requestId: Int) {
        fulfilledRequests.set(requestId)
    }

    fun isFulfilled(requestId: Int): Boolean {
        return fulfilledRequests.get(requestId)
    }

    //how many requests are fulfilled
    fun fulfilledCount(): Int {
        return fulfilledRequests.cardinality()
    }

    fun clone(): Solution = Solution(this)

    fun sum(): Int {
        return sumsPerRoute.sum()
    }

    fun sumSquaredWithDelta(routeIndex: Int, delta: Int): Int {
        var sum = 0
        for ((idx, r) in sumsPerRoute.withIndex()) {
            if (idx == routeIndex) {
                sum += (delta + r) * (delta + r)
            } else {
                sum += r * r
            }
        }
        return sum
    }

    fun addDeltaToRouteCost(routeIndex: Int, amount: Int) {
        sumsPerRoute[routeIndex] += amount
    }

    override fun toString(): String {
        return routes.joinToString(separator = System.lineSeparator()) { route ->
            route.joinToString(separator = " ")
        }
    }
}
