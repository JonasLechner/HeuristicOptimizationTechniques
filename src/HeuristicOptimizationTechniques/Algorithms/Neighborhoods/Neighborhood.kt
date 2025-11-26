package HeuristicOptimizationTechniques.Algorithms.Neighborhoods

import HeuristicOptimizationTechniques.Helper.Solution

interface Neighborhood {
    /* Neighborhoods:
    - 2Swap: swap 2 stops in the route from one vehicle
    - VehicleSwap: Move one request(pickup + dropoff) to another vehicle. Determine position with instance.computeObjectiveFunction(routes, requestIndex, k);
    - FairnessSwap: Move one request from vehicle with most request to n vehicles with least request -> optimize fairness
     */
    fun createNeighborhood(solution : Solution) : List<Solution>?
}