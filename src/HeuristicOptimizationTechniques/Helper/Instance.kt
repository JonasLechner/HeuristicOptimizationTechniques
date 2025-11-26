package HeuristicOptimizationTechniques.Helper

import java.io.*
import kotlin.math.ceil

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

    fun computeObjectiveFunction(routes: Routes, requestIndex: Int, targetVehicle: Int): Double {
        var objectiveFunction = 0.0
        var i = 0;
        for (route in routes) {
            if (i == targetVehicle) {
                objectiveFunction += computeRouteLength(route, requestIndex);
            } else {
                objectiveFunction += computeRouteLength(route);
            }
            ++i
        }
        objectiveFunction += fairnessWeight * (1 - computeFairness(
            routes,
            requestIndex,
            targetVehicle
        ));
        return objectiveFunction
    }

    fun computeObjectiveFunction(
        routes: Routes,
        requestIndex: Int,
        previousDropoff: Int,
        nextPickup: Int,
        targetVehicle: Int
    ): Double { //adding between previousDropoff and nextPickup
        var objectiveFunction = 0.0
        var i = 0;
        for (route in routes) {
            if (i == targetVehicle) {
                objectiveFunction += computeRouteLength(
                    route,
                    requestIndex,
                    previousDropoff,
                    nextPickup
                );
            } else {
                objectiveFunction += computeRouteLength(route);
            }
            ++i
        }
        objectiveFunction += fairnessWeight * (1 - computeFairness(
            routes,
            requestIndex,
            previousDropoff,
            nextPickup,
            targetVehicle
        ));
        return objectiveFunction
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
        return (sum * sum) / (numberOfVehicles * sumSquared)
    }

    fun calculateObjectiveFromSolution(solution: Solution, candidate: Candidate): Double {
        val delta = deltaCalculation(solution, candidate)

        val newSum = solution.sum() + delta
        val newSumSquared = solution.sumSquaredWithDelta(candidate.routeIndex, delta)

        if (newSumSquared == 0) {
            return 1.0
        }

        val fairness: Double =
            (newSum.toDouble() * newSum.toDouble()) / (numberOfVehicles.toDouble() * newSumSquared.toDouble());

        return newSum + fairnessWeight * (1 - fairness)
    }

    fun deltaCalculation(solution: Solution, candidate: Candidate): Int {
        val (newLoad, newUnload) = getLocationPairForRequest(candidate.requestId)
        val (newLoadIdx, newUnloadIdx) = getIndexPairForRequest(candidate.requestId)

        //new route
        if (candidate.routeIndex >= solution.routes.size) {
            return computeRouteLength(mutableListOf(newLoadIdx, newUnloadIdx))
        }

        val route = solution.routes[candidate.routeIndex]

        require(candidate.pickPos >= 0 && candidate.pickPos <= route.size) { "pickpos out of range" }
        //append
        val replaceLoc = if (route.size == candidate.pickPos) depotLocation else
            getLocationOf(route[candidate.pickPos])

        val prevOfReplace =
            if (candidate.pickPos == 0) depotLocation else getLocationOf(route[candidate.pickPos - 1])

        val delta = Location.distWithoutDepot(
            listOf(
                prevOfReplace,
                newLoad,
                newUnload,
                replaceLoc
            )
        ) - prevOfReplace.distance(replaceLoc)

        return delta
    }

    private fun computeFairness(
        routes: Routes,
        requestIndex: Int,
        targetVehicle: Int
    ): Double { //adding as last element
        var sum = 0.0
        var sumSquared = 0.0

        var i = 0
        for (route in routes) {
            val d = if (i == targetVehicle) {
                computeRouteLength(route, requestIndex)
            } else {
                computeRouteLength(route)
            }
            sum += d
            sumSquared += (d * d)
            ++i
        }

        if (sumSquared == 0.0) return 1.0
        return (sum * sum) / (numberOfVehicles * sumSquared)
    }

    private fun computeFairness(
        routes: Routes,
        requestIndex: Int,
        previousDropoff: Int,
        nextPickup: Int,
        targetVehicle: Int
    ): Double { //adding between previousDropoff and nextPickup
        var sum = 0.0
        var sumSquared = 0.0

        var i = 0
        for (route in routes) {
            val d = if (i == targetVehicle) {
                computeRouteLength(route, requestIndex, previousDropoff, nextPickup)
            } else {
                computeRouteLength(route)
            }
            sum += d
            sumSquared += (d * d)
            ++i
        }

        if (sumSquared == 0.0) return 1.0
        return (sum * sum) / (numberOfVehicles * sumSquared)
    }

    fun computeRouteLength(route: Route): Int {
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


    fun computeRouteLength(route: Route, requestIndex: Int): Int { //adding as last element
        val (one, two) = getIndexPairForRequest(requestIndex)
        val pickup = getLocationOf(one)
        val dropoff = getLocationOf(two)
        if (route.isEmpty()) return depotLocation.distance(pickup) + pickup.distance(dropoff) + dropoff.distance(
            depotLocation
        )

        var totalLength = 0
        var prev = depotLocation
        for (i in route) {
            val next = getLocationOf(i)
            totalLength += prev.distance(next)
            prev = next
        }
        return totalLength + prev.distance(pickup) + pickup.distance(dropoff) + dropoff.distance(
            depotLocation
        )
    }

    fun computeRouteLength(
        route: Route,
        requestIndex: Int,
        previousDropoff: Int,
        nextPickup: Int
    ): Int { //adding between previousDropoff and nextPickup
        val (one, two) = getIndexPairForRequest(requestIndex)
        val pickup = getLocationOf(one)
        val dropoff = getLocationOf(two)
        if (route.isEmpty()) return depotLocation.distance(pickup) + pickup.distance(dropoff) + dropoff.distance(
            depotLocation
        )

        val baseLength = computeRouteLength(route)
        val previousDropoffLocation = if (previousDropoff == -1) depotLocation else getLocationOf(
            getDropOffIndexForRequestId(previousDropoff - numberOfRequests)
        )
        val nextPickupLocation = if (nextPickup == -1) depotLocation else getLocationOf(
            getPickupIndexForRequestId(nextPickup - numberOfRequests)
        )

        val delta =
            previousDropoffLocation.distance(pickup) + pickup.distance(dropoff) + dropoff.distance(
                nextPickupLocation
            ) - previousDropoffLocation.distance(nextPickupLocation)
        return baseLength + delta
    }

    fun computeRouteLengthDelta(route: Route, requestIndex: Int): Int {
        val (one, two) = getIndexPairForRequest(requestIndex)
        val pickup = getLocationOf(one)
        val dropoff = getLocationOf(two)

        if (route.isEmpty()) return depotLocation.distance(pickup) + pickup.distance(dropoff) + dropoff.distance(
            depotLocation
        )

        return getLocationOf(route.last()).distance(pickup) +
                pickup.distance(dropoff) + dropoff.distance(depotLocation) -
                getLocationOf(route.last()).distance(depotLocation)
    }

    fun computeRouteLengthDelta(
        route: Route,
        requestIndex: Int,
        previousDropoff: Int,
        nextPickup: Int
    ): Int {
        val (one, two) = getIndexPairForRequest(requestIndex)
        val pickup = getLocationOf(one)
        val dropoff = getLocationOf(two)

        if (route.isEmpty()) return depotLocation.distance(pickup) + pickup.distance(dropoff) + dropoff.distance(
            depotLocation
        )

        val previousDropoffLocation =
            if (previousDropoff == -1) depotLocation else getLocationOf(
                getDropOffIndexForRequestId(previousDropoff - numberOfRequests)
            )

        val nextPickupLocation = if (nextPickup == -1) depotLocation else getLocationOf(
            getPickupIndexForRequestId(nextPickup - numberOfRequests)
        )

        val delta = previousDropoffLocation.distance(pickup) +
                pickup.distance(dropoff) + dropoff.distance(nextPickupLocation) -
                previousDropoffLocation.distance(nextPickupLocation)
        return delta + ceil(route.size.toDouble() * 1).toInt()  //penalty
    }

    fun computeRouteLengthDelta(
        route: Route,
        pickupIndex: Int,
        dropoffIndex: Int,
        previousDropoff: Int,
        nextPickup: Int
    ): Int {
        val pickup = getLocationOf(pickupIndex)
        val dropoff = getLocationOf(dropoffIndex)

        if (route.isEmpty()) return depotLocation.distance(pickup) + pickup.distance(dropoff) + dropoff.distance(
            depotLocation
        )

        val previousDropoffLocation = if (previousDropoff == -1) depotLocation else getLocationOf(previousDropoff)

        val nextPickupLocation = if (nextPickup == -1) depotLocation else getLocationOf(nextPickup)

        val delta = previousDropoffLocation.distance(pickup) +
                pickup.distance(dropoff) + dropoff.distance(nextPickupLocation) -
                previousDropoffLocation.distance(nextPickupLocation)
        return delta + ceil(route.size.toDouble() * 1).toInt()  //penalty
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

    fun isPickupIndex(locationIndex: Int) = locationIndex in 1..numberOfRequests
    fun isDropIndex(locationIndex: Int) =
        locationIndex in (numberOfRequests + 1)..(2 * numberOfRequests)

    private fun requestIdOfIndex(locationIndex: Int): Int {
        if (isPickupIndex(locationIndex)) {
            return locationIndex
        } else if (isDropIndex(locationIndex)) {
            return locationIndex - numberOfRequests
        }
        throw IllegalArgumentException("Index out of range: $locationIndex")
    }

    fun getRequestById(requestId: Int): Request {
        requestRangeLimit(requestId)
        return requests[requestId - 1]
    }

    fun getPickupIndexForRequestId(requestId: Int): Int {
        requestRangeLimit(requestId)
        return requestId
    }

    private fun requestRangeLimit(id: Int) {
        require(id > 0 && id <= numberOfRequests) {
            "id was $id, however first request has index 1! last request has index $numberOfRequests!"
        }
    }

    fun getDropOffIndexForRequestId(requestId: Int): Int {
        requestRangeLimit(requestId)
        return numberOfRequests + requestId
    }

    fun getIndexPairForRequest(requestId: Int): Pair<Int, Int> =
        Pair(getPickupIndexForRequestId(requestId), getDropOffIndexForRequestId(requestId))

    fun getLocationPairForRequest(requestId: Int): Pair<Location, Location> {
        val (id1, id2) = getIndexPairForRequest(requestId)

        return Pair(getLocationOf(id1), getLocationOf(id2))
    }

    fun getLocationOf(locationId: Int): Location {
        return locations[locationId - 1]
    }

    fun calculateDelta(solution: Solution, candidate: Candidate): Double {
        //TODO real delta computation

        val copy = solution.clone()
        applyCandidateToSolution(copy, candidate)
        return computeObjectiveFunction(copy.routes)
    }

    fun createLastInsertionCandidatesPerRequest(
        sol: Solution,
        requestId: Int
    ): List<Candidate> {
        val candidates = ArrayList<Candidate>()
        val (pickup, dropOff) = getIndexPairForRequest(requestId)
        val withNewRoute = mutableListOf(pickup, dropOff)

        // add in new route
        if (sol.routes.size < numberOfVehicles && isCapacityWithinBounds(withNewRoute)) {
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


