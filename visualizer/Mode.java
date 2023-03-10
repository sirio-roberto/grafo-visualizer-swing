package visualizer;

public enum Mode {
    VERTEX("Add a Vertex"),
    EDGE("Add an Edge"),
    REMOVE_VERTEX("Remove a Vertex"),
    REMOVE_EDGE("Remove an Edge"),
    NONE("None");

    public final String label;

    Mode (String label) {
        this.label = label;
    }
}
