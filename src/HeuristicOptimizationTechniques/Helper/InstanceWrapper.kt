package HeuristicOptimizationTechniques.Helper

//Kotlin Wrapper for Java Instance
//TODO replace Instance entirely
class InstanceWrapper(
    val instance: Instance
) {
    private val requestsById = instance.requests.associateBy { it.id }
    val nRequests: Int = instance.numberOfRequest
    val minRequests: Int = instance.minNumberOfRequestsFulfilled
    val depot = instance.depotLocation

    val requests = instance.requests

    val capacity = instance.vehicleCapacity

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

    fun requestById(requestId: Int): Request = requestsById.getValue(requestId)
    fun pickupIndexFor(requestId: Int) = requestId
    fun dropIndexFor(requestId: Int) = nRequests + requestId

    fun indecesForRequest(requestId: Int): Pair<Int, Int> =
        Pair(pickupIndexFor(requestId), dropIndexFor(requestId))

    fun locationOf(locationId: Int) = indexToLocation[locationId]
}
