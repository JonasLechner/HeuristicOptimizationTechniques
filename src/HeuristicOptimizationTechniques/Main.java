package HeuristicOptimizationTechniques;

import HeuristicOptimizationTechniques.Algorithms.GreedyConstruction;
import HeuristicOptimizationTechniques.Algorithms.PilotSearch;
import HeuristicOptimizationTechniques.Helper.Instance;
import HeuristicOptimizationTechniques.Helper.InstanceWrapper;
import HeuristicOptimizationTechniques.Helper.Request;
import HeuristicOptimizationTechniques.Helper.Solution;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {

        String path = "instances/50/test/instance40_nreq50_nveh2_gamma46.txt";

        Instance instance = new Instance(path);

        System.out.println("Instance name: " + instance.getInstanceName());
        System.out.println("Requests: " + instance.getNumberOfRequest());
        System.out.println("Vehicles: " + instance.getNumberOfVehicles());
        System.out.println("Capacity: " + instance.getVehicleCapacity());
        System.out.println("Gamma: " + instance.getMinNumberOfRequestsFulfilled());
        System.out.println("Fairness weight: " + instance.getFairnessWeight());

        System.out.println("Depot: " + instance.getDepotLocation());

        for (Request r : instance.getRequests()) {
            System.out.println(r);
        }

        //GreedyConstruction gc = new GreedyConstruction(instance);
        //List<List<Integer>> routes = gc.construct();
        //gc.writeSolution("mySolution.txt", routes, instance.getInstanceName());

        PilotSearch pilot = new PilotSearch(new InstanceWrapper(instance), null);
        Solution solution = pilot.solve();
        instance.writeSolution("mySolution.txt", solution.getRoutes(), instance.getInstanceName());
    }
}

