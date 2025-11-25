package HeuristicOptimizationTechniques.Helper

import kotlin.math.ceil
import kotlin.math.sqrt

data class Location(var x: Int, var y: Int, var index: Int) {
    override fun toString(): String {
        return "(index: $index, coords: ($x, $y))"
    }

    fun distance(b: Location): Int {
        val dx = (x - b.x).toDouble()
        val dy = (y - b.y).toDouble()
        return ceil(sqrt(dx * dx + dy * dy)).toInt()
    }

    companion object {
        fun distWithoutDepot(route: List<Location>): Int =
            route.zipWithNext().sumOf { (a, b) -> a.distance(b) }
    }

}
