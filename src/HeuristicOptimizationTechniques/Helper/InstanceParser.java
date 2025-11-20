package HeuristicOptimizationTechniques.Helper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class InstanceParser {
    private String instanceName;

    private int numberOfRequest; //n
    private int numberOfVehicles; //nk
    private int vehicleCapacity; //C
    private int minNumberOfRequestsFulfilled; //γ
    private double fairnessWeight; //ρ

    private int[] demands;

    private Location depotLocation;
    private Location[] pickupLocations;
    private Location[] dropOffLocations;

    public InstanceParser(String relativePath) { //f.e.: instances/50/test/instance31_nreq50_nveh2_gamma50.txt
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
            demands = new int[numberOfRequest];
            String demandsLine = br.readLine();
            if (demandsLine == null) {
                throw new IllegalArgumentException("No demands given.");
            }

            String[] tokensDemands = demandsLine.trim().split("\\s+");
            if (tokensDemands.length != numberOfRequest) {
                throw new IllegalArgumentException("there must be exactly " + numberOfRequest + " demands given.");
            }

            for (int i = 0; i < numberOfRequest; i++) {
                demands[i] = Integer.parseInt(tokensDemands[i]);
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
            pickupLocations = new Location[numberOfRequest];
            for (int i = 0; i < numberOfRequest; i++) {
                line = br.readLine();
                if (line == null) {
                    throw new IllegalArgumentException("No pickupLocation line given.");
                }
                String[] pickupLocationTokens = line.trim().split("\\s+");
                if (pickupLocationTokens.length != 2) {
                    throw new IllegalArgumentException("there must be exactly " + 2 + " values given.");
                }
                pickupLocations[i] = new Location(Integer.parseInt(pickupLocationTokens[0]), Integer.parseInt(pickupLocationTokens[1]));
            }

            //---------------------------------//
            // dropOff locations
            //---------------------------------//
            dropOffLocations = new Location[numberOfRequest];
            for (int i = 0; i < numberOfRequest; i++) {
                line = br.readLine();
                if (line == null) {
                    throw new IllegalArgumentException("No dropOffLocation line given.");
                }
                String[] dropOffLocationTokens = line.trim().split("\\s+");
                if (dropOffLocationTokens.length != 2) {
                    throw new IllegalArgumentException("there must be exactly " + 2 + " values given.");
                }
                dropOffLocations[i] = new Location(Integer.parseInt(dropOffLocationTokens[0]), Integer.parseInt(dropOffLocationTokens[1]));
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

    public int[] getDemands() {
        return demands;
    }

    public void setDemands(int[] demands) {
        this.demands = demands;
    }

    public Location getDepotLocation() {
        return depotLocation;
    }

    public void setDepotLocation(Location depotLocation) {
        this.depotLocation = depotLocation;
    }

    public Location[] getPickupLocations() {
        return pickupLocations;
    }

    public void setPickupLocations(Location[] pickupLocations) {
        this.pickupLocations = pickupLocations;
    }

    public Location[] getDropOffLocations() {
        return dropOffLocations;
    }

    public void setDropOffLocations(Location[] dropOffLocations) {
        this.dropOffLocations = dropOffLocations;
    }


    private void parseName(String path) {
        String normalizedPath = path.replace("\\", "/");
        instanceName = normalizedPath.substring(normalizedPath.lastIndexOf("/") + 1, normalizedPath.length() - 4);
    }

    public class Location {
        private int x;
        private int y;

        public Location(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }
    }
}


