package visualizer.customComponents;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Comparator;

public class WeightedEdge extends JComponent implements Comparable<WeightedEdge> {
    private static final String EDGE_NAME = "Edge <%s -> %s>";
    private static final String WEIGHT_LABEL_NAME = "EdgeLabel <%s -> %s>";
    private Color color;
    private final Edge edge1;
    private final Edge edge2;
    private final JLabel weightLabel;
    private final java.util.List<Vertex> vertices = new ArrayList<>();

    public WeightedEdge(Vertex v1, Vertex v2, int weight) {
        vertices.add(v1);
        vertices.add(v2);
        edge1 = new Edge(v1, v2);
        edge2 = new Edge(v2, v1);
        weightLabel = createWeightLabel(v1, v2, weight);
    }

    public java.util.List<Vertex> getVertices() {
        return vertices;
    }
    private static JLabel createWeightLabel(Vertex src, Vertex dest, int weight) {
        JLabel label = new JLabel(String.valueOf(weight));

        label.setLocation((src.getCenterX() + dest.getCenterX()) / 2 + 3, (src.getCenterY() + dest.getCenterY()) / 2 + 3);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 22));
        label.setSize(label.getPreferredSize());
        label.setForeground(Color.YELLOW);
        label.setName(String.format(WEIGHT_LABEL_NAME, src.getLabel(), dest.getLabel()));

        return label;
    }

    public Edge getEdge1() {
        return edge1;
    }

    public Edge getEdge2() {
        return edge2;
    }

    public JLabel getWeightLabel() {
        return weightLabel;
    }

    public int getWeight() {
        return Integer.parseInt(weightLabel.getText());
    }

    public void setDefaultColor() {
        color = Color.WHITE;
    }

    public void setSelectedColor() {
        color = Color.YELLOW;
    }

    @Override
    public int compareTo(WeightedEdge other) {
        return Integer.compare(this.getWeight(), other.getWeight());
    }

    public class Edge extends JComponent {
        private final boolean mainDiagonal;


        public Edge(Vertex src, Vertex dest) {
            int srcX = src.getCenterX();
            int srcY = src.getCenterY();
            int destX = dest.getCenterX();
            int destY = dest.getCenterY();

            setBackground(Color.WHITE);
            setDefaultColor();
            setBounds(Math.min(srcX, destX), Math.min(srcY, destY), Math.abs(srcX - destX), Math.abs(srcY - destY));
            mainDiagonal = (srcX - destX) * (srcY - destY) >= 0;
            setName(String.format(EDGE_NAME, src.getLabel(), dest.getLabel()));
            setLayout(null);
        }

        public WeightedEdge getParentEdge() {
            return WeightedEdge.this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(color);

            Graphics2D g2d = (Graphics2D) g;
            g2d.setStroke(new BasicStroke(4.0F));

            if (mainDiagonal) {
                g.drawLine(0, 0, getWidth(), getHeight());
            } else {
                g.drawLine(0, getHeight(), getWidth(), 0);
            }
        }

    }
}
