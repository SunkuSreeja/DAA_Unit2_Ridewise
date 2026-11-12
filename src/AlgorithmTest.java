import algorithms.ClosestPairAlgorithm;
import algorithms.DijkstraAlgorithm;
import algorithms.JobSequencingAlgorithm;
import algorithms.MSTAlgorithm;
import data.DataStore;
import models.RideRequest;

/**
 * Headless test runner — verifies all four algorithms produce correct output.
 * Run: java -cp out AlgorithmTest
 */
public class AlgorithmTest {

    public static void main(String[] args) {
        DataStore ds = DataStore.getInstance();
        boolean allPassed = true;

        System.out.println("=================================================");
        System.out.println("  RideWise Algorithm Test Suite");
        System.out.println("=================================================\n");

        // ---- Test 1a: Closest Pair — unique nearest (Ananya Singh) ----
        System.out.println("--- TEST 1a: Closest Pair — Ananya Singh at (4,5) ---");
        try {
            ds.loadDefaults();
            ClosestPairAlgorithm cpa = new ClosestPairAlgorithm();
            RideRequest p = ds.getRequests().get(0); // Ananya at (4,5)
            ClosestPairAlgorithm.CPResult r = cpa.findClosestCab(p, ds.getCabs());
            if (r == null) throw new RuntimeException("Result is null");
            ClosestPairAlgorithm.Point cabPt = r.point1.cab != null ? r.point1 : r.point2;
            System.out.println("  Passenger : " + p.getPassengerName() + " at (" + p.getPickupX() + "," + p.getPickupY() + ")");
            System.out.println("  Nearest   : " + cabPt.label);
            System.out.printf ("  Distance  : %.4f%n", r.distance);
            // Ananya at (4,5):
            // Priya Sharma Cab#2 at (5,3): dist=sqrt((4-5)²+(5-3)²)=sqrt(1+4)=sqrt(5)≈2.2361
            // Arjun Reddy  Cab#5 at (6,6): dist=sqrt((4-6)²+(5-6)²)=sqrt(4+1)=sqrt(5)≈2.2361  TIE → Cab#2 wins
            double expected1a = Math.sqrt(5);
            boolean distOk = Math.abs(r.distance - expected1a) < 0.001;
            boolean cabOk  = cabPt.cab != null && cabPt.cab.getId() == 2; // Cab#2 wins tie (lower ID)
            if (distOk && cabOk) {
                System.out.printf("  PASS — Cab#2 Priya Sharma, dist ≈ %.4f (tie with Cab#5, Cab#2 wins)%n", expected1a);
            } else {
                System.out.printf("  FAIL — expected Cab#2 dist=%.4f, got cab=%s dist=%.4f%n",
                    expected1a, cabPt.label, r.distance);
                allPassed = false;
            }
        } catch (Exception e) {
            System.out.println("  FAIL: " + e.getMessage());
            allPassed = false;
        }

        // ---- Test 1b: Closest Pair — tied distance, tie-breaker by Cab ID (Kiran Rao) ----
        System.out.println("\n--- TEST 1b: Closest Pair — Kiran Rao at (7,2), tie-breaker ---");
        try {
            ds.loadDefaults();
            ClosestPairAlgorithm cpa = new ClosestPairAlgorithm();
            RideRequest p = ds.getRequests().get(1); // Kiran Rao at (7,2)
            ClosestPairAlgorithm.CPResult r = cpa.findClosestCab(p, ds.getCabs());
            if (r == null) throw new RuntimeException("Result is null");
            ClosestPairAlgorithm.Point cabPt = r.point1.cab != null ? r.point1 : r.point2;
            System.out.println("  Passenger  : " + p.getPassengerName() + " at (" + p.getPickupX() + "," + p.getPickupY() + ")");
            System.out.println("  Assigned   : " + cabPt.label);
            System.out.printf ("  Distance   : %.4f%n", r.distance);
            // Cab#2 Priya (5,3): dist=sqrt((7-5)²+(2-3)²)=sqrt(5)≈2.2361
            // Cab#6 Sunita (9,1): dist=sqrt((7-9)²+(2-1)²)=sqrt(5)≈2.2361  →  TIE → Cab#2 wins
            double expectedTie = Math.sqrt(5);
            boolean distOk = Math.abs(r.distance - expectedTie) < 0.001;
            boolean cabOk  = cabPt.cab != null && cabPt.cab.getId() == 2; // lowest ID in tie
            if (distOk && cabOk) {
                System.out.printf("  PASS — Cab#2 Priya Sharma wins tie, dist ≈ %.4f%n", expectedTie);
            } else {
                System.out.printf("  FAIL — expected Cab#2 dist=%.4f, got cab=%s dist=%.4f%n",
                    expectedTie, cabPt.label, r.distance);
                allPassed = false;
            }
        } catch (Exception e) {
            System.out.println("  FAIL: " + e.getMessage());
            allPassed = false;
        }

        // ---- Test 2: Job Sequencing ----
        System.out.println("\n--- TEST 2: Job Sequencing with Deadlines ---");
        try {
            ds.loadDefaults();
            JobSequencingAlgorithm jsa = new JobSequencingAlgorithm();
            JobSequencingAlgorithm.JSResult r = jsa.schedule(ds.getRequests());
            System.out.println("  Scheduled jobs: " + r.scheduledJobs.size());
            System.out.println("  Missed jobs:    " + r.missedJobs.size());
            System.out.println("  Total reward:   Rs." + r.totalReward);
            for (RideRequest rr : r.scheduledJobs) {
                System.out.println("    Scheduled: " + rr.getPassengerName() + " (Rs." + rr.getReward() + ")");
            }
            if (r.scheduledJobs.size() > 0 && r.totalReward > 0) {
                System.out.println("  PASS");
            } else {
                System.out.println("  FAIL: no scheduled jobs");
                allPassed = false;
            }
        } catch (Exception e) {
            System.out.println("  FAIL: " + e.getMessage());
            allPassed = false;
        }

        // ---- Test 3: MST (Prim's) ----
        System.out.println("\n--- TEST 3a: Prim's MST ---");
        try {
            ds.loadDefaults();
            MSTAlgorithm mst = new MSTAlgorithm();
            MSTAlgorithm.MSTResult r = mst.runPrims(ds.getLocations(), ds.getAdjMatrix());
            System.out.println("  Edges in MST: " + r.mstEdges.size());
            System.out.printf("  Total cost: %.2f km%n", r.totalCost);
            System.out.println("  Connected: " + r.connected);
            if (r.mstEdges.size() == ds.getLocations().size() - 1 && r.connected) {
                System.out.println("  PASS");
            } else {
                System.out.println("  FAIL: MST edge count should be " + (ds.getLocations().size()-1));
                allPassed = false;
            }
        } catch (Exception e) {
            System.out.println("  FAIL: " + e.getMessage());
            allPassed = false;
        }

        // ---- Test 3b: Kruskal's MST ----
        System.out.println("\n--- TEST 3b: Kruskal's MST ---");
        try {
            ds.loadDefaults();
            MSTAlgorithm mst = new MSTAlgorithm();
            MSTAlgorithm.MSTResult r = mst.runKruskals(ds.getLocations(), ds.getEdgeList());
            System.out.println("  Edges in MST: " + r.mstEdges.size());
            System.out.printf("  Total cost: %.2f km%n", r.totalCost);
            System.out.println("  Connected: " + r.connected);
            if (r.mstEdges.size() == ds.getLocations().size() - 1 && r.connected) {
                System.out.println("  PASS");
            } else {
                System.out.println("  FAIL: MST edge count should be " + (ds.getLocations().size()-1));
                allPassed = false;
            }
        } catch (Exception e) {
            System.out.println("  FAIL: " + e.getMessage());
            allPassed = false;
        }

        // ---- Test 4: Dijkstra ----
        System.out.println("\n--- TEST 4: Dijkstra Shortest Path ---");
        try {
            ds.loadDefaults();
            DijkstraAlgorithm dijk = new DijkstraAlgorithm();
            // Secunderabad (0) to LB Nagar (8)
            DijkstraAlgorithm.DijkstraResult r = dijk.findShortestPath(
                ds.getLocations(), ds.getAdjMatrix(), 0, 8);
            System.out.println("  Source: " + ds.getLocations().get(0).getName());
            System.out.println("  Destination: " + ds.getLocations().get(8).getName());
            System.out.println("  Reachable: " + r.reachable);
            System.out.println("  Path: " + String.join(" -> ", r.pathNames));
            System.out.printf("  Distance: %.2f km%n", r.totalDistance);
            if (r.reachable && r.totalDistance > 0 && r.path.size() >= 2) {
                System.out.println("  PASS");
            } else {
                System.out.println("  FAIL: no valid path found");
                allPassed = false;
            }
        } catch (Exception e) {
            System.out.println("  FAIL: " + e.getMessage());
            allPassed = false;
        }

        // ---- Edge Case Tests ----
        System.out.println("\n--- TEST 5: Edge Cases ---");

        // Empty cab list
        try {
            ClosestPairAlgorithm cpa = new ClosestPairAlgorithm();
            ClosestPairAlgorithm.CPResult r = cpa.findClosestCab(ds.getRequests().get(0),
                                                                  java.util.Collections.emptyList());
            System.out.println("  Empty cab list -> result is null: " + (r == null ? "PASS" : "FAIL"));
        } catch (Exception e) { System.out.println("  Empty cab list -> threw exception (FAIL): " + e); allPassed=false; }

        // Same src/dest in Dijkstra
        try {
            ds.loadDefaults();
            DijkstraAlgorithm dijk = new DijkstraAlgorithm();
            DijkstraAlgorithm.DijkstraResult r = dijk.findShortestPath(ds.getLocations(), ds.getAdjMatrix(), 3, 3);
            System.out.println("  Same src/dest Dijkstra -> dist=0: " + (r.totalDistance == 0 ? "PASS" : "FAIL ("+r.totalDistance+")"));
        } catch (Exception e) { System.out.println("  Same src/dest -> exception (FAIL): " + e); allPassed=false; }

        // Empty job list
        try {
            JobSequencingAlgorithm jsa = new JobSequencingAlgorithm();
            JobSequencingAlgorithm.JSResult r = jsa.schedule(java.util.Collections.emptyList());
            System.out.println("  Empty job list -> total reward=0: " + (r.totalReward == 0 ? "PASS" : "FAIL"));
        } catch (Exception e) { System.out.println("  Empty job list -> exception (FAIL): " + e); allPassed=false; }

        System.out.println("\n=================================================");
        System.out.println(allPassed ? "  ALL TESTS PASSED" : "  SOME TESTS FAILED — check above");
        System.out.println("=================================================");
    }
}
