package models;

/**
 * Represents a weighted edge in the city road network graph.
 * Used by MST (Prim's / Kruskal's) and Dijkstra's algorithm.
 */
public class Edge implements Comparable<Edge> {
    private int    src;    // source vertex index
    private int    dest;   // destination vertex index
    private double weight; // road distance / cost in km

    public Edge(int src, int dest, double weight) {
        this.src    = src;
        this.dest   = dest;
        this.weight = weight;
    }

    public int    getSrc()    { return src; }
    public int    getDest()   { return dest; }
    public double getWeight() { return weight; }

    /** Natural ordering by weight — needed for Kruskal's sort */
    @Override
    public int compareTo(Edge other) {
        return Double.compare(this.weight, other.weight);
    }

    @Override
    public String toString() {
        return String.format("Edge(%d -> %d, %.2f km)", src, dest, weight);
    }
}
