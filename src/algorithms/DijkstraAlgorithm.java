package algorithms;

import models.Location;
import java.util.*;

/**
 * ============================================================
 * DIJKSTRA'S SHORTEST PATH ALGORITHM
 * ============================================================
 * Algorithm:
 *   1. Initialize distance to all vertices as ∞; source = 0.
 *   2. Use a min-heap (priority queue) keyed on current best distance.
 *   3. At each step, extract the vertex u with minimum distance.
 *   4. For each neighbor v of u, if dist[u] + w(u,v) < dist[v],
 *      update dist[v] and record predecessor.
 *   5. Repeat until destination is reached or heap is empty.
 *   6. Reconstruct path via predecessor array.
 *
 * Time Complexity: O((V + E) log V) with binary heap
 * Space Complexity: O(V)
 *
 * Application: Find the shortest road route between two city
 * locations in the RideWise road network.
 *
 * NOTE: This is distinct from Closest Pair — Dijkstra finds the
 * shortest PATH in a road graph; Closest Pair finds the nearest
 * cab using straight-line (Euclidean) distance.
 * ============================================================
 */
public class DijkstraAlgorithm {

    /** Result from Dijkstra's algorithm */
    public static class DijkstraResult {
        public List<Integer> path = new ArrayList<>();    // indices of locations on shortest path
        public List<String> pathNames = new ArrayList<>(); // names of locations on path
        public double totalDistance;
        public boolean reachable;
        public List<String> steps = new ArrayList<>();
        public double[] allDistances; // distance from source to every vertex
    }

    /**
     * Run Dijkstra's shortest path algorithm.
     * @param locations  List of all city locations (vertices)
     * @param adjMatrix  Weighted adjacency matrix (0 or ∞ = no edge)
     * @param srcIdx     Index of source location
     * @param destIdx    Index of destination location
     * @return DijkstraResult with path and distance
     */
    public DijkstraResult findShortestPath(List<Location> locations,
                                            double[][] adjMatrix,
                                            int srcIdx, int destIdx) {
        DijkstraResult result = new DijkstraResult();
        int n = locations.size();

        if (srcIdx < 0 || srcIdx >= n || destIdx < 0 || destIdx >= n) {
            result.reachable = false;
            result.steps.add("Invalid source or destination index.");
            return result;
        }

        if (srcIdx == destIdx) {
            result.reachable = true;
            result.totalDistance = 0;
            result.path.add(srcIdx);
            result.pathNames.add(locations.get(srcIdx).getName());
            result.steps.add("Source and destination are the same location.");
            return result;
        }

        result.steps.add("=== DIJKSTRA'S SHORTEST PATH ===");
        result.steps.add("Source     : " + locations.get(srcIdx).getName());
        result.steps.add("Destination: " + locations.get(destIdx).getName());
        result.steps.add("");

        // Initialize distances
        double[] dist = new double[n];
        int[]    prev = new int[n];
        boolean[] visited = new boolean[n];

        Arrays.fill(dist, Double.MAX_VALUE);
        Arrays.fill(prev, -1);
        dist[srcIdx] = 0.0;

        // Min-heap: {distance, vertex}
        PriorityQueue<double[]> pq =
            new PriorityQueue<>(Comparator.comparingDouble(a -> a[0]));
        pq.offer(new double[]{0.0, srcIdx});

        result.steps.add("Step 1: Initialize all distances to ∞, dist[source]=0.");
        result.steps.add("Step 2: Process vertices in order of shortest known distance:");
        result.steps.add("");

        while (!pq.isEmpty()) {
            double[] top = pq.poll();
            double d = top[0];
            int u = (int) top[1];

            if (visited[u]) continue;
            visited[u] = true;

            result.steps.add(String.format("  → Processing: %s (dist=%.2f km)",
                    locations.get(u).getName(), d));

            if (u == destIdx) break; // Early exit once destination is settled

            // Relax all neighbors
            for (int v = 0; v < n; v++) {
                double wt = adjMatrix[u][v];
                if (wt <= 0 || wt == Double.MAX_VALUE || visited[v]) continue;

                double newDist = dist[u] + wt;
                if (newDist < dist[v]) {
                    dist[v] = newDist;
                    prev[v] = u;
                    pq.offer(new double[]{newDist, v});
                    result.steps.add(String.format(
                        "    Relaxed: %s → %s  edge=%.2f | new dist=%.2f km",
                        locations.get(u).getName(),
                        locations.get(v).getName(), wt, newDist));
                }
            }
        }

        result.allDistances = dist;

        // Check reachability
        if (dist[destIdx] == Double.MAX_VALUE) {
            result.reachable = false;
            result.steps.add("");
            result.steps.add("✗ Destination is UNREACHABLE from source!");
            return result;
        }

        result.reachable = true;
        result.totalDistance = dist[destIdx];

        // Reconstruct path by backtracking through prev[]
        Deque<Integer> stack = new ArrayDeque<>();
        for (int cur = destIdx; cur != -1; cur = prev[cur]) {
            stack.push(cur);
        }

        result.steps.add("");
        result.steps.add("Step 3: Reconstruct shortest path by backtracking predecessors:");
        StringBuilder pathStr = new StringBuilder();
        while (!stack.isEmpty()) {
            int idx = stack.pop();
            result.path.add(idx);
            result.pathNames.add(locations.get(idx).getName());
            pathStr.append(locations.get(idx).getName());
            if (!stack.isEmpty()) pathStr.append(" → ");
        }

        result.steps.add("  Path: " + pathStr);
        result.steps.add(String.format("  Total Distance: %.2f km", result.totalDistance));
        result.steps.add("");
        result.steps.add("Step 4: All-distances table from " + locations.get(srcIdx).getName() + ":");
        for (int i = 0; i < n; i++) {
            String distStr = dist[i] == Double.MAX_VALUE ? "∞ (unreachable)" :
                             String.format("%.2f km", dist[i]);
            result.steps.add(String.format("  → %s : %s", locations.get(i).getName(), distStr));
        }

        return result;
    }
}
