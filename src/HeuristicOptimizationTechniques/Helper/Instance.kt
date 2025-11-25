package HeuristicOptimizationTechniques.Helper

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

class Instance(relativePath: String) {
    val instanceName: String
    val numberOfRequests: Int // n
    val numberOfVehicles: Int // nk
    val vehicleCapacity: Int // C
    val minNumberOfRequestsFulfilled: Int // γ
    val fairnessWeight: Double // ρ
    val depotLocation: Location
    val requests: List<Request>
    val locations: List<Location> //without Depot

    init {
        instanceName = parseName(relativePath)

        try {
            BufferedReader(FileReader(relativePath)).use { br ->
                // properties line
                val firstLine = br.readLine()
                    ?: throw IllegalArgumentException("File is empty.")

                val tokens = firstLine.trim().split(Regex("\\s+"))
                if (tokens.size != 5) throw IllegalArgumentException("First line must contain exactly 5 values")

                numberOfRequests = tokens[0].toInt()
                numberOfVehicles = tokens[1].toInt()
                vehicleCapacity = tokens[2].toInt()
                minNumberOfRequestsFulfilled = tokens[3].toInt()
                fairnessWeight = tokens[4].toDouble()

                // demands
                br.readLine() // skip "# demands" line
                requests = List(numberOfRequests) { i -> Request(i + 1) }
                val demandsLine =
                    br.readLine() ?: throw IllegalArgumentException("No demands given.")
                val tokensDemands = demandsLine.trim().split(Regex("\\s+"))
                if (tokensDemands.size != numberOfRequests)
                    throw IllegalArgumentException("there must be exactly $numberOfRequests demands given.")

                for (i in 0 until numberOfRequests) {
                    requests[i].demand = tokensDemands[i].toInt()
                }

                // depot
                br.readLine() // skip "# request locations" line
                val xyDepotLine =
                    br.readLine() ?: throw IllegalArgumentException("No xyDepotLine given.")
                val xyDepotTokens = xyDepotLine.trim().split(Regex("\\s+"))
                if (xyDepotTokens.size != 2)
                    throw IllegalArgumentException("there must be exactly 2 xyDepotTokens given.")
                depotLocation = Location(xyDepotTokens[0].toInt(), xyDepotTokens[1].toInt(), 0)

                // pickups
                var line: String?
                for (i in 0 until numberOfRequests) {
                    line = br.readLine()
                        ?: throw IllegalArgumentException("No pickupLocation line given.")
                    val pickupLocationTokens = line.trim().split(Regex("\\s+"))
                    if (pickupLocationTokens.size != 2) throw IllegalArgumentException("there must be exactly 2 values given.")
                    requests[i].pickupLocation = Location(
                        pickupLocationTokens[0].toInt(),
                        pickupLocationTokens[1].toInt(),
                        1 + i
                    )
                }

                // dropoffs
                for (i in 0 until numberOfRequests) {
                    line = br.readLine()
                        ?: throw IllegalArgumentException("No dropOffLocation line given.")
                    val dropOffLocationTokens = line.trim().split(Regex("\\s+"))
                    if (dropOffLocationTokens.size != 2) throw IllegalArgumentException("there must be exactly 2 values given.")
                    requests[i].dropOffLocation = Location(
                        dropOffLocationTokens[0].toInt(),
                        dropOffLocationTokens[1].toInt(),
                        numberOfRequests + i + 1
                    )
                }


                locations = ArrayList<Location>(2 * numberOfRequests + 1)
                for (r in requests) {
                    locations.add(r.pickupLocation)
                }
                for (r in requests) {
                    locations.add(r.dropOffLocation)
                }
            }
        } catch (e: IOException) {
            throw RuntimeException("Error reading instance file: $instanceName", e)
        }
    }

    private fun parseName(path: String): String {
        val normalizedPath = path.replace("\\", "/")

        return normalizedPath.substring(
            normalizedPath.lastIndexOf("/") + 1,
            normalizedPath.length - 4
        )
    }

    fun computeObjectiveFunction(routes: Routes): Double {
        var totalLength = 0.0
        for (route in routes) {
            totalLength += computeRouteLength(route)
        }
        return totalLength + fairnessWeight * (1 - computeFairness(routes))
    }

    private fun computeFairness(routes: Routes): Double {
        var sum = 0.0
        var sumSquared = 0.0

        for (route in routes) {
            val d = computeRouteLength(route).toDouble()
            sum += d
            sumSquared += (d * d)
        }

        if (sumSquared == 0.0) return 1.0
        return (sum * sum) / (numberOfRequests * sumSquared)
    }

    private fun computeRouteLength(route: Route): Int {
        if (route.isEmpty()) return 0
        var totalLength = 0

        var prev = depotLocation
        for (i in route) {
            val next = getLocationOf(i)
            totalLength += prev.distance(next)
            prev = next
        }
        return totalLength + prev.distance(depotLocation)
    }

    @Throws(IOException::class)
    fun writeSolution(path: String, routes: Routes, instanceNameToWrite: String) {
        BufferedWriter(FileWriter(path)).use { bw ->
            bw.write(instanceNameToWrite)
            bw.newLine()
            for (route in routes) {
                for (i in route.indices) {
                    if (i > 0) bw.write(" ")
                    bw.write(route[i].toString())
                }
                bw.newLine()
            }
        }
    }

    private fun isPickupIndex(locationIndex: Int) = locationIndex in 1..numberOfRequests
    private fun isDropIndex(locationIndex: Int) =
        locationIndex in (numberOfRequests + 1)..(2 * numberOfRequests)

    private fun requestIdOfIndex(locationIndex: Int): Int {
        if (isPickupIndex(locationIndex)) {
            return locationIndex
        } else if (isDropIndex(locationIndex)) {
            return locationIndex - numberOfRequests
        }
        throw IllegalArgumentException("Index out of range: $locationIndex")
    }

    private fun getRequestById(requestId: Int): Request {
        requestRangeLimit(requestId)
        return requests[requestId - 1]
    }

    private fun getPickupIndexForRequestId(requestId: Int): Int {
        requestRangeLimit(requestId)
        return requestId
    }

    private fun requestRangeLimit(id: Int) {
        require(id > 0 && id <= numberOfRequests) {
            "first request has index 1! last request has index n!"
        }
    }

    private fun getDropOffIndexForRequestId(requestId: Int): Int {
        requestRangeLimit(requestId)
        return numberOfRequests + requestId
    }

    fun getIndexPairForRequest(requestId: Int): Pair<Int, Int> =
        Pair(getPickupIndexForRequestId(requestId), getDropOffIndexForRequestId(requestId))

    fun getLocationOf(locationId: Int): Location {
        return locations[locationId - 1]
    }

    fun calculateDelta(solution: Solution, candidate: Candidate): Double {
        //TODO real delta computation

        val copy = solution.clone()
        applyCandidateToSolution(copy, candidate)
        return computeObjectiveFunction(copy.routes)
    }

    fun createLastInsertionCandidatesPerRequest(sol: Solution, requestId: Int): List<Candidate> {
        val candidates = ArrayList<Candidate>()
        val (pickup, dropOff) = getIndexPairForRequest(requestId)
        val withNewRoute = mutableListOf(pickup, dropOff)

        // add in new route
        if (isCapacityWithinBounds(withNewRoute)) {
            candidates.add(Candidate(requestId, sol.routes.size, 0, 1))
        }

        // add in existing route at last position
        for ((routeIdx, route) in sol.routes.withIndex()) {
            val tmp = ArrayList(route)
            tmp.addAll(withNewRoute)

            if (isCapacityWithinBounds(tmp)) {
                // tmp.size -2 is pickup index, tmp.size -1 is drop index (they were appended)
                candidates.add(Candidate(requestId, routeIdx, tmp.size - 2, tmp.size - 1))
            }
        }

        return candidates
    }

    fun createAllValidCandidates(sol: Solution): List<Candidate> {
        val all = ArrayList<Candidate>()
        for (rId in 1..this.numberOfRequests) {
            if (sol.isFulfilled(rId)) {
                continue
            }
            val candidates = createLastInsertionCandidatesPerRequest(sol, rId)
            all.addAll(candidates)
        }
        return all
    }

    fun applyCandidateToSolution(solution: Solution, candidate: Candidate) {
        require(candidate.routeIndex <= solution.routes.size) { "routeIndex out of bounds" }

        val (pickup, dropOff) = getIndexPairForRequest(candidate.requestId)

        if (candidate.routeIndex == solution.routes.size) {
            // add to new route
            solution.routes.add(mutableListOf(pickup, dropOff))
        } else {
            // add to existing route
            val toModify = solution.routes[candidate.routeIndex]
            toModify.add(candidate.pickPos, pickup)
            toModify.add(candidate.dropPos, dropOff)
        }

        // set request to fulfilled
        solution.setFulfilled(candidate.requestId)
    }


    fun isCapacityWithinBounds(route: Route): Boolean {
        var capacity = 0

        for (stopId in route) {
            val request = getRequestById(requestIdOfIndex(stopId))

            //if pickup -> increase, dropoff -> decrease
            if (isPickupIndex(stopId)) {
                capacity += request.demand
            } else {
                capacity -= request.demand
            }

            //capacity out of bounds
            if (capacity !in 0..vehicleCapacity)
                return false
        }

        return true
    }
}
