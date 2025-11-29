package HeuristicOptimizationTechniques.Helper

import java.util.ArrayList
import java.util.BitSet

typealias Route = MutableList<Int> //Tour without depot at start and end
typealias Routes = MutableList<Route>

class Solution(val instance: Instance) {
    val routes: Routes = ArrayList(numberOfVehicles)

    val numberOfRequests: Int
        get() = instance.numberOfRequests

    val numberOfVehicles: Int
        get() = instance.numberOfVehicles
    val fulfilledRequests: BitSet =
        BitSet(numberOfRequests + 1) //n+ bits, bit==1 if request already fulfilled
    var totalCost: Double = 0.0 //total cost of the partial/full solution
    var sumsPerRoute: MutableList<Int> = MutableList(numberOfVehicles) { 0 }

    //copy constructor
    constructor(other: Solution) : this(other.instance) {
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

    fun sumSquared(): Int {
        return sumsPerRoute.sumOf { s -> s * s }
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

    fun getIndexOfLongestRoute(): Int {
        var maxIndex = -1
        var maxValue = Int.MIN_VALUE

        for (i in sumsPerRoute.indices) {
            val value = sumsPerRoute[i]
            if (value > maxValue) {
                maxValue = value
                maxIndex = i
            }
        }
        return maxIndex
    }

    override fun toString(): String {
        return routes.joinToString(separator = System.lineSeparator()) { route ->
            route.joinToString(separator = " ")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Solution

        if (totalCost != other.totalCost) return false
        if (routes != other.routes) return false
        if (fulfilledRequests != other.fulfilledRequests) return false
        if (sumsPerRoute != other.sumsPerRoute) return false

        return true
    }

    //instance is ignored
    override fun hashCode(): Int {
        var result = totalCost.hashCode()
        result = 31 * result + routes.hashCode()
        result = 31 * result + fulfilledRequests.hashCode()
        result = 31 * result + sumsPerRoute.hashCode()
        return result
    }
}
