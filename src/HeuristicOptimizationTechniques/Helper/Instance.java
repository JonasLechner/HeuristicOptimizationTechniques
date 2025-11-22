package HeuristicOptimizationTechniques.Helper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;


public class Instance {
    private String instanceName;

    private int numberOfRequest; //n
    private int numberOfVehicles; //nk
    private int vehicleCapacity; //C
    private int minNumberOfRequestsFulfilled; //γ
    private double fairnessWeight; //ρ

    private Location depotLocation;

    private Request[] requests;

    public Instance(String relativePath) { //f.e.: instances/50/test/instance31_nreq50_nveh2_gamma50.txt
        parseName(relativePath);
        try (BufferedReader br = new BufferedReader(new FileReader(relativePath))) {
            //---------------------------------//
            // properties
            //---------------------------------//
            String firstLine = br.readLine();
            if (firstLine == null) {
                throw new IllegalArgumentException("File is empty.");
            }

            String[] tokens = firstLine.trim().split("\\s+");
            if (tokens.length != 5) {
                throw new IllegalArgumentException("First line must contain exactly 5 values");
            }

            numberOfRequest = Integer.parseInt(tokens[0]);
            numberOfVehicles = Integer.parseInt(tokens[1]);
            vehicleCapacity = Integer.parseInt(tokens[2]);
            minNumberOfRequestsFulfilled = Integer.parseInt(tokens[3]);
            fairnessWeight = Double.parseDouble(tokens[4]);

            //---------------------------------//
            // demands
            //---------------------------------//
            br.readLine(); //skip # demands line
            requests = new Request[numberOfRequest];
            String demandsLine = br.readLine();
            if (demandsLine == null) {
                throw new IllegalArgumentException("No demands given.");
            }

            String[] tokensDemands = demandsLine.trim().split("\\s+");
            if (tokensDemands.length != numberOfRequest) {
                throw new IllegalArgumentException("there must be exactly " + numberOfRequest + " demands given.");
            }

            for (int i = 0; i < numberOfRequest; i++) {
                requests[i] = new Request();
                requests[i].setDemand(Integer.parseInt(tokensDemands[i]));
            }

            //---------------------------------//
            // depot location
            //---------------------------------//
            br.readLine(); //skip # request locations line
            String xyDepotLine = br.readLine();

            if (xyDepotLine == null) {
                throw new IllegalArgumentException("No xyDepotLine given.");
            }

            String[] xyDepotTokens = xyDepotLine.trim().split("\\s+");
            if (xyDepotTokens.length != 2) {
                throw new IllegalArgumentException("there must be exactly " + 2 + " xyDepotTokens given.");
            }
            depotLocation = new Location(Integer.parseInt(xyDepotTokens[0]), Integer.parseInt(xyDepotTokens[1]));

            //---------------------------------//
            // pickup locations
            //---------------------------------//
            String line;
            for (int i = 0; i < numberOfRequest; i++) {
                line = br.readLine();
                if (line == null) {
                    throw new IllegalArgumentException("No pickupLocation line given.");
                }
                String[] pickupLocationTokens = line.trim().split("\\s+");
                if (pickupLocationTokens.length != 2) {
                    throw new IllegalArgumentException("there must be exactly " + 2 + " values given.");
                }
                requests[i].setPickupLocation(new Location(Integer.parseInt(pickupLocationTokens[0]), Integer.parseInt(pickupLocationTokens[1])));
            }

            //---------------------------------//
            // dropOff locations
            //---------------------------------//
            for (int i = 0; i < numberOfRequest; i++) {
                line = br.readLine();
                if (line == null) {
                    throw new IllegalArgumentException("No dropOffLocation line given.");
                }
                String[] dropOffLocationTokens = line.trim().split("\\s+");
                if (dropOffLocationTokens.length != 2) {
                    throw new IllegalArgumentException("there must be exactly " + 2 + " values given.");
                }
                requests[i].setDropOffLocation(new Location(Integer.parseInt(dropOffLocationTokens[0]), Integer.parseInt(dropOffLocationTokens[1])));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading instance file: " + instanceName, e);
        }
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public int getNumberOfRequest() {
        return numberOfRequest;
    }

    public void setNumberOfRequest(int numberOfRequest) {
        this.numberOfRequest = numberOfRequest;
    }

    public int getNumberOfVehicles() {
        return numberOfVehicles;
    }

    public void setNumberOfVehicles(int numberOfVehicles) {
        this.numberOfVehicles = numberOfVehicles;
    }

    public int getVehicleCapacity() {
        return vehicleCapacity;
    }

    public void setVehicleCapacity(int vehicleCapacity) {
        this.vehicleCapacity = vehicleCapacity;
    }

    public int getMinNumberOfRequestsFulfilled() {
        return minNumberOfRequestsFulfilled;
    }

    public void setMinNumberOfRequestsFulfilled(int minNumberOfRequestsFulfilled) {
        this.minNumberOfRequestsFulfilled = minNumberOfRequestsFulfilled;
    }

    public double getFairnessWeight() {
        return fairnessWeight;
    }

    public void setFairnessWeight(double fairnessWeight) {
        this.fairnessWeight = fairnessWeight;
    }

    public Location getDepotLocation() {
        return depotLocation;
    }

    public void setDepotLocation(Location depotLocation) {
        this.depotLocation = depotLocation;
    }

    public Request[] getRequests() {
        return requests;
    }

    public void setRequests(Request[] requests) {
        this.requests = requests;
    }

    private void parseName(String path) {
        String normalizedPath = path.replace("\\", "/");
        instanceName = normalizedPath.substring(normalizedPath.lastIndexOf("/") + 1, normalizedPath.length() - 4);
    }

    public double computeObjectiveFunction(List<List<Integer>> routes) {
        double objectiveFunction = 0.0;
        for (List<Integer> route : routes) {
            objectiveFunction += computeRouteLength(route);
        }
        objectiveFunction += fairnessWeight * (1 - computeFairness(routes));
        return objectiveFunction;
    }

    public double computeFairness(List<List<Integer>> routes) {

        double sum = 0.0;
        double sumSquared = 0.0;

        for (List<Integer> route : routes) {
            int d = computeRouteLength(route);
            sum += d;
            sumSquared += (d * d);
        }

        if (sumSquared == 0) {
            return 1.0;
        }

        return (sum * sum) / (routes.size() * sumSquared);
    }

    public int computeRouteLength (List<Integer> route) {
        if (route.isEmpty()) {
            return 0;
        }
        int totalLength = 0;

        Location prev = depotLocation;
        for (int i : route) {
            Location next = getLocationBySolutionIndex(i);
            totalLength += distance(prev, next);
            prev = next;
        }

        return  totalLength + distance(prev, depotLocation);
    }

    public Location getLocationBySolutionIndex(int index) {
        if (index == 0) {
            return depotLocation;
        }

        index--;
        if (index < numberOfRequest) { //pickup
            return requests[index].getPickupLocation();
        }
        else { //dropoff
            return requests[index - numberOfRequest].getDropOffLocation();
        }
    }

    public static int distance(Location a, Location b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        return (int) Math.ceil(Math.sqrt(dx * dx + dy * dy));
    }
}


