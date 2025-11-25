package HeuristicOptimizationTechniques.Algorithms;

import HeuristicOptimizationTechniques.Helper.Instance;
import HeuristicOptimizationTechniques.Helper.Location;
import HeuristicOptimizationTechniques.Helper.Request;
import kotlin.Pair;

import java.util.*;

public class GreedyConstruction {
    private final Instance instance;
    private final int numberOfRequest;
    private final int numberOfVehicles;
    private final int vehicleCapacity;
    private final int minNumberOfRequestsFulfilled;

    public GreedyConstruction(Instance instance) {
        this.numberOfRequest = instance.getNumberOfRequests();
        this.numberOfVehicles = instance.getNumberOfVehicles();
        this.vehicleCapacity = instance.getVehicleCapacity();
        this.minNumberOfRequestsFulfilled = instance.getMinNumberOfRequestsFulfilled();
        this.instance = instance;
    }

    public List<List<Integer>> construct() {

        // Append route for each vehicle
        List<List<Integer>> routes = new ArrayList<>();
        for (int k = 0; k < numberOfVehicles; k++) {
            routes.add(new ArrayList<>());
        }

        // Order by cheapest demand
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < numberOfRequest; i++) {
            order.add(i + 1);
        }
        order.sort(Comparator.comparingInt(i -> instance.getRequestById(i).getDemand()));

        int alreadyServed = 0;
        int count = 0;
        // Check all requests in sorted order
        for (int requestIndex : order) {
            Pair<Integer, Integer> pair = instance.getIndexPairForRequest(requestIndex);
            var pickup = pair.component1();
            var dropoff = pair.component2();

            //first requests are assigned manually to avoid that one vehicle takes all requests
            if (count < numberOfVehicles) {
                routes.get(count).add(pickup);
                routes.get(count).add(dropoff);
                count++;
                alreadyServed++;
                continue;
            }
            count++;
            if (alreadyServed >= minNumberOfRequestsFulfilled) {
                break;
            }
            Request r = instance.getRequestById(requestIndex);
            int demand = r.getDemand();
            if (demand > vehicleCapacity) {
                continue; // not possible
            }

            int bestK = -1;
            int bestPosition = -1;
            double bestDelta = Integer.MAX_VALUE;

            for (int k = 0; k < numberOfVehicles; k++) {
                List<Integer> route = routes.get(k);
                for (int i = 0; i < route.size(); i+=2) {
                    int previousDropoff =  i == 0 ? -1 : route.get(i - 1);
                    int nextPickup =  i == route.size() - 2 ? -1 : route.get(i + 1);
                    double delta = instance.computeObjectiveFunction(routes, requestIndex, previousDropoff, nextPickup, k);
                    if (delta < bestDelta) {
                        bestDelta = delta;
                        bestK = k;
                        bestPosition = i;
                    }
                }
            }

            // update route from best vehicle
            routes.get(bestK).add(bestPosition, pickup);
            routes.get(bestK).add(bestPosition + 1, dropoff);
            //System.out.println("iteration: " + count);
            alreadyServed++;
        }
        System.out.println("score: " + instance.computeObjectiveFunction(routes));

        return routes;
    }

}
