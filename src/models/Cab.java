package models;

/**
 * Represents a cab (driver) in the RideWise system.
 * Each cab has a position on the grid and an availability status.
 */
public class Cab {
    private static int idCounter = 1;

    private int id;
    private String driverName;
    private double x;
    private double y;
    private boolean available;
    private String vehicleType; // "Micro", "Mini", "Sedan", "SUV"

    public Cab(String driverName, double x, double y, String vehicleType) {
        this.id          = idCounter++;
        this.driverName  = driverName;
        this.x           = x;
        this.y           = y;
        this.vehicleType = vehicleType;
        this.available   = true;
    }

    /** Reset the static counter (used when resetting demo data) */
    public static void resetCounter() { idCounter = 1; }

    public int getId()             { return id; }
    public String getDriverName()  { return driverName; }
    public double getX()           { return x; }
    public double getY()           { return y; }
    public boolean isAvailable()   { return available; }
    public String getVehicleType() { return vehicleType; }

    public void setAvailable(boolean available) { this.available = available; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }

    /** Euclidean distance from this cab to a passenger's coordinates */
    public double distanceTo(double px, double py) {
        double dx = this.x - px;
        double dy = this.y - py;
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public String toString() {
        return String.format("Cab #%d [%s] - Driver: %s at (%.1f, %.1f) | %s",
                id, vehicleType, driverName, x, y, available ? "Available" : "Busy");
    }
}
