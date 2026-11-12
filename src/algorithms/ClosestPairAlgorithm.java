package algorithms;

import models.Cab;
import models.RideRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * CLOSEST PAIR OF POINTS — Divide and Conquer
 * ============================================================
 * Algorithm:
 *  1. Sort all points (cabs + passenger) by x-coordinate.
 *  2. Divide the array into two halves recursively.
 *  3. Find the minimum distance in each half.
 *  4. Merge: check the "strip" of points within distance d of
 *     the dividing line for a closer cross-pair.
 *  5. Return the overall minimum.
 *
 * Time Complexity: O(n log^2 n)  [O(n log n) with sorted-strip merge]
 * Space Complexity: O(n)
 *
 * Application here: Find the cab nearest to a passenger using
 * the divide-and-conquer approach instead of a naive O(n^2) scan.
 * ============================================================
 */
public class ClosestPairAlgorithm {

    /** A simple 2-D point that can represent either a cab or the passenger. */
    public static class Point {
        public double x, y;
        public String label; // e.g. "Cab #3 (Ravi)" or "Passenger"
        public Cab cab;      // non-null if this point represents a cab

        public Point(double x, double y, String label, Cab cab) {
            this.x = x; this.y = y; this.label = label; this.cab = cab;
        }
    }

    /** Result returned after running the algorithm */
    public static class CPResult {
        public Point point1, point2;
        public double distance;
        public List<String> steps = new ArrayList<>();

        public CPResult(Point p1, Point p2, double dist) {
            this.point1 = p1; this.point2 = p2; this.distance = dist;
        }
    }

    // ----------------------------------------------------------------
    // Public entry point
    // ----------------------------------------------------------------

    /**
     * Find the closest pair of points among all available cabs and the passenger.
     * @param passenger  The passenger's ride request
     * @param cabs       List of available cabs
     * @return CPResult containing the two nearest points and distance
     */
    public CPResult findClosestCab(RideRequest passenger, List<Cab> cabs) {
        if (cabs == null || cabs.isEmpty()) {
            return null; // handled by UI
        }

        // Build list of points: passenger + each available cab
        List<Point> points = new ArrayList<>();
        points.add(new Point(passenger.getPickupX(), passenger.getPickupY(),
                             "Passenger: " + passenger.getPassengerName(), null));
        for (Cab c : cabs) {
            if (c.isAvailable()) {
                points.add(new Point(c.getX(), c.getY(),
                                     "Cab #" + c.getId() + " (" + c.getDriverName() + ")", c));
            }
        }

        if (points.size() < 2) {
            return null; // no available cabs
        }

        List<String> steps = new ArrayList<>();
        steps.add("Step 1: Collected " + points.size() + " points "
                  + "(1 passenger + " + (points.size()-1) + " available cabs).");

        // Sort by x
        points.sort((a, b) -> Double.compare(a.x, b.x));
        steps.add("Step 2: Sorted points by x-coordinate.");
        StringBuilder sorted = new StringBuilder("  Sorted order: ");
        for (Point p : points) sorted.append(p.label).append("  ");
        steps.add(sorted.toString());

        Point[] arr = points.toArray(new Point[0]);
        steps.add("Step 3: Running Divide-and-Conquer closest-pair...");

        CPResult result = closestPairRec(arr, 0, arr.length - 1, steps);
        result.steps = steps;

        steps.add(String.format(
            "Step 4: Closest pair found: [%s] ↔ [%s]  Distance = %.4f units",
            result.point1.label, result.point2.label, result.distance));

        // Identify which point is the cab
        Point cabPt = result.point1.cab != null ? result.point1 : result.point2;
        steps.add("Step 5: Nearest cab to passenger is: " + cabPt.label);

        return result;
    }

    // ----------------------------------------------------------------
    // Recursive Divide-and-Conquer
    // ----------------------------------------------------------------

    private CPResult closestPairRec(Point[] pts, int left, int right, List<String> steps) {
        // Base cases — use brute force for small sub-arrays
        if (right - left <= 2) {
            return bruteForce(pts, left, right, steps);
        }

        int mid = (left + right) / 2;
        double midX = pts[mid].x;

        // Conquer
        CPResult leftResult  = closestPairRec(pts, left, mid,       steps);
        CPResult rightResult = closestPairRec(pts, mid + 1, right,  steps);

        // Choose the better of the two sub-results (tie → lower Cab ID wins)
        CPResult best;
        if (leftResult.distance < rightResult.distance - 1e-9) {
            best = leftResult;
        } else if (rightResult.distance < leftResult.distance - 1e-9) {
            best = rightResult;
        } else {
            // Equal distances: pick the one with the lower Cab ID
            best = minCabId(leftResult.point1, leftResult.point2)
                   <= minCabId(rightResult.point1, rightResult.point2)
                   ? leftResult : rightResult;
        }
        double d = best.distance;

        steps.add(String.format("  Merge: left-min=%.4f, right-min=%.4f → d=%.4f",
                  leftResult.distance, rightResult.distance, d));

        // Build strip: points within distance d of the dividing line
        List<Point> strip = new ArrayList<>();
        for (int i = left; i <= right; i++) {
            if (Math.abs(pts[i].x - midX) < d) {
                strip.add(pts[i]);
            }
        }

        // Sort strip by y for efficient comparison
        strip.sort((a, b) -> Double.compare(a.y, b.y));

        // Check strip pairs — use isBetter so ties resolve by lowest Cab ID
        CPResult stripResult = stripClosest(strip, d, steps);
        if (stripResult != null &&
                isBetter(stripResult.distance, stripResult.point1, stripResult.point2, best)) {
            best = stripResult;
        }

        return best;
    }

    /** Brute-force closest pair for small sub-arrays (≤3 points) */
    private CPResult bruteForce(Point[] pts, int left, int right, List<String> steps) {
        CPResult best = null;
        for (int i = left; i <= right; i++) {
            for (int j = i + 1; j <= right; j++) {
                double dist = euclidean(pts[i], pts[j]);
                steps.add(String.format("  BruteForce: dist(%s, %s) = %.4f",
                          pts[i].label, pts[j].label, dist));
                if (best == null || isBetter(dist, pts[i], pts[j], best)) {
                    best = new CPResult(pts[i], pts[j], dist);
                }
            }
        }
        return best;
    }

    /** Check strip pairs (at most 7 comparisons per point due to geometry) */
    private CPResult stripClosest(List<Point> strip, double d, List<String> steps) {
        CPResult best = null;
        int n = strip.size();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n && (strip.get(j).y - strip.get(i).y) < d; j++) {
                double dist = euclidean(strip.get(i), strip.get(j));
                if (best == null || isBetter(dist, strip.get(i), strip.get(j), best)) {
                    best = new CPResult(strip.get(i), strip.get(j), dist);
                    steps.add(String.format("  Strip closer pair: [%s] ↔ [%s] = %.4f",
                              strip.get(i).label, strip.get(j).label, dist));
                }
            }
        }
        return best;
    }

    /**
     * Returns true if the new pair (newDist, p1, p2) should replace the current best.
     * A pair is better if its distance is strictly smaller, OR if distances are equal
     * and the new pair involves a lower Cab ID (deterministic tie-breaker).
     */
    private boolean isBetter(double newDist, Point p1, Point p2, CPResult best) {
        if (newDist < best.distance - 1e-9) return true;
        if (newDist > best.distance + 1e-9) return false;
        // Tie: prefer the pair with the lower Cab ID
        int newId  = minCabId(p1, p2);
        int bestId = minCabId(best.point1, best.point2);
        return newId < bestId;
    }

    /** Returns the lowest Cab ID present in a pair of points (Integer.MAX_VALUE if none). */
    private int minCabId(Point p1, Point p2) {
        int id1 = (p1.cab != null) ? p1.cab.getId() : Integer.MAX_VALUE;
        int id2 = (p2.cab != null) ? p2.cab.getId() : Integer.MAX_VALUE;
        return Math.min(id1, id2);
    }

    /** Euclidean distance between two points */
    private double euclidean(Point a, Point b) {
        return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
    }
}
