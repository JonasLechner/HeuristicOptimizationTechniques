package HeuristicOptimizationTechniques.Algorithms;

import static HeuristicOptimizationTechniques.Helper.Instance.distance;

import HeuristicOptimizationTechniques.Helper.Instance;
import HeuristicOptimizationTechniques.Helper.Location;
import HeuristicOptimizationTechniques.Helper.Request;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class GreedyConstruction {

    private int numberOfRequest;
    private int numberOfVehicles;
    private int vehicleCapacity;
    private int minNumberOfRequestsFulfilled;
    private double fairnessWeight;

    private Location depotLocation;

    private Request[] requests;

    public GreedyConstruction(Instance instance) {
        this.numberOfRequest = instance.getNumberOfRequest();
        this.numberOfVehicles = instance.getNumberOfVehicles();
        this.vehicleCapacity = instance.getVehicleCapacity();
        this.minNumberOfRequestsFulfilled = instance.getMinNumberOfRequestsFulfilled();
        this.fairnessWeight = instance.getFairnessWeight();
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
        order.sort(Comparator.comparingInt(i -> distance(depotLocation, requests[i].getPickupLocation())));

        int alreadyServed = 0;
        for (int i = 0; i < numberOfVehicles; i++) {
            //TODO fix it logically check if             if (demand > vehicleCapacity) {...
            int pickupIndex = 1 + order.get(i);
            int dropOffIndex = 1 + numberOfRequest + order.get(i);
            routes.get(i).add(pickupIndex);
            routes.get(i).add(dropOffIndex);
            ++alreadyServed;
        }

        int count = 0;

        // Check all requests in sorted order
        for (int requestIndex : order) {
            //first requests are assigned manually to avoid that one vehicle takes all routes
            if (count < numberOfVehicles) {
                count++;
                continue;
            }

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

            // check for which vehicle the extra cost is the smallest
            int bestK = -1;
            int bestDelta = Integer.MAX_VALUE;

            for (int k = 0; k < numberOfVehicles; k++) {
                List<Integer> route = routes.get(k);

                int delta = computeExtraCost(route, r);

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

    private int computeExtraCost(List<Integer> route, Request r) {

        Location pickup = r.getPickupLocation();
        Location dropoff = r.getDropOffLocation();

        //whole distance from depot -> pickup -> dropoff -> depots
        if (route.isEmpty()) {
            return distance(depotLocation, pickup) + distance(pickup, dropoff) + distance(dropoff, depotLocation);
        }

        int lastIndex = route.getLast();
        Location lastLoc = getLocationBySolutionIndex(lastIndex);

        //extra cost is lastLocation -> pickup -> dropoff -> depot - lastLocation -> depot
        return distance(lastLoc, pickup) + distance(pickup, dropoff) + distance(dropoff, depotLocation) - distance(lastLoc, depotLocation);
    }

    private Location getLocationBySolutionIndex(int index) {
        if (index == 0) {
            return depotLocation;
        }

        index--;
        if (index < numberOfRequest) { //pickup
            return requests[index].getPickupLocation();
        } else { //dropoff
            return requests[index - numberOfRequest].getDropOffLocation();
        }
    }


}
