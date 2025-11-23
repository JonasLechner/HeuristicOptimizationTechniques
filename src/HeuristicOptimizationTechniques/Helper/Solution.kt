package HeuristicOptimizationTechniques.Helper

import java.util.BitSet

typealias Route = MutableList<Int> //Tour without depot at start and end
typealias Routes = MutableList<Route>

class Solution(val nRequests: Int) {
    val routes: Routes = mutableListOf()
    val assigned: BitSet = BitSet(nRequests + 1)
    var totalCost: Int = 0

    //copy constructor
    constructor(other: Solution) : this(other.nRequests) {
        other.routes.forEach { r -> routes.add(r.toMutableList()) }
        assigned.or(other.assigned)
        totalCost = other.totalCost
    }

    fun clone(): Solution = Solution(this)
}
