package algorithms;

import models.Edge;
import models.Location;
import java.util.*;

/**
 * ============================================================
 * MINIMUM SPANNING TREE — Prim's & Kruskal's Algorithms
 * ============================================================
 *
 * PRIM'S ALGORITHM (Greedy, vertex-centric):
 *   1. Start from an arbitrary vertex (vertex 0).
 *   2. Maintain a visited set and a min-heap of edges.
 *   3. At each step pick the minimum-weight edge to an unvisited vertex.
 *   4. Repeat until all vertices are in the MST.
 *   Time Complexity: O(E log V) with a priority queue
 *
 * KRUSKAL'S ALGORITHM (Greedy, edge-centric):
 *   1. Sort all edges by weight ascending.
 *   2. Use Union-Find to detect cycles.
 *   3. Add an edge to the MST if it connects two different components.
 *   4. Stop when we have V-1 edges.
 *   Time Complexity: O(E log E)
 *
 * Application: Find the cheapest road network connecting all
 * cab service stations / city hubs.
 * ============================================================
 */
public class MSTAlgorithm {

    /** Result from either Prim's or Kruskal's */
    public static class MSTResult {
        public List<Edge> mstEdges = new ArrayList<>();
        public double totalCost;
        public List<String> steps = new ArrayList<>();
        public String algorithmName;
        public boolean connected = true; // false if graph is disconnected
    }

    // ============================================================
    // PRIM'S ALGORITHM
    // ============================================================

    /**
     * Run Prim's algorithm on the given adjacency matrix.
     * @param locations List of city locations (vertices)
     * @param adjMatrix Symmetric adjacency matrix; 0 or ∞ means no edge
     * @return MSTResult with selected edges and total cost
     */
    public MSTResult runPrims(List<Location> locations, double[][] adjMatrix) {
        MSTResult result = new MSTResult();
        result.algorithmName = "Prim's Algorithm";

        int n = locations.size();
        if (n == 0) { result.steps.add("No locations."); return result; }

        result.steps.add("=== PRIM'S ALGORITHM ===");
        result.steps.add("Start: Add vertex 0 (" + locations.get(0).getName() + ") to MST.");

        boolean[] inMST   = new boolean[n];
        double[]  key     = new double[n];   // min edge weight to include vertex
        int[]     parent  = new int[n];

        Arrays.fill(key, Double.MAX_VALUE);
        Arrays.fill(parent, -1);
        key[0] = 0.0;

        // Min-heap: [weight, vertex]
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingDouble(a -> a[1]));
        pq.offer(new int[]{0, 0}); // {vertex, key as int for heap — use wrapper}

        // Use a proper heap with double keys
        PriorityQueue<double[]> heap = new PriorityQueue<>(Comparator.comparingDouble(a -> a[0]));
        heap.offer(new double[]{0.0, 0}); // {key, vertex}

        int edgesAdded = 0;

        while (!heap.isEmpty() && edgesAdded < n - 1) {
            double[] top = heap.poll();
            double w  = top[0];
            int u = (int) top[1];

            if (inMST[u]) continue;
            inMST[u] = true;

            if (parent[u] != -1) {
                Edge e = new Edge(parent[u], u, w);
                result.mstEdges.add(e);
                result.totalCost += w;
                edgesAdded++;
                result.steps.add(String.format(
                    "  Added edge: %s → %s  (%.2f km)  | MST cost so far: %.2f km",
                    locations.get(parent[u]).getName(),
                    locations.get(u).getName(), w, result.totalCost));
            }

            // Relax neighbors
            for (int v = 0; v < n; v++) {
                double wt = adjMatrix[u][v];
                if (wt > 0 && wt < Double.MAX_VALUE && !inMST[v] && wt < key[v]) {
                    key[v]    = wt;
                    parent[v] = u;
                    heap.offer(new double[]{wt, v});
                    result.steps.add(String.format(
                        "    Updated key[%s] = %.2f (via %s)",
                        locations.get(v).getName(), wt, locations.get(u).getName()));
                }
            }
        }

        // Check connectivity
        for (boolean visited : inMST) {
            if (!visited) { result.connected = false; break; }
        }

        if (!result.connected) {
            result.steps.add("⚠ Graph is DISCONNECTED — MST does not span all vertices.");
        } else {
            result.steps.add(String.format(
                "✓ Prim's MST complete: %d edges selected | Total cost = %.2f km",
                result.mstEdges.size(), result.totalCost));
        }
        return result;
    }

    // ============================================================
    // KRUSKAL'S ALGORITHM
    // ============================================================

    /**
     * Run Kruskal's algorithm on the given edge list.
     * @param locations List of city locations (vertices)
     * @param edges     All edges in the graph
     * @return MSTResult with selected edges and total cost
     */
    public MSTResult runKruskals(List<Location> locations, List<Edge> edges) {
        MSTResult result = new MSTResult();
        result.algorithmName = "Kruskal's Algorithm";

        int n = locations.size();
        if (n == 0) { result.steps.add("No locations."); return result; }

        result.steps.add("=== KRUSKAL'S ALGORITHM ===");

        // Sort edges by weight ascending
        List<Edge> sortedEdges = new ArrayList<>(edges);
        Collections.sort(sortedEdges);

        result.steps.add("Step 1: Sorted " + sortedEdges.size() + " edges by weight:");
        for (Edge e : sortedEdges) {
            result.steps.add(String.format("  %s ↔ %s  %.2f km",
                    locations.get(e.getSrc()).getName(),
                    locations.get(e.getDest()).getName(),
                    e.getWeight()));
        }

        // Union-Find
        int[] parent = new int[n];
        int[] rank   = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;

        result.steps.add("Step 2: Processing edges (Union-Find to detect cycles):");

        for (Edge e : sortedEdges) {
            if (result.mstEdges.size() == n - 1) break;

            int rootSrc  = find(parent, e.getSrc());
            int rootDest = find(parent, e.getDest());

            if (rootSrc == rootDest) {
                result.steps.add(String.format(
                    "  ✗ SKIP %s ↔ %s (%.2f km) — would create a CYCLE",
                    locations.get(e.getSrc()).getName(),
                    locations.get(e.getDest()).getName(),
                    e.getWeight()));
            } else {
                union(parent, rank, rootSrc, rootDest);
                result.mstEdges.add(e);
                result.totalCost += e.getWeight();
                result.steps.add(String.format(
                    "  ✓ ADD  %s ↔ %s (%.2f km) — components merged | MST cost: %.2f km",
                    locations.get(e.getSrc()).getName(),
                    locations.get(e.getDest()).getName(),
                    e.getWeight(), result.totalCost));
            }
        }

        if (result.mstEdges.size() < n - 1) {
            result.connected = false;
            result.steps.add("⚠ Graph is DISCONNECTED — MST does not span all vertices.");
        } else {
            result.steps.add(String.format(
                "✓ Kruskal's MST complete: %d edges | Total cost = %.2f km",
                result.mstEdges.size(), result.totalCost));
        }
        return result;
    }

    // ============================================================
    // Union-Find helpers for Kruskal's
    // ============================================================

    /** Path-compression find */
    private int find(int[] parent, int x) {
        if (parent[x] != x) parent[x] = find(parent, parent[x]);
        return parent[x];
    }

    /** Union by rank */
    private void union(int[] parent, int[] rank, int x, int y) {
        if (rank[x] < rank[y]) {
            parent[x] = y;
        } else if (rank[x] > rank[y]) {
            parent[y] = x;
        } else {
            parent[y] = x;
            rank[x]++;
        }
    }
}
