package models;

/**
 * Represents a passenger ride request.
 * Used by the Closest Pair algorithm (for cab matching) and
 * the Job Sequencing algorithm (scheduled as a "job" with deadline and reward).
 */
public class RideRequest {
    private static int idCounter = 1;

    private int id;
    private String passengerName;
    private double pickupX;
    private double pickupY;
    private String destination;

    // Job Sequencing fields
    private int    deadline;  // time slot by which this ride must start (1-indexed)
    private int    reward;    // profit/reward if ride is scheduled in time
    private boolean scheduled; // set by the greedy algorithm

    public RideRequest(String passengerName, double pickupX, double pickupY,
                       String destination, int deadline, int reward) {
        this.id            = idCounter++;
        this.passengerName = passengerName;
        this.pickupX       = pickupX;
        this.pickupY       = pickupY;
        this.destination   = destination;
        this.deadline      = deadline;
        this.reward        = reward;
        this.scheduled     = false;
    }

    public static void resetCounter() { idCounter = 1; }

    public int    getId()            { return id; }
    public String getPassengerName() { return passengerName; }
    public double getPickupX()       { return pickupX; }
    public double getPickupY()       { return pickupY; }
    public String getDestination()   { return destination; }
    public int    getDeadline()      { return deadline; }
    public int    getReward()        { return reward; }
    public boolean isScheduled()     { return scheduled; }

    public void setScheduled(boolean scheduled) { this.scheduled = scheduled; }
    public void setDeadline(int deadline)       { this.deadline = deadline; }
    public void setReward(int reward)           { this.reward = reward; }

    @Override
    public String toString() {
        return String.format("R#%d [%s] Pickup:(%.1f,%.1f) -> %s | Deadline: Slot %d | Reward: ₹%d",
                id, passengerName, pickupX, pickupY, destination, deadline, reward);
    }
}
