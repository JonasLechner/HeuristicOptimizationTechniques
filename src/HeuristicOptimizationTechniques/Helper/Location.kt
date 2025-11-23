package HeuristicOptimizationTechniques.Helper;

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

    private int distance(Location other) {
        double dx = this.x  - other.x;
        double dy = this.y - other.y;
        return (int) Math.ceil(Math.sqrt(dx * dx + dy * dy));
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
