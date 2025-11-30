package HeuristicOptimizationTechniques;

import static java.lang.Math.max;
import static java.lang.Math.min;

import com.sun.jdi.ArrayReference;

import HeuristicOptimizationTechniques.Algorithms.GreedyConstruction;
import HeuristicOptimizationTechniques.Algorithms.LocalSearch;
import HeuristicOptimizationTechniques.Algorithms.Neighborhoods.TwoSwapNeighborhood;
import HeuristicOptimizationTechniques.Algorithms.Neighborhoods.VehicleMoveNeighborhood;
import HeuristicOptimizationTechniques.Algorithms.NewGreedyConstruction;
import HeuristicOptimizationTechniques.Algorithms.PilotSearch;
import HeuristicOptimizationTechniques.Algorithms.RandomizedConstruction;
import HeuristicOptimizationTechniques.Helper.Instance;
import HeuristicOptimizationTechniques.Helper.Request;
import HeuristicOptimizationTechniques.Helper.Solution;
import HeuristicOptimizationTechniques.Helper.SolutionRunner;
import HeuristicOptimizationTechniques.Helper.StepFunction;
import HeuristicOptimizationTechniques.Helper.StopCondition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        String path = "instances/100/competition/instance61_nreq100_nveh2_gamma91.txt";
        Instance i1 = new Instance(path);
        Instance i2 = new Instance("instances/1000/competition/instance61_nreq1000_nveh20_gamma879.txt");
        Instance i3 = new Instance("instances/2000/competition/instance61_nreq2000_nveh40_gamma1829.txt");

        var instances = new ArrayList<>(List.of(i1, i2, i3));

        System.out.println("Instance name: " + i1.getInstanceName());
        System.out.println("Requests: " + i1.getNumberOfRequests());
        System.out.println("Vehicles: " + i1.getNumberOfVehicles());
        System.out.println("Capacity: " + i1.getVehicleCapacity());
        System.out.println("Gamma: " + i1.getMinNumberOfRequestsFulfilled());
        System.out.println("Fairness weight: " + i1.getFairnessWeight());

        System.out.println("Depot: " + i1.getDepotLocation());

        for (Request r : i1.getRequests()) {
            //System.out.println(r);
        }

        /*GreedyConstruction gc = new GreedyConstruction(instance);
        List<List<Integer>> routes = gc.construct();
        instance.writeSolution("mySolution2.txt", routes, instance.getInstanceName());*/

        /*
        RandomizedConstruction rc = new RandomizedConstruction(instance1k, 1, 10);
        Solution solution = rc.construct();
        instance1k.writeSolution("mySolution2.txt", solution.getRoutes(), instance1k.getInstanceName());

        System.out.println("Instance totalcost: " + instance1k.computeObjectiveFunction(solution.getRoutes()));

         */

        /*PilotSearch pilotSearch = new PilotSearch(instance, 10, 3);
        var solu = pilotSearch.construct();
        instance.writeSolution("mySolution2.txt", solu.getRoutes(), instance.getInstanceName());*/
        /*PilotSearch pilotSearch = new PilotSearch(instance, 5,5);
        pilotSearch.solve();*/

        /*
        LocalSearch localSearch = new LocalSearch(new VehicleMoveNeighborhood(instance1k), StepFunction.FIRST_IMPROVEMENT, new StopCondition.Iterations(100));
        var solulu = localSearch.improve(solution);
        instance1k.writeSolution("mySolution3.txt", solulu.getRoutes(), instance1k.getInstanceName());
        System.out.println("Instance totalcost: " + instance1k.computeObjectiveFunction(solulu.getRoutes()));

         */

        //SolutionRunner.Companion.run();

        for (Instance i : instances
        ) {
            //var twentyPercent = min(20, i.getNumberOfRequests() / 100);
            var twentyPercent = max(500, i.getNumberOfRequests() / 5);
            var greedyConstruction = new NewGreedyConstruction(i, false, twentyPercent);
            var solutionBefore = greedyConstruction.construct();

            LocalSearch ls = new LocalSearch(new TwoSwapNeighborhood(i), StepFunction.BEST_IMPROVEMENT, new StopCondition.Iterations(50));
            var solution = ls.improve(solutionBefore);

            System.out.println("Value: " + i.computeObjectiveFunction(solution.getRoutes()));
            i.writeSolution("results/2swap/" + i.getInstanceName() + ".txt", solution.getRoutes(), i.getInstanceName());
        }


        /*GRASP grasp = new GRASP(instance, 15, new TwoSwapNeighborhood(instance));
        var soluludelulu = grasp.construct();
        instance.writeSolution("mySolution4.txt", soluludelulu.getRoutes(), instance.getInstanceName());*/
    }
}

