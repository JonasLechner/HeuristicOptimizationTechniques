package HeuristicOptimizationTechniques;

import HeuristicOptimizationTechniques.Algorithms.GreedyConstruction;
import HeuristicOptimizationTechniques.Helper.Instance;
import HeuristicOptimizationTechniques.Helper.Request;

import java.util.*;

public class Main {

    public static void main(String[] args) {

        String path = "instances/50/test/instance34_nreq50_nveh2_gamma47.txt";

        Instance instance = new Instance(path);

        System.out.println("Instance name: " + instance.getInstanceName());
        System.out.println("Requests: " + instance.getNumberOfRequest());
        System.out.println("Vehicles: " + instance.getNumberOfVehicles());
        System.out.println("Capacity: " + instance.getVehicleCapacity());
        System.out.println("Gamma: " + instance.getMinNumberOfRequestsFulfilled());
        System.out.println("Fairness weight: " + instance.getFairnessWeight());

        System.out.println("Depot: " + instance.getDepotLocation());

        for(Request r : instance.getRequests()){
            System.out.println(r);
        }

        GreedyConstruction gc = new GreedyConstruction(instance);

        List<List<Integer>> routes = gc.construct();
        System.out.println(instance.computeObjectiveFunction(routes));
        Instance.writeSolution("mySolution.txt", routes, instance.getInstanceName());
    }
}

