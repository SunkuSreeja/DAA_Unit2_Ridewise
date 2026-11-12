package algorithms;

import models.RideRequest;
import java.util.*;

/**
 * ============================================================
 * JOB SEQUENCING WITH DEADLINES — Greedy Algorithm
 * ============================================================
 * Problem:
 *   Given n ride requests (jobs), each with a deadline and a reward,
 *   schedule at most one ride per time slot such that:
 *     - Each ride is completed by its deadline (slot ≤ deadline)
 *     - Total reward is maximized
 *
 * Greedy Strategy:
 *   1. Sort jobs in descending order of reward.
 *   2. For each job, try to assign the latest available slot
 *      at or before its deadline.
 *   3. If a slot is found → schedule the job; else → skip (miss).
 *
 * Time Complexity: O(n^2) — O(n log n) for sort + O(n*d) for slot search
 * Space Complexity: O(n)
 * ============================================================
 */
public class JobSequencingAlgorithm {

    /** Result returned after running the algorithm */
    public static class JSResult {
        public List<RideRequest> scheduledJobs = new ArrayList<>();
        public List<RideRequest> missedJobs    = new ArrayList<>();
        public int[] slotAssignment;         // slotAssignment[slot] = job id, -1 if empty
        public int totalReward;
        public List<String> steps            = new ArrayList<>();
    }

    /**
     * Run Greedy Job Sequencing on the given ride requests.
     * @param requests List of ride requests (jobs)
     * @return JSResult containing schedule, missed jobs, and total reward
     */
    public JSResult schedule(List<RideRequest> requests) {
        JSResult result = new JSResult();

        if (requests == null || requests.isEmpty()) {
            result.steps.add("No ride requests to schedule.");
            return result;
        }

        // Reset scheduled flags
        for (RideRequest r : requests) r.setScheduled(false);

        // Find the maximum deadline to know how many slots we need
        int maxDeadline = 0;
        for (RideRequest r : requests) {
            if (r.getDeadline() > maxDeadline) maxDeadline = r.getDeadline();
        }

        result.steps.add("Step 1: Found " + requests.size() + " ride requests.");
        result.steps.add("        Maximum deadline = Slot " + maxDeadline);

        // Sort by reward descending (greedy choice)
        List<RideRequest> sorted = new ArrayList<>(requests);
        sorted.sort((a, b) -> Integer.compare(b.getReward(), a.getReward()));

        result.steps.add("Step 2: Sorted requests by reward (descending):");
        for (int i = 0; i < sorted.size(); i++) {
            RideRequest r = sorted.get(i);
            result.steps.add(String.format("  [%d] %s → Reward=₹%d, Deadline=Slot %d",
                    i + 1, r.getPassengerName(), r.getReward(), r.getDeadline()));
        }

        // Slot array: index 1..maxDeadline, -1 = empty
        int[] slots = new int[maxDeadline + 1];
        Arrays.fill(slots, -1);
        // Track which request fills each slot (for display)
        RideRequest[] slotRequests = new RideRequest[maxDeadline + 1];

        result.steps.add("Step 3: Greedily assigning jobs to latest available slots:");

        for (RideRequest r : sorted) {
            // Try from deadline down to slot 1
            boolean placed = false;
            for (int s = Math.min(r.getDeadline(), maxDeadline); s >= 1; s--) {
                if (slots[s] == -1) {
                    slots[s] = r.getId();
                    slotRequests[s] = r;
                    r.setScheduled(true);
                    result.scheduledJobs.add(r);
                    result.totalReward += r.getReward();
                    result.steps.add(String.format(
                        "  ✓ Assigned [%s] (Reward=₹%d) → Slot %d",
                        r.getPassengerName(), r.getReward(), s));
                    placed = true;
                    break;
                }
            }
            if (!placed) {
                result.missedJobs.add(r);
                result.steps.add(String.format(
                    "  ✗ MISSED [%s] (Reward=₹%d, Deadline=Slot %d) — no slot available",
                    r.getPassengerName(), r.getReward(), r.getDeadline()));
            }
        }

        result.slotAssignment = slots;

        // Final schedule summary
        result.steps.add("Step 4: Final Schedule:");
        for (int s = 1; s <= maxDeadline; s++) {
            if (slotRequests[s] != null) {
                RideRequest r = slotRequests[s];
                result.steps.add(String.format(
                    "  Slot %d → %s (₹%d)", s, r.getPassengerName(), r.getReward()));
            } else {
                result.steps.add("  Slot " + s + " → [empty]");
            }
        }

        result.steps.add(String.format(
            "Step 5: Total scheduled rides = %d | Missed = %d | Total Reward = ₹%d",
            result.scheduledJobs.size(), result.missedJobs.size(), result.totalReward));

        return result;
    }
}
