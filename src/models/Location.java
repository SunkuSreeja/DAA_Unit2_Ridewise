package models;

/**
 * Represents a geographic point in the city grid.
 * Used by both the map display and graph-based algorithms.
 */
public class Location {
    private String name;
    private double x; // x-coordinate on grid
    private double y; // y-coordinate on grid

    public Location(String name, double x, double y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public String getName() { return name; }
    public double getX()    { return x; }
    public double getY()    { return y; }

    /** Euclidean distance to another location */
    public double distanceTo(Location other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public String toString() {
        return String.format("%s (%.1f, %.1f)", name, x, y);
    }
}
