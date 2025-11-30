package HeuristicOptimizationTechniques;

import static java.lang.Math.max;
import static java.lang.Math.min;

import com.sun.jdi.ArrayReference;

import HeuristicOptimizationTechniques.Algorithms.GRASP;
import HeuristicOptimizationTechniques.Algorithms.GreedyConstruction;
import HeuristicOptimizationTechniques.Algorithms.LocalSearch;
import HeuristicOptimizationTechniques.Algorithms.Neighborhoods.TwoSwapNeighborhood;
import HeuristicOptimizationTechniques.Algorithms.Neighborhoods.VehicleMoveNeighborhood;
import HeuristicOptimizationTechniques.Algorithms.NewGreedyConstruction;
import HeuristicOptimizationTechniques.Algorithms.PilotSearch;
import HeuristicOptimizationTechniques.Algorithms.RandomizedConstruction;
import HeuristicOptimizationTechniques.Algorithms.TabuSearch;
import HeuristicOptimizationTechniques.Algorithms.VariableNeighborhoodDescent;
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

        for (Instance instance : instances
        ) {
            var solution = getSolutionGRASP(instance);

            System.out.println("Value: " + instance.computeObjectiveFunction(solution.getRoutes()));
            instance.writeSolution("results/grasp/" + instance.getInstanceName() + ".txt", solution.getRoutes(), instance.getInstanceName());
        }
    }

    private static Solution getSolution(Instance instance) {
        var maxCandidates = max(500, instance.getNumberOfRequests() / 5);
        var greedyConstruction = new NewGreedyConstruction(instance, false, maxCandidates);
        var solutionBefore = greedyConstruction.construct();

        //LocalSearch ls = new LocalSearch(new TwoSwapNeighborhood(instance), StepFunction.BEST_IMPROVEMENT, new StopCondition.Iterations(50));
        TabuSearch ls = new TabuSearch(
                new TwoSwapNeighborhood(instance),
                new StopCondition.Iterations(50),
                instance.getNumberOfRequests() / 10
        );

        return ls.improve(solutionBefore);
    }

    private static Solution getSolutionGRASP(Instance instance) {
        GRASP ls = new GRASP(
                instance,
                new TwoSwapNeighborhood(instance),
                new StopCondition.Iterations(3)
        );

        return ls.construct();
    }
}

