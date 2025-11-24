package HeuristicOptimizationTechniques.Helper

import java.util.BitSet

typealias Route = MutableList<Int> //Tour without depot at start and end
typealias Routes = MutableList<Route>

class Solution(val nRequests: Int) {
    val routes: Routes = mutableListOf()
    val fulfilledRequests: BitSet =
        BitSet(nRequests + 1) //n+ bits, bit==1 if request already fulfilled
    var totalCost: Double = 0.0 //total cost of the partial/full solution

    //copy constructor
    constructor(other: Solution) : this(other.nRequests) {
        other.routes.forEach { r -> routes.add(r.toMutableList()) }
        fulfilledRequests.or(other.fulfilledRequests)
        totalCost = other.totalCost
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

    override fun toString(): String {
        return routes.joinToString(separator = System.lineSeparator()) { route ->
            route.joinToString(separator = " ")
        }
    }
}
