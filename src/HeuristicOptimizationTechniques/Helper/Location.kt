package HeuristicOptimizationTechniques.Helper

data class Location(var x: Int, var y: Int, var index: Int) {
    override fun toString(): String {
        return "(index: $index, coords: ($x, $y))"
    }
}
