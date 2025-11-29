package HeuristicOptimizationTechniques;

import HeuristicOptimizationTechniques.Algorithms.GreedyConstruction;
import HeuristicOptimizationTechniques.Algorithms.LocalSearch;
import HeuristicOptimizationTechniques.Algorithms.Neighborhoods.VehicleMoveNeighborhood;
import HeuristicOptimizationTechniques.Algorithms.PilotSearch;
import HeuristicOptimizationTechniques.Algorithms.RandomizedConstruction;
import HeuristicOptimizationTechniques.Algorithms.TabuSearch;
import HeuristicOptimizationTechniques.Helper.Instance;
import HeuristicOptimizationTechniques.Helper.Request;
import HeuristicOptimizationTechniques.Helper.Solution;
import HeuristicOptimizationTechniques.Helper.StepFunction;

import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        String path = "instances/100/competition/instance61_nreq100_nveh2_gamma91.txt";
        Instance instance = new Instance(path);
        Instance instance1k = new Instance("instances/1000/competition/instance61_nreq1000_nveh20_gamma879.txt");

        System.out.println("Instance name: " + instance.getInstanceName());
        System.out.println("Requests: " + instance.getNumberOfRequests());
        System.out.println("Vehicles: " + instance.getNumberOfVehicles());
        System.out.println("Capacity: " + instance.getVehicleCapacity());
        System.out.println("Gamma: " + instance.getMinNumberOfRequestsFulfilled());
        System.out.println("Fairness weight: " + instance.getFairnessWeight());

        System.out.println("Depot: " + instance.getDepotLocation());

        for (Request r : instance.getRequests()) {
            //System.out.println(r);
        }

        /*GreedyConstruction gc = new GreedyConstruction(instance);
        List<List<Integer>> routes = gc.construct();
        instance.writeSolution("mySolution2.txt", routes, instance.getInstanceName());*/

        RandomizedConstruction rc = new RandomizedConstruction(instance, 1, 10);
        Solution solution = rc.construct();
        instance.writeSolution("mySolution2.txt", solution.getRoutes(), instance.getInstanceName());

        /*PilotSearch pilotSearch = new PilotSearch(instance, 10, 3);
        var solu = pilotSearch.construct();
        instance.writeSolution("mySolution2.txt", solu.getRoutes(), instance.getInstanceName());*/
        /*PilotSearch pilotSearch = new PilotSearch(instance, 5,5);
        pilotSearch.solve();*/

        LocalSearch localSearch = new LocalSearch(new VehicleMoveNeighborhood(instance), StepFunction.BEST_IMPROVEMENT, 15);
        var solulu = localSearch.improve(solution);
        instance.writeSolution("mySolution3.txt", solulu.getRoutes(), instance.getInstanceName());
        System.out.println("Instance totalcost: " + solulu.getTotalCost());

        /*GRASP grasp = new GRASP(instance, 15, new TwoSwapNeighborhood(instance));
        var soluludelulu = grasp.construct();
        instance.writeSolution("mySolution4.txt", soluludelulu.getRoutes(), instance.getInstanceName());*/
    }
}

