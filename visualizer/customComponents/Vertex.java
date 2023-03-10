package visualizer.customComponents;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class Vertex extends JPanel implements Comparable<Vertex> {
    private static final String VERTEX_NAME = "Vertex %s";
    private static final String LABEL_NAME = "VertexLabel %s";
    private final int SIZE = 50;
    private final String label;

    private Color color;

    private int distance;
    public Vertex(int x, int y, String label) {
        this.label = label;
        this.distance = Integer.MAX_VALUE;
        setName(String.format(VERTEX_NAME, label));
        setBounds(x - SIZE / 2, y - SIZE / 2, SIZE, SIZE);
        setBackground(Color.BLACK);
        setLayout(new GridBagLayout());
        setDefaultColor();

        JLabel vertexLabel = new JLabel(label);
        vertexLabel.setName(String.format(LABEL_NAME, label));
        add(vertexLabel);
    }

    public void setDefaultColor() {
        color = Color.WHITE;
    }

    public void setSelectedColor() {
        color = Color.YELLOW;
    }

    public void setRootColor() {
        color = Color.RED;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(color);
        g.fillOval(0, 0, SIZE, SIZE);
    }

    public String getLabel() {
        return label;
    }

    public int getCenterX() {
        return getX() + SIZE / 2;
    }

    public int getCenterY() {
        return getY() + SIZE / 2;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    @Override
    public int compareTo(Vertex other) {
        return Integer.compare(this.getDistance(), other.getDistance());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vertex vertex = (Vertex) o;
        return Objects.equals(label, vertex.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label);
    }
}
