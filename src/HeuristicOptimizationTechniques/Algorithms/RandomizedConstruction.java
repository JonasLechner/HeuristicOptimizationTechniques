package HeuristicOptimizationTechniques.Algorithms;

import HeuristicOptimizationTechniques.Helper.Instance;
import HeuristicOptimizationTechniques.Helper.Logger;
import HeuristicOptimizationTechniques.Helper.Request;
import HeuristicOptimizationTechniques.Helper.Solution;
import kotlin.Pair;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class RandomizedConstruction implements ConstructionHeuristic {
    private final Instance instance;
    private final int numberOfRequest;
    private final int numberOfVehicles;
    private final int vehicleCapacity;
    private final int minNumberOfRequestsFulfilled;
    private final int numberOfIterations;
    private final int numberOfCandidatesToKeep;
    private final Logger logger;

    public RandomizedConstruction(Instance instance, int numberOfIterations, int numberOfCandidatesToKeep) {
        this.logger = Logger.Companion.getLogger(RandomizedConstruction.class.getName());
        this.numberOfRequest = instance.getNumberOfRequests();
        this.numberOfVehicles = instance.getNumberOfVehicles();
        this.vehicleCapacity = instance.getVehicleCapacity();
        this.minNumberOfRequestsFulfilled = instance.getMinNumberOfRequestsFulfilled();
        this.instance = instance;
        this.numberOfIterations = numberOfIterations;
        this.numberOfCandidatesToKeep = numberOfCandidatesToKeep;
    }

    @NotNull
    public Solution construct() {
        // Order by cheapest demand
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < numberOfRequest; i++) {
            order.add(i + 1);
        }
        order.sort(Comparator.comparingInt(i -> instance.getRequestById(i).getDemand()));

        double bestObjectiveFunction = Double.MAX_VALUE;
        List<List<Integer>> bestRoutes = new ArrayList<>();

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

                //adding each request to random vehicle
                Random random = new Random();
                int k = random.nextInt(numberOfVehicles);
                List<Integer> route = routes.get(k);
                for (int i = 0; i < route.size(); i += 2) {
                    int previousDropoff = i == 0 ? -1 : route.get(i - 1);
                    int nextPickup = i == route.size() - 2 ? -1 : route.get(i + 1);
                    double delta = instance.computeObjectiveFunction(routes, requestIndex, k);
                    //double delta = instance.computeObjectiveFunction(routes, requestIndex, previousDropoff, nextPickup, k);
                    candidates.add(new Candidate(k, i, delta));
                }

                if (candidates.isEmpty()) {
                    continue; // or break, depending on your logic
                }

                candidates.sort(Comparator.comparingDouble(c -> c.delta));

                int limit = Math.min(numberOfCandidatesToKeep, candidates.size());

                Candidate chosen = candidates.get(random.nextInt(limit));

                // update route from best vehicle
                routes.get(chosen.vehicle).add(chosen.position, pickup);
                routes.get(chosen.vehicle).add(chosen.position + 1, dropoff);

                alreadyServed++;
            }
            double objectivefunction = instance.computeObjectiveFunction(routes);
            if (objectivefunction < bestObjectiveFunction) {
                bestObjectiveFunction = objectivefunction;
                bestRoutes = routes;
            }
        }


        Solution solution = new Solution(instance);
        for (int v = 0; v < numberOfVehicles; v++) {
            solution.getRoutes().add(new ArrayList<>(bestRoutes.get(v)));

            int routeLength = instance.computeRouteLength(bestRoutes.get(v));
            solution.getSumsPerRoute().set(v, routeLength);
        }

        // Mark fulfilled requests
        for (List<Integer> route : bestRoutes) {
            for (int loc : route) {
                int reqId = instance.requestIdOfIndex(loc);
                solution.setFulfilled(reqId);
            }
        }

        solution.setTotalCost(bestObjectiveFunction);
        logger.info("Initial solution with cost: " + solution.getTotalCost());
        return solution;
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
