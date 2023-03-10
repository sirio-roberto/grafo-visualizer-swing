package visualizer;

import visualizer.customComponents.Vertex;
import visualizer.customComponents.WeightedEdge;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainFrame extends JFrame {

    public final int APP_WIDTH = 800;
    public final int APP_HEIGHT = 600;

    public Mode currentMode = Mode.VERTEX;

    private final JLabel currentModeLabel = new JLabel();

    private final JLabel displayLabel = new JLabel();

    private final JPanel graphPanel = new JPanel();

    private final List<Vertex> verticesForEdge = new ArrayList<>();

    private final HashSet<String> vertexNames = new HashSet<>();

    private final HashSet<WeightedEdge> graphEdges = new HashSet<>();
    private final HashSet<Vertex> graphVertices = new HashSet<>();

    private final HashSet<WeightedEdge> visitedEdges = new LinkedHashSet<>();

    private final HashSet<Vertex> visitedVertices = new LinkedHashSet<>();

    private AlgorithmMode algorithmMode;

    public MainFrame() {
        super("Graph-Algorithms Visualizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(APP_WIDTH, APP_HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);
        initComponents();
        setVisible(true);
        setLayout(null);
    }

    private void initComponents() {

        createAndSetMenuBar();
        updateCurrentModeLabel();

        graphPanel.setName("Graph");
        graphPanel.setBackground(Color.BLACK);
        graphPanel.setLayout(null);
        graphPanel.addMouseListener(graphMouseListener);

        currentModeLabel.setName(Mode.class.getSimpleName());
        currentModeLabel.setForeground(Color.WHITE);
        currentModeLabel.setBounds(580, 0, 200, 30);
        currentModeLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        displayLabel.setName("Display");
        displayLabel.setForeground(Color.WHITE);
        displayLabel.setBounds(0, APP_HEIGHT - 100, APP_WIDTH, 30);
        displayLabel.setHorizontalAlignment(SwingConstants.CENTER);

        add(currentModeLabel);
        add(displayLabel);
        add(graphPanel);
    }

    private void resetGraph() {
        graphPanel.removeAll();
        verticesForEdge.clear();
        vertexNames.clear();
        graphEdges.clear();
        graphVertices.clear();
        clearSelectedVerticesAndEdges();
        currentMode = Mode.VERTEX;
        updateCurrentModeLabel();
    }

    private final MouseListener graphMouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (currentMode == Mode.VERTEX) {
                    String vertexText = getVertexText();

                    if (vertexText != null) {
                        Vertex vertex = new Vertex(e.getX(), e.getY(), vertexText);
                        graphPanel.add(vertex);
                        vertexNames.add(vertexText);
                        graphVertices.add(vertex);
                        refreshGraph();

                        vertex.addMouseListener(vertexMouseListener);
                    }
                }
            }
        };

    MouseListener edgeMouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (currentMode == Mode.REMOVE_EDGE) {
                WeightedEdge.Edge clickedEdge = (WeightedEdge.Edge) e.getSource();
                WeightedEdge weightedEdge = clickedEdge.getParentEdge();
                removeWeightedEdgeComponents(weightedEdge);
                refreshGraph();
            }
        }
    };

    MouseListener vertexMouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            Vertex clickedVertex = (Vertex) e.getSource();
            if (currentMode == Mode.EDGE) {
                if (verticesForEdge.contains(clickedVertex)) {
                    clickedVertex.setDefaultColor();
                    refreshGraph();
                    verticesForEdge.remove(clickedVertex);
                } else {
                    clickedVertex.setSelectedColor();
                    refreshGraph();
                    verticesForEdge.add(clickedVertex);
                    if (verticesForEdge.size() == 2) {
                        Integer edgeWeight = getEdgeWeight();

                        if (edgeWeight != null) {
                            WeightedEdge weightedEdge = new WeightedEdge(verticesForEdge.get(0), verticesForEdge.get(1), edgeWeight);
                            graphEdges.add(weightedEdge);
                            weightedEdge.addMouseListener(edgeMouseListener);

                            WeightedEdge.Edge edge1 = weightedEdge.getEdge1();
                            edge1.addMouseListener(edgeMouseListener);

                            graphPanel.add(edge1);
                            graphPanel.add(weightedEdge.getEdge2());
                            graphPanel.add(weightedEdge.getWeightLabel());

                            clearVertices();
                            refreshGraph();
                        }
                    }
                }
            } else if (currentMode == Mode.REMOVE_VERTEX) {
                List<WeightedEdge> linkedEdges = getLinkedEdges(clickedVertex);
                for(WeightedEdge edge: linkedEdges) {
                    removeWeightedEdgeComponents(edge);
                    graphEdges.remove(edge);
                }
                graphPanel.remove(clickedVertex);
                verticesForEdge.remove(clickedVertex);
                graphVertices.remove(clickedVertex);
                refreshGraph();
            } else if (currentMode == Mode.NONE){
                displayLabel.setText("Please wait...");
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(() -> {
                    if (algorithmMode == AlgorithmMode.DFS) {
                        runDFS(clickedVertex);
                    } else if (algorithmMode == AlgorithmMode.BSF) {
                        runBFS(clickedVertex);
                    } else if (algorithmMode == AlgorithmMode.DIJKSTRA) {
                        runDijkstra(clickedVertex);
                    }
                });
            }
        }
    };

    private void runPrim() throws InterruptedException {
        List<WeightedEdge> sortedEdges = graphEdges.stream().sorted().toList();
        for (var edge: sortedEdges) {
            if (!visitedVertices.containsAll(edge.getVertices())) {
                visitedEdges.add(edge);
                edge.setSelectedColor();
                for (Vertex v : edge.getVertices()) {
                    visitedVertices.add(v);
                    v.setSelectedColor();
                }
                refreshGraph();
                Thread.sleep(200L);
            }
        }
        setDisplayResult(true);
        Thread.sleep(500L);
    }

    private void runDijkstra(Vertex clickedVertex) {
        Deque<Vertex> vertexDeque = new ArrayDeque<>();
        vertexDeque.add(clickedVertex);
        clickedVertex.setRootColor();
        clickedVertex.setDistance(0);
        refreshGraph();
        try {
            Thread.sleep(200L);
            while (!vertexDeque.isEmpty()) {
                visitedVertices.add(vertexDeque.peekFirst());
                Vertex currentVertex = vertexDeque.pollFirst();
                var linkedEdges = getLinkedEdges(currentVertex);
                for (var edge : linkedEdges) {
                    if (!visitedEdges.contains(edge)) {
                        for (Vertex v: edge.getVertices()) {
                            if (currentVertex != null && !v.equals(currentVertex)) {
                                calculateCurrentDistance(currentVertex, v, edge.getWeight());
                                vertexDeque.add(v);
                            }
                            visitedEdges.add(edge);
                            refreshGraph();
                            Thread.sleep(200L);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        setDisplayResult(false);
    }

    private void calculateCurrentDistance(Vertex source, Vertex next, int weight) {
        if (source.getDistance() + weight < next.getDistance()) {
            next.setDistance(source.getDistance() + weight);
        }
    }

    private void runDFS(Vertex clickedVertex) {
        try {
            visitLinkedVertices(clickedVertex);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        setDisplayResult("DFS : ");
    }

    private void setDisplayResult(String prefix) {
        StringBuilder sb = new StringBuilder(prefix);
        for (Vertex s: visitedVertices) {
            sb.append(s.getLabel()).append(" -> ");
        }
        displayLabel.setText(sb.substring(0, sb.lastIndexOf(" -> ")));
    }

    private void setDisplayResult(boolean isPrim) {
        StringBuilder sb = new StringBuilder();
        if (isPrim) {
            for (var edge: visitedEdges) {
                List<Vertex> edgeVertices = edge.getVertices();
                sb.append(edgeVertices.get(1).getLabel()).append("=");
                sb.append(edgeVertices.get(0).getLabel()).append(", ");
            }
        } else {
            for (Vertex v : visitedVertices) {
                if (v.getDistance() > 0) {
                    sb.append(v.getLabel()).append("=").append(v.getDistance()).append(", ");
                }
            }
        }
        displayLabel.setText(sb.substring(0, sb.lastIndexOf(", ")));
    }

    private void visitLinkedVertices(Vertex clickedVertex) throws InterruptedException {
        if (visitedVertices.size() != graphVertices.size()) {
            var edges = getLinkedEdges(clickedVertex);
            for (var edge: edges) {
                var twoVertices = edge.getVertices();
                if (!visitedEdges.contains(edge)) {
                    twoVertices.get(0).setSelectedColor();
                    visitedVertices.add(twoVertices.get(0));
                    visitedEdges.add(edge);
                    edge.setSelectedColor();
                    refreshGraph();
                    Thread.sleep(200L);
                    visitLinkedVertices(twoVertices.get(1));
                } else {
                    twoVertices.get(1).setSelectedColor();
                    visitedVertices.add(twoVertices.get(1));
                }
            }
        }
    }

    private void runBFS(Vertex clickedVertex) {
        Deque<Vertex> vertexDeque = new ArrayDeque<>();
        vertexDeque.add(clickedVertex);
        clickedVertex.setSelectedColor();
        refreshGraph();
        try {
            Thread.sleep(200L);
            while (!vertexDeque.isEmpty()) {
                visitedVertices.add(vertexDeque.peekFirst());
                Vertex currentVertex = vertexDeque.pollFirst();
                var linkedEdges = getLinkedEdges(currentVertex);
                for (var edge : linkedEdges) {
                    if (!visitedEdges.contains(edge)) {
                        for (Vertex v: edge.getVertices()) {
                            if (!v.equals(currentVertex)) {
                                vertexDeque.add(v);
                                visitedEdges.add(edge);

                                edge.setSelectedColor();
                                v.setSelectedColor();
                                refreshGraph();
                                Thread.sleep(200L);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        setDisplayResult("BFS : ");
    }

    private List<WeightedEdge> getLinkedEdges(Vertex vertex) {
        List<WeightedEdge> edges = new ArrayList<>();
        for(WeightedEdge edge: graphEdges) {
            if (edge.getVertices().contains(vertex)) {
                edges.add(edge);
            }
        }
        return edges.stream().sorted().toList();
    }

    private void removeWeightedEdgeComponents(WeightedEdge edge) {
        graphPanel.remove(edge.getWeightLabel());
        graphPanel.remove(edge.getEdge1());
        graphPanel.remove(edge.getEdge2());
    }

    private void clearVertices() {
        for (Vertex v: verticesForEdge) {
            v.setDefaultColor();
        }
        verticesForEdge.clear();
    }

    private Integer getEdgeWeight() {
        String edgeWeight;
        do {
            edgeWeight = JOptionPane.showInputDialog(graphPanel,
                    "Enter Weight:", "Edge",
                    JOptionPane.QUESTION_MESSAGE);
            if (edgeWeight == null) {
                break;
            }
        } while (!edgeWeight.matches("-?\\d+"));
        return edgeWeight == null ? null : Integer.parseInt(edgeWeight);
    }

    private String getVertexText() {
        String vertexText;
        do {
            vertexText = JOptionPane.showInputDialog(graphPanel,
                    "Enter the Vertex ID (Should be 1 char):", "Vertex",
                    JOptionPane.QUESTION_MESSAGE);
            if (vertexText == null) {
                break;
            }
        } while (vertexText.isBlank() || vertexText.trim().length() != 1 || vertexNames.contains(vertexText));

        return vertexText;
    }

    private void createAndSetMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu modeMenu = new JMenu(Mode.class.getSimpleName());
        JMenu fileMenu = new JMenu("File");
        JMenu algorithmsMenu = new JMenu("Algorithms");

        JMenuItem newMenu = new JMenuItem("New");
        newMenu.setName("New");
        JMenuItem exitMenu = new JMenuItem("Exit");
        exitMenu.setName("Exit");

        JMenuItem addVertex = new JMenuItem(Mode.VERTEX.label);
        addVertex.setName(Mode.VERTEX.label);
        JMenuItem addEdge = new JMenuItem(Mode.EDGE.label);
        addEdge.setName(Mode.EDGE.label);

        JMenuItem removeVertex = new JMenuItem(Mode.REMOVE_VERTEX.label);
        removeVertex.setName(Mode.REMOVE_VERTEX.label);
        JMenuItem removeEdge = new JMenuItem(Mode.REMOVE_EDGE.label);
        removeEdge.setName(Mode.REMOVE_EDGE.label);
        JMenuItem none = new JMenuItem(Mode.NONE.label);
        none.setName(Mode.NONE.label);

        JMenuItem DFSItem = new JMenuItem("Depth-First Search");
        DFSItem.setName("Depth-First Search");
        JMenuItem BFSItem = new JMenuItem("Breadth-First Search");
        BFSItem.setName("Breadth-First Search");
        JMenuItem DijkstraItem = new JMenuItem("Dijkstra's Algorithm");
        DijkstraItem.setName("Dijkstra's Algorithm");
        JMenuItem primItem = new JMenuItem("Prim's Algorithm");
        primItem.setName("Prim's Algorithm");

        fileMenu.add(newMenu);
        fileMenu.add(exitMenu);

        modeMenu.add(addVertex);
        modeMenu.add(addEdge);
        modeMenu.addSeparator();
        modeMenu.add(removeVertex);
        modeMenu.add(removeEdge);
        modeMenu.add(none);

        algorithmsMenu.add(DFSItem);
        algorithmsMenu.add(BFSItem);
        algorithmsMenu.add(DijkstraItem);
        algorithmsMenu.add(primItem);

        menuBar.add(fileMenu);
        menuBar.add(modeMenu);
        menuBar.add(algorithmsMenu);
        setJMenuBar(menuBar);

        newMenu.addActionListener(l -> resetGraph());
        exitMenu.addActionListener(l -> System.exit(0));

        ActionListener modeItemsActionListener = actionEvent -> {
            if (actionEvent.getActionCommand().equals(Mode.VERTEX.label)) {
                currentMode = Mode.VERTEX;
            } else if (actionEvent.getActionCommand().equals(Mode.EDGE.label)) {
                currentMode = Mode.EDGE;
            } else if (actionEvent.getActionCommand().equals(Mode.REMOVE_VERTEX.label)) {
                currentMode = Mode.REMOVE_VERTEX;
            } else if (actionEvent.getActionCommand().equals(Mode.REMOVE_EDGE.label)) {
                currentMode = Mode.REMOVE_EDGE;
            } else {
                currentMode = Mode.NONE;
            }
            updateCurrentModeLabel();
            clearSelectedVerticesAndEdges();
        };

        addVertex.addActionListener(modeItemsActionListener);
        addEdge.addActionListener(modeItemsActionListener);
        removeVertex.addActionListener(modeItemsActionListener);
        removeEdge.addActionListener(modeItemsActionListener);
        none.addActionListener(modeItemsActionListener);

        DFSItem.addActionListener(l -> {
            startAlgorithmMode();
            algorithmMode = AlgorithmMode.DFS;
        });

        BFSItem.addActionListener(l -> {
            startAlgorithmMode();
            algorithmMode = AlgorithmMode.BSF;
        });

        DijkstraItem.addActionListener(l -> {
            startAlgorithmMode();
            algorithmMode = AlgorithmMode.DIJKSTRA;
        });

        primItem.addActionListener(l -> {
            startAlgorithmMode();
            algorithmMode = AlgorithmMode.PRIM;
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(() -> {
                try {
                    runPrim();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

    private void startAlgorithmMode() {
        clearSelectedVerticesAndEdges();
        currentMode = Mode.NONE;
        updateCurrentModeLabel();
        displayLabel.setText("Please choose a starting vertex");
    }

    private void clearSelectedVerticesAndEdges() {
        visitedEdges.clear();
        visitedVertices.clear();
        graphEdges.forEach(WeightedEdge::setDefaultColor);
        graphVertices.forEach(Vertex::setDefaultColor);
        refreshGraph();
    }

    private void updateCurrentModeLabel() {
        currentModeLabel.setText("Current Mode -> " + currentMode.label);
        if (currentMode != Mode.NONE) {
            displayLabel.setText("");
        }
    }

    private void refreshGraph() {
        repaint();
        revalidate();
    }
}