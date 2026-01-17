package HeuristicOptimizationTechniques;

import static java.lang.Math.max;
import static java.lang.Math.min;

import HeuristicOptimizationTechniques.Algorithms.*;

import HeuristicOptimizationTechniques.Algorithms.Neighborhoods.TwoSwapNeighborhood;
import HeuristicOptimizationTechniques.Helper.Instance;
import HeuristicOptimizationTechniques.Helper.Solution;
import HeuristicOptimizationTechniques.Helper.StopCondition;
import kotlin.random.RandomKt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        String path = "instances/100/competition/instance61_nreq100_nveh2_gamma91.txt";
        Instance i1 = new Instance(path);
        Instance i2 = new Instance("instances/1000/competition/instance61_nreq1000_nveh20_gamma879.txt");
        Instance i3 = new Instance("instances/2000/competition/instance61_nreq2000_nveh40_gamma1829.txt");
        Instance i4 = new Instance("instances/5000/competition/instance61_nreq5000_nveh100_gamma4448.txt");

        var instances = new ArrayList<>(List.of(i1, i2, i3));

        System.out.println("Instance name: " + i1.getInstanceName());
        System.out.println("Requests: " + i1.getNumberOfRequests());
        System.out.println("Vehicles: " + i1.getNumberOfVehicles());
        System.out.println("Capacity: " + i1.getVehicleCapacity());
        System.out.println("Gamma: " + i1.getMinNumberOfRequestsFulfilled());
        System.out.println("Fairness weight: " + i1.getFairnessWeight());

        System.out.println("Depot: " + i1.getDepotLocation());


        //ConstructionHeuristic newGreedyConstruction = new CompletelyRandomConstruction(i4);
        //var solu = newGreedyConstruction.construct();
        //System.out.println(solu.getTotalCost());

        var random = RandomKt.Random(1001);
        GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm(
                new CompletelyRandomConstruction(i2, random),
                i2,
                30,
                2,
                random,
                new ArrayList<StopCondition>(
                        List.of(new StopCondition.NoImprovementForKIterations(10),
                                new StopCondition.Time(30))
                ),
                .5
        );
        geneticAlgorithm.construct();

        /*
        var solution = geneticAlgorithm.construct();
        System.out.println("Value: " + i2.computeObjectiveFunction(solution.getRoutes()));
        System.out.println("Value: " + solution.fulfilledCount());
        i2.writeSolution("results/genetic/" + i2.getInstanceName() + ".txt", solution.getRoutes(), i2.getInstanceName());

        for (Instance instance : instances ) {
            var solution = getSolutionGRASP(instance);

            System.out.println("Value: " + instance.computeObjectiveFunction(solution.getRoutes()));
            instance.writeSolution("results/pilot/" + instance.getInstanceName() + ".txt", solution.getRoutes(), instance.getInstanceName());
        }

        AntColonyOptimization antColonyOptimization = new AntColonyOptimization(
                i2, 40,
                new ArrayList<StopCondition>(
                        List.of(new StopCondition.NoImprovementForKIterations(50),
                                new StopCondition.Time(60))
                ),
                .7, .8, 0.85,
                .5, null);
        var solutionAnt = antColonyOptimization.construct();

        System.out.println("Value: " + i2.computeObjectiveFunction(solutionAnt.getRoutes()));
        i2.writeSolution("results/ant/" + i2.getInstanceName() + ".txt", solutionAnt.getRoutes(), i2.getInstanceName());
         */
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

    private static Solution getSolutionPilot(Instance instance) {
        //LocalSearch ls = new LocalSearch(new TwoSwapNeighborhood(instance), StepFunction.BEST_IMPROVEMENT, new StopCondition.Iterations(50));
        PilotSearch ls = new PilotSearch(
                instance,
                3,
                3
        );
        return ls.construct();
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

