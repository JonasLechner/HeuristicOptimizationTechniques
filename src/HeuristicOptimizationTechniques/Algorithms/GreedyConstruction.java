package HeuristicOptimizationTechniques.Algorithms;

import HeuristicOptimizationTechniques.Helper.Instance;
import HeuristicOptimizationTechniques.Helper.Location;
import HeuristicOptimizationTechniques.Helper.Request;
import kotlin.Pair;

import java.util.*;

public class GreedyConstruction {
    private final Instance instance;
    private int numberOfRequest;
    private int numberOfVehicles;
    private int vehicleCapacity;
    private int minNumberOfRequestsFulfilled;

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

            if (alreadyServed >= minNumberOfRequestsFulfilled) {
                break;
            }
            Request r = instance.getRequestById(requestIndex);
            int demand = r.getDemand();
            if (demand > vehicleCapacity) {
                continue; // not possible
            }

            // check for which vehicle the extra cost is the smallest
            int bestK = -1;
            int bestDelta = Integer.MAX_VALUE;

            for (int k = 0; k < numberOfVehicles; k++) {
                List<Integer> route = routes.get(k);
                int delta = instance.computeRouteLengthDelta(route, requestIndex);
                if (delta < bestDelta) {
                    bestDelta = delta;
                    bestK = k;
                }
            }

            // update route from best vehicle
            routes.get(bestK).add(pickup);
            routes.get(bestK).add(dropoff);

            alreadyServed++;
        }
        System.out.println("score: " + instance.computeObjectiveFunction(routes));

        return routes;
    }

}
