package HeuristicOptimizationTechniques.Helper

//Wrapper for Instance, will be replaced later
class Instance2(
    val instance: Instance
) {
    private val requestsById = instance.requests.associateBy { it.id }
    val nRequests: Int = instance.requests.size

    val depot = instance.depotLocation

    val requests = instance.requests

    val indexToLocation: List<Location> = run {
        val arr = ArrayList<Location>(2 * nRequests + 1)
        arr.add(instance.depotLocation)

        for (i in 1..nRequests) {
            arr.add(requestsById.getValue(i).pickupLocation)
        }

        for (i in 1..nRequests) {
            arr.add(requestsById.getValue(i).dropOffLocation)
        }

        arr
    }

    fun isPickupIndex(idx: Int) = idx in 1..nRequests
    fun isDropIndex(idx: Int) = idx in (nRequests + 1)..(2 * nRequests)
    fun requestIdOfIndex(idx: Int): Int =
        when {
            isPickupIndex(idx) -> idx
            isDropIndex(idx) -> idx - nRequests
            else -> throw IllegalArgumentException("Index out of range: $idx")
        }

    fun requestById(requestId: Int) = requestsById.getValue(requestId)
    fun pickupIndexFor(requestId: Int) = requestId
    fun dropIndexFor(requestId: Int) = nRequests + requestId
    fun locationOf(requestId: Int) = indexToLocation[requestId]
}
