package HeuristicOptimizationTechniques.Helper;

public class Request {

    private int index;
    private int demand;
    private Location pickupLocation;
    private Location dropOffLocation;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getDemand() {
        return demand;
    }

    public void setDemand(int demand) {
        this.demand = demand;
    }

    public Location getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(Location pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public Location getDropOffLocation() {
        return dropOffLocation;
    }

    public void setDropOffLocation(Location dropOffLocation) {
        this.dropOffLocation = dropOffLocation;
    }

    public String toString() {
        return "Request " + index + ": " + "demand=" + demand + ", pickup=" + pickupLocation + ", dropoff=" + dropOffLocation;
    }
}
