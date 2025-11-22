package HeuristicOptimizationTechniques.Algorithms;

import HeuristicOptimizationTechniques.Helper.Instance;
import HeuristicOptimizationTechniques.Helper.Location;
import HeuristicOptimizationTechniques.Helper.Request;

import java.util.*;


public class GreedyConstruction {
    private final Instance instance;
    private final int numberOfRequest;
    private final int numberOfVehicles;
    private final int vehicleCapacity;
    private final int minNumberOfRequestsFulfilled;

    private final Location depotLocation;

    private final Request[] requests;

    public GreedyConstruction(Instance instance) {
        this.instance = instance;
        this.numberOfRequest = instance.getNumberOfRequest();
        this.numberOfVehicles = instance.getNumberOfVehicles();
        this.vehicleCapacity = instance.getVehicleCapacity();
        this.minNumberOfRequestsFulfilled = instance.getMinNumberOfRequestsFulfilled();
        this.depotLocation = instance.getDepotLocation();
        this.requests = instance.getRequests();
    }



    public List<List<Integer>> construct() {
        // Append route for each vehicle
        List<List<Integer>> routes = new ArrayList<>();
        for (int k = 0; k < numberOfVehicles; k++) {
            routes.add(new ArrayList<>());
        }

        // Order by cheapest distance from depot to pickup
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < numberOfRequest; i++) {
            order.add(i);
        }
        order.sort(Comparator.comparingInt(i -> Instance.distance(depotLocation, requests[i].getPickupLocation())));

        int alreadyServed = 0;
        // Check all requests in sorted order
        for (int requestIndex : order) {

            if (alreadyServed >= minNumberOfRequestsFulfilled) {
                break;
            }
            Request r = requests[requestIndex];
            int demand = r.getDemand();
            if (demand > vehicleCapacity) {
                continue; // not possible
            }

            int pickupIndex = 1 + requestIndex;
            int dropOffIndex = 1 + numberOfRequest + requestIndex;

            // check for which vehicle the total cost is the smallest
            int bestK = -1;
            double bestDelta = Double.MAX_VALUE;

            for (int k = 0; k < numberOfVehicles; k++) {
                double delta = instance.computeObjectiveFunction(routes, requestIndex, k);

                if (delta < bestDelta) {
                    bestDelta = delta;
                    bestK = k;
                }
            }

            // update route from best vehicle
            routes.get(bestK).add(pickupIndex);
            routes.get(bestK).add(dropOffIndex);

            alreadyServed++;
        }

        return routes;
    }
}
