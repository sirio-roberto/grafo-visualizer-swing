package visualizer;

public enum AlgorithmMode {
    DFS("Depth-First Search"),
    BSF("Breadth-First Search"),
    DIJKSTRA("Dijkstra's Algorithm"),
    PRIM("Prim's Algorithm");

    public final String label;

    AlgorithmMode(String label) {
        this.label = label;
    }
}
