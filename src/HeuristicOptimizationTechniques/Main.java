package HeuristicOptimizationTechniques;

import HeuristicOptimizationTechniques.Helper.Instance;

public class Main {
    public static void main(String[] args) {

        String path = "instances/50/test/instance31_nreq50_nveh2_gamma50.txt";

        Instance parser = new Instance(path);

        System.out.println("Instance name: " + parser.getInstanceName());
        System.out.println("Requests: " + parser.getNumberOfRequest());
        System.out.println("Vehicles: " + parser.getNumberOfVehicles());
        System.out.println("Capacity: " + parser.getVehicleCapacity());
        System.out.println("Gamma: " + parser.getMinNumberOfRequestsFulfilled());
        System.out.println("Fairness weight: " + parser.getFairnessWeight());

        System.out.println("Depot: " + parser.getDepotLocation());

        for(Instance.Request r : parser.getRequests()){
            System.out.println(r);
        }
    }
}

