package data;

import models.Cab;
import models.Edge;
import models.Location;
import models.RideRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Central data store for RideWise.
 * Holds all sample cabs, passengers, locations, and road edges.
 * Call loadDefaults() to reset to the initial sample data.
 */
public class DataStore {

    // ----------------------------------------------------------------
    // Singleton
    // ----------------------------------------------------------------
    private static final DataStore INSTANCE = new DataStore();
    public static DataStore getInstance() { return INSTANCE; }

    // ----------------------------------------------------------------
    // Data Collections
    // ----------------------------------------------------------------
    private List<Cab>         cabs         = new ArrayList<>();
    private List<RideRequest> requests     = new ArrayList<>();
    private List<Location>    locations    = new ArrayList<>(); // city hubs / graph vertices
    private double[][]        adjMatrix;                        // weighted adjacency matrix

    private DataStore() { loadDefaults(); }

    // ================================================================
    // DEFAULT SAMPLE DATA
    // ================================================================

    /**
     * Reset everything to the built-in sample dataset.
     * City: Fictional "Hyderabad Metro" with 9 key locations.
     */
    public void loadDefaults() {
        // Reset ID counters
        Cab.resetCounter();
        RideRequest.resetCounter();

        // ---- CABS ----
        cabs = new ArrayList<>();
        cabs.add(new Cab("Ravi Kumar",    2.0,  7.0, "Mini"));
        cabs.add(new Cab("Priya Sharma",  5.0,  3.0, "Sedan"));
        cabs.add(new Cab("Mohammed Ali",  8.0,  8.0, "Micro"));
        cabs.add(new Cab("Lakshmi Devi",  1.0,  2.0, "SUV"));
        cabs.add(new Cab("Arjun Reddy",   6.0,  6.0, "Mini"));
        cabs.add(new Cab("Sunita Patel",  9.0,  1.0, "Sedan"));

        // ---- RIDE REQUESTS (also used as job-sequencing jobs) ----
        requests = new ArrayList<>();
        //                          name             x     y    dest          DL  Reward
        requests.add(new RideRequest("Ananya Singh",  4.0, 5.0, "HITEC City",   2, 500));
        requests.add(new RideRequest("Kiran Rao",     7.0, 2.0, "Banjara Hills",1, 400));
        requests.add(new RideRequest("Deepa Nair",    3.0, 8.0, "Gachibowli",   3, 350));
        requests.add(new RideRequest("Suresh Babu",   6.0, 4.0, "Secunderabad", 2, 300));
        requests.add(new RideRequest("Meena Kapoor",  1.5, 6.0, "Kukatpally",   1, 250));
        requests.add(new RideRequest("Rahul Gupta",   8.5, 7.0, "Jubilee Hills",3, 200));
        requests.add(new RideRequest("Fatima Shaikh", 5.0, 9.0, "Ameerpet",     2, 450));
        requests.add(new RideRequest("Vijay Teja",    2.5, 3.5, "Dilsukhnagar", 3, 320));

        // ---- CITY LOCATIONS (Graph Vertices) ----
        // 9 real-ish Hyderabad landmarks used for MST and Dijkstra
        locations = new ArrayList<>();
        //                    name               x     y
        locations.add(new Location("Secunderabad",   1.0,  9.0));   // 0
        locations.add(new Location("HITEC City",     3.0,  7.0));   // 1
        locations.add(new Location("Gachibowli",     2.0,  5.0));   // 2
        locations.add(new Location("Banjara Hills",  5.0,  6.0));   // 3
        locations.add(new Location("Jubilee Hills",  4.0,  4.0));   // 4
        locations.add(new Location("Kukatpally",     6.0,  8.0));   // 5
        locations.add(new Location("Ameerpet",       7.0,  6.0));   // 6
        locations.add(new Location("Dilsukhnagar",   8.0,  4.0));   // 7
        locations.add(new Location("LB Nagar",       9.0,  2.0));   // 8

        // ---- ADJACENCY MATRIX (road distances in km) ----
        // Symmetric, undirected road network
        int n = locations.size();
        adjMatrix = new double[n][n];
        // Road connections (src, dest, distance in km):
        addRoad(0, 1, 4.5);   // Secunderabad ↔ HITEC City
        addRoad(0, 2, 5.0);   // Secunderabad ↔ Gachibowli
        addRoad(1, 2, 3.2);   // HITEC City  ↔ Gachibowli
        addRoad(1, 3, 3.8);   // HITEC City  ↔ Banjara Hills
        addRoad(1, 5, 3.0);   // HITEC City  ↔ Kukatpally
        addRoad(2, 4, 2.5);   // Gachibowli  ↔ Jubilee Hills
        addRoad(3, 4, 2.8);   // Banjara Hills ↔ Jubilee Hills
        addRoad(3, 6, 2.2);   // Banjara Hills ↔ Ameerpet
        addRoad(4, 6, 3.5);   // Jubilee Hills ↔ Ameerpet
        addRoad(4, 7, 4.0);   // Jubilee Hills ↔ Dilsukhnagar
        addRoad(5, 6, 2.0);   // Kukatpally   ↔ Ameerpet
        addRoad(6, 7, 2.6);   // Ameerpet     ↔ Dilsukhnagar
        addRoad(7, 8, 3.1);   // Dilsukhnagar ↔ LB Nagar
        addRoad(0, 5, 6.0);   // Secunderabad ↔ Kukatpally (ring road)
    }

    /** Add undirected road edge in the adjacency matrix */
    public void addRoad(int u, int v, double dist) {
        adjMatrix[u][v] = dist;
        adjMatrix[v][u] = dist;
    }

    // ================================================================
    // Edge List (derived from adjMatrix) — used by Kruskal's
    // ================================================================
    public List<Edge> getEdgeList() {
        List<Edge> edges = new ArrayList<>();
        int n = adjMatrix.length;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (adjMatrix[i][j] > 0) {
                    edges.add(new Edge(i, j, adjMatrix[i][j]));
                }
            }
        }
        return edges;
    }

    // ================================================================
    // Getters / Setters
    // ================================================================
    public List<Cab>         getCabs()      { return cabs; }
    public List<RideRequest> getRequests()  { return requests; }
    public List<Location>    getLocations() { return locations; }
    public double[][]        getAdjMatrix() { return adjMatrix; }

    public void addCab(Cab c)               { cabs.add(c); }
    public void addRequest(RideRequest r)   { requests.add(r); }
    public void addLocation(Location loc)   { /* also resize matrix — handled by UI */ }

    public void setCabs(List<Cab> cabs)             { this.cabs = cabs; }
    public void setRequests(List<RideRequest> r)    { this.requests = r; }
    public void setLocations(List<Location> locs)   { this.locations = locs; }
    public void setAdjMatrix(double[][] m)           { this.adjMatrix = m; }
}
