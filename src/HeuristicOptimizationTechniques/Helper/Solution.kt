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

    fun setAssigned(locationId: Int) {
        assert(locationId <= assigned.size())
        assigned.set(locationId)
    }

    fun isAssigned(locationId: Int): Boolean {
        return assigned.get(locationId)
    }

    fun assignedCount(): Int {
        return assigned.cardinality()
    }

    fun clone(): Solution = Solution(this)

    override fun toString(): String {
        return routes.joinToString(separator = System.lineSeparator()) { route ->
            route.joinToString(separator = " ")
        }
    }
}
