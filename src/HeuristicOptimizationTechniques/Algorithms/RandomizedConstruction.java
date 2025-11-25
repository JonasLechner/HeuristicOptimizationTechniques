package HeuristicOptimizationTechniques.Algorithms;

import HeuristicOptimizationTechniques.Helper.Instance;
import HeuristicOptimizationTechniques.Helper.Request;
import kotlin.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class RandomizedConstruction {
    private final Instance instance;
    private final int numberOfRequest;
    private final int numberOfVehicles;
    private final int vehicleCapacity;
    private final int minNumberOfRequestsFulfilled;
    private final int numberOfIterations;
    private final int numberOfCandidatesToKeep;

    public RandomizedConstruction(Instance instance, int numberOfIterations, int numberOfCandidatesToKeep) {
        this.numberOfRequest = instance.getNumberOfRequests();
        this.numberOfVehicles = instance.getNumberOfVehicles();
        this.vehicleCapacity = instance.getVehicleCapacity();
        this.minNumberOfRequestsFulfilled = instance.getMinNumberOfRequestsFulfilled();
        this.instance = instance;
        this.numberOfIterations = numberOfIterations;
        this.numberOfCandidatesToKeep = numberOfCandidatesToKeep;
    }

    public List<List<Integer>> construct() {

        // Order by cheapest demand
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < numberOfRequest; i++) {
            order.add(i + 1);
        }
        order.sort(Comparator.comparingInt(i -> instance.getRequestById(i).getDemand()));

        List<List<Integer>> bestRoutes = new ArrayList<>();
        double bestObjectiveFunction = Double.MAX_VALUE;

        for (int it = 0; it < numberOfIterations; ++it) {
            // Append route for each vehicle
            List<List<Integer>> routes = new ArrayList<>();
            for (int k = 0; k < numberOfVehicles; k++) {
                routes.add(new ArrayList<>());
            }

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

                List<Candidate> candidates = new ArrayList<>();

                for (int k = 0; k < numberOfVehicles; k++) {
                    List<Integer> route = routes.get(k);
                    for (int i = 0; i < route.size(); i+=2) {
                        int previousDropoff =  i == 0 ? -1 : route.get(i - 1);
                        int nextPickup =  i == route.size() - 2 ? -1 : route.get(i + 1);
                        double delta = instance.computeObjectiveFunction(routes, requestIndex, previousDropoff, nextPickup, k);
                        candidates.add(new Candidate(k, i, delta));
                    }
                }

                if (candidates.isEmpty()) {
                    continue; // or break, depending on your logic
                }

                candidates.sort(Comparator.comparingDouble(c -> c.delta));

                int limit = Math.min(numberOfCandidatesToKeep, candidates.size());

                Random rnd = new Random();
                Candidate chosen = candidates.get(rnd.nextInt(limit));

                // update route from best vehicle
                routes.get(chosen.vehicle).add(chosen.position, pickup);
                routes.get(chosen.vehicle).add(chosen.position + 1, dropoff);

                alreadyServed++;
            }
            System.out.println("score randomized: " + instance.computeObjectiveFunction(routes));
            if (instance.computeObjectiveFunction(routes) < bestObjectiveFunction) {
                bestObjectiveFunction = instance.computeObjectiveFunction(routes);
                bestRoutes = routes;
            }
        }

        System.out.println("best objective is: " + instance.computeObjectiveFunction(bestRoutes));

        return bestRoutes;
    }

    private static class Candidate {
        int vehicle;
        int position;
        double delta;

        Candidate(int vehicle, int position, double delta) {
            this.vehicle = vehicle;
            this.position = position;
            this.delta = delta;
        }
    }
}
