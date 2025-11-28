package org.example.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.Collections;
import java.util.Random;

import org.example.algorithms.*;
import org.example.graph.Arc;
import org.example.graph.Connection;
import org.example.graph.Edge;
import org.example.graph.Vertex;


public class GraphPanel extends JPanel {

    private final List<Vertex> vertexList = new ArrayList<>();
    private final List<Connection> connectionList = new ArrayList<>();

    public enum ToolMode { ADD_NODE, REMOVE, CONNECT, DISCONNECT }
    public enum GraphType {DIRECTED, UNDIRECTED}
    private ToolMode currentMode = ToolMode.ADD_NODE;
    private GraphType currentGraphType = GraphType.UNDIRECTED;
    private Vertex selectedNode = null;
    private int nextVertexIdCounter = 1;
    private int nextConnectionIdCounter = 1;

    private List<Connection> mstEdges = new ArrayList<>();
    private boolean showMST = false;
    private double mstTotalWeight = 0.0;

    private List<Connection> bfsTreeEdges = new ArrayList<>();
    private List<Vertex> bfsVisitOrder = new ArrayList<>();
    private boolean showBFS = false;
    private Vertex bfsStartVertex = null;
    private Map<Vertex, Integer> bfsDistances = new HashMap<>();

    private List<Connection> dfsTreeEdges = new ArrayList<>();
    private List<Vertex> dfsVisitOrder = new ArrayList<>();
    private boolean showDFS = false;
    private Vertex dfsStartVertex = null;
    private Map<Vertex, Integer> dfsDiscoveryTimes = new HashMap<>();
    private Map<Vertex, Integer> dfsFinishTimes = new HashMap<>();

    private boolean showComponents = false;
    private Map<Vertex, Integer> componentMap = new HashMap<>();
    private int numberOfComponents = 0;

    private boolean showColoring = false;
    private Map<Vertex, Color> vertexColors = new HashMap<>();
    private int chromaticNumber = 0;

    private boolean showAStarPath = false;
    private List<Connection> aStarPathConnections = new ArrayList<>();
    private double aStarPathTotalWeight = 0.0;

    private boolean showPCVPath = false;
    private List<Connection> pcvPathConnections = new ArrayList<>();
    private double pcvTotalWeight = 0.0;

    private Interface ui;

    public GraphPanel(Interface ui) {
        this.ui = ui;
        MouseAdapter mouseListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMousePress(e.getPoint());
            }
        };
        addMouseListener(mouseListener);
    }

    public void addVertex(int x, int y) {
        boolean validName=true;
        String vertexName = "";
        do {
            validName=true;
            vertexName = JOptionPane.showInputDialog(
                    this,
                    "Digite o nome do vértice até 10 caracteres:",
                    "Nomear novo vértice",
                    JOptionPane.INFORMATION_MESSAGE
            );
            if(vertexName==null) {
                validName=false;
                break;
            }else if (vertexName.length()>10 || vertexName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nome do vértice deve possuir até 10 caracteres", "Nome Inválido", JOptionPane.ERROR_MESSAGE);
                validName = false;
            }else{
                for (Vertex v : vertexList) {
                    if(v.getId().equals(vertexName)) {
                        JOptionPane.showMessageDialog(this, "Já existe um vértice chamado: "+vertexName+"\nPor favor escolha outro nome.", "Vértice já existe", JOptionPane.ERROR_MESSAGE);
                        validName = false;
                    }
                }
            }
        }while (!validName);

        if (validName) {
            Vertex newVertex = new Vertex(vertexName, x, y);

            int addCoords = JOptionPane.showConfirmDialog(
                    this,
                    "Deseja adicionar coordenadas geográficas (latitude/longitude)?",
                    "Coordenadas Geográficas",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (addCoords == JOptionPane.YES_OPTION) {
                try {
                    String latStr = JOptionPane.showInputDialog(
                            this,
                            "Digite a latitude (ex: -25.0916):",
                            "Latitude",
                            JOptionPane.QUESTION_MESSAGE
                    );

                    if (latStr != null && !latStr.trim().isEmpty()) {
                        double latitude = Double.parseDouble(latStr.trim());

                        String lonStr = JOptionPane.showInputDialog(
                                this,
                                "Digite a longitude (ex: -50.1668):",
                                "Longitude",
                                JOptionPane.QUESTION_MESSAGE
                        );

                        if (lonStr != null && !lonStr.trim().isEmpty()) {
                            double longitude = Double.parseDouble(lonStr.trim());
                            newVertex.setLatitude(latitude);
                            newVertex.setLongitude(longitude);
                        }
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Coordenadas inválidas. Vértice será criado sem coordenadas geográficas.",
                            "Aviso",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            }

            this.vertexList.add(newVertex);
        }
    }


    public boolean removeVertex(Vertex vertex) {
        if (vertex == null) return false;

        connectionList.removeIf(connection ->
                connection.getSource().equals(vertex) || connection.getTarget().equals(vertex));

        for (Vertex v : vertexList) {
            v.getNeighbours().remove(vertex);
        }

        boolean removed = vertexList.remove(vertex);

        if (selectedNode == vertex) {
            selectedNode = null;
        }

        return removed;
    }

    public Connection addConnection(String connectionId, Vertex source, Vertex target, double weight) {
        if (source == null || target == null) return null;

        if (currentGraphType == GraphType.UNDIRECTED && hasConnectionBetween(source, target)) {
            System.out.println("Já existe uma aresta entre " + source.getId() + " e " + target.getId());
            return null;
        }

        if (currentGraphType == GraphType.DIRECTED && hasDirectedConnectionBetween(source, target)) {
            System.out.println("Já existe um arco de " + source.getId() + " para " + target.getId());
            return null;
        }

        if (connectionId == null || connectionId.isEmpty()) {
            String prefix = currentGraphType == GraphType.DIRECTED ? "A" : "E";
            connectionId = prefix + nextConnectionIdCounter++;
        }

        Connection newConnection;
        if (currentGraphType == GraphType.DIRECTED) {
            newConnection = new Arc(connectionId, source, target, weight);
        } else {
            newConnection = new Edge(connectionId, source, target, weight);
        }

        connectionList.add(newConnection);

        source.addNeighbour(target);
        if (currentGraphType == GraphType.UNDIRECTED) {
            target.addNeighbour(source);
        }

        return newConnection;
    }

    public boolean removeConnection(Connection connection) {
        if (connection == null) return false;

        boolean removed = connectionList.remove(connection);

        if (removed) {
            connection.getSource().getNeighbours().remove(connection.getTarget());
            if (currentGraphType == GraphType.UNDIRECTED) {
                connection.getTarget().getNeighbours().remove(connection.getSource());
            }
        }

        return removed;
    }

    public boolean removeConnectionBetween(Vertex v1, Vertex v2) {
        Connection connection = findConnectionBetween(v1, v2);
        return removeConnection(connection);
    }

    public boolean areAdjacent(Vertex v1, Vertex v2) {
        if (v1 == null || v2 == null) return false;
        return v1.getNeighbours().contains(v2);
    }

    public void checkVertexAdjacency() {
        if (vertexList.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "O grafo não possui vértices!",
                    "Verificar Adjacência",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (vertexList.size() < 2) {
            JOptionPane.showMessageDialog(
                    this,
                    "O grafo precisa ter pelo menos 2 vértices para verificar adjacência!",
                    "Verificar Adjacência",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String[] vertexIds = vertexList.stream()
                .map(Vertex::getId)
                .toArray(String[]::new);

        String vertex1Id = (String) JOptionPane.showInputDialog(
                this,
                "Selecione o primeiro vértice:",
                "Verificar Adjacência - Vértice 1",
                JOptionPane.QUESTION_MESSAGE,
                null,
                vertexIds,
                vertexIds[0]
        );

        if (vertex1Id == null) {
            return;
        }

        String vertex2Id = (String) JOptionPane.showInputDialog(
                this,
                "Selecione o segundo vértice:",
                "Verificar Adjacência - Vértice 2",
                JOptionPane.QUESTION_MESSAGE,
                null,
                vertexIds,
                vertexIds.length > 1 ? vertexIds[1] : vertexIds[0]
        );

        if (vertex2Id == null) {
            return;
        }

        Vertex vertex1 = vertexList.stream()
                .filter(v -> v.getId().equals(vertex1Id))
                .findFirst()
                .orElse(null);

        Vertex vertex2 = vertexList.stream()
                .filter(v -> v.getId().equals(vertex2Id))
                .findFirst()
                .orElse(null);

        if (vertex1 == null || vertex2 == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Erro ao encontrar os vértices selecionados!",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        boolean adjacent = areAdjacent(vertex1, vertex2);

        String connectionType = currentGraphType == GraphType.DIRECTED ? "arco" : "aresta";
        String message;

        if (adjacent) {
            Connection connection = findConnectionBetween(vertex1, vertex2);
            if (connection != null) {
                String direction;
                if (currentGraphType == GraphType.DIRECTED) {
                    if (connection.getSource().equals(vertex1)) {
                        direction = vertex1Id + " → " + vertex2Id;
                    } else {
                        direction = vertex2Id + " → " + vertex1Id;
                    }
                } else {
                    direction = vertex1Id + " ↔ " + vertex2Id;
                }

                message = String.format(
                        "✓ Os vértices %s e %s SÃO ADJACENTES!\n\n" +
                                "Detalhes da conexão:\n" +
                                "• Tipo: %s\n" +
                                "• ID: %s\n" +
                                "• Direção: %s\n" +
                                "• Peso: %.2f",
                        vertex1Id, vertex2Id,
                        connectionType,
                        connection.getId(),
                        direction,
                        connection.getWeight()
                );
            } else {
                message = String.format(
                        "✓ Os vértices %s e %s SÃO ADJACENTES!",
                        vertex1Id, vertex2Id
                );
            }
        } else {
            message = String.format(
                    "✗ Os vértices %s e %s NÃO SÃO ADJACENTES.\n\n" +
                            "Não há %s conectando estes vértices.",
                    vertex1Id, vertex2Id, connectionType
            );
        }

        JOptionPane.showMessageDialog(
                this,
                message,
                "Resultado da Verificação de Adjacência",
                adjacent ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE
        );

        System.out.println("Verificação de adjacência: " + vertex1Id + " e " + vertex2Id + " -> " +
                (adjacent ? "ADJACENTES" : "NÃO ADJACENTES"));
    }


    public boolean hasConnectionBetween(Vertex v1, Vertex v2) {
        return findConnectionBetween(v1, v2) != null;
    }

    public boolean hasDirectedConnectionBetween(Vertex source, Vertex target) {
        for (Connection connection : connectionList) {
            if (connection.getSource().equals(source) && connection.getTarget().equals(target)) {
                return true;
            }
        }
        return false;
    }

    public Connection findConnectionBetween(Vertex v1, Vertex v2) {
        for (Connection connection : connectionList) {
            if (connection.connects(v1, v2)) {
                return connection;
            }
        }
        return null;
    }

    public double getConnectionWeight(Connection connection) {
        return connection != null ? connection.getWeight() : 0;
    }

    public Vertex[] getConnectionEndpoints(Connection connection) {
        return connection != null ? connection.getEndpoints() : null;
    }

    public List<Edge> getEdges() {
        List<Edge> edges = new ArrayList<>();
        for (Connection connection : connectionList) {
            if (connection instanceof Edge) {
                edges.add((Edge) connection);
            }
        }
        return edges;
    }

    public List<Arc> getArcs() {
        List<Arc> arcs = new ArrayList<>();
        for (Connection connection : connectionList) {
            if (connection instanceof Arc) {
                arcs.add((Arc) connection);
            }
        }
        return arcs;
    }

    public void removeSelectedNode() {
        if (selectedNode != null) {
            removeVertex(selectedNode);
            selectedNode = null;
        }
    }

    private void handleMousePress(Point p) {
        Vertex clickedNode = getNodeAt(p);

        switch (currentMode) {
            case ADD_NODE:
                if (clickedNode == null) {
                    addVertex(p.x, p.y);
                }
                break;

            case REMOVE:
                if (clickedNode != null) {

                    selectNode(clickedNode);
                    removeSelectedNode();
                } else {
                    selectNode(null);
                }
                break;

            case CONNECT:
                if (clickedNode != null) {
                    if (selectedNode == null) {
                        selectNode(clickedNode);
                    } else {
                        if (clickedNode != selectedNode) {
                            if(hasConnectionBetween(selectedNode, clickedNode)) {
                                JOptionPane.showMessageDialog(
                                        this,
                                        "Já existe conexão de "+selectedNode.getId()+" para "+clickedNode.getId()+".",
                                        "Conexão sobreposta",
                                        JOptionPane.WARNING_MESSAGE
                                );
                                selectNode(null);
                            }else {
                                String connectionType = currentGraphType == GraphType.DIRECTED ? "arco" : "aresta";
                                String direction = currentGraphType == GraphType.DIRECTED ?
                                        selectedNode.getId() + " → " + clickedNode.getId() :
                                        selectedNode.getId() + " ↔ " + clickedNode.getId();

                                System.out.println("Criando " + connectionType + ": " + direction);

                                String weightStr = JOptionPane.showInputDialog(
                                        this,
                                        "Digite o peso da " + connectionType + ":",
                                        "Peso da " + (currentGraphType == GraphType.DIRECTED ? "Arco" : "Aresta"),
                                        JOptionPane.QUESTION_MESSAGE
                                );

                                double weight = 1.0;
                                if (weightStr != null && !weightStr.trim().isEmpty()) {
                                    try {
                                        weight = Double.parseDouble(weightStr.trim());
                                    } catch (NumberFormatException e) {
                                        JOptionPane.showMessageDialog(
                                                this,
                                                "Peso inválido. Usando peso padrão 1.0",
                                                "Aviso",
                                                JOptionPane.WARNING_MESSAGE
                                        );
                                    }
                                }

                                addConnection(null, selectedNode, clickedNode, weight);
                                selectNode(null);
                            }
                        }
                    }
                }else{
                    selectNode(null);
                }
                break;

            case DISCONNECT:
                if (clickedNode != null) {
                    if (selectedNode == null) {
                        selectNode(clickedNode);
                    } else {
                        if (clickedNode != selectedNode) {
                            boolean removed = removeConnectionBetween(selectedNode, clickedNode);
                            String connectionType = currentGraphType == GraphType.DIRECTED ? "arco" : "aresta";

                            if (removed) {
                                System.out.println(connectionType + " removido entre " + selectedNode.getId() + " e " + clickedNode.getId());
                            } else {
                                System.out.println("Nenhum " + connectionType + " encontrado entre " + selectedNode.getId() + " e " + clickedNode.getId());
                            }
                            selectNode(null);
                        }
                    }
                }
                break;
        }
        repaint();
    }

    public void setToolMode(ToolMode mode) {
        this.currentMode = mode;
        selectNode(null);
        System.out.println("Modo atual: " + mode);
    }

    private void selectNode(Vertex node) {
        if (selectedNode != null) {
            selectedNode.setSelected(false);
        }
        selectedNode = node;
        if (selectedNode != null) {
            selectedNode.setSelected(true);
        }
    }

    private Vertex getNodeAt(Point p) {
        for (int i = vertexList.size() - 1; i >= 0; i--) {
            Vertex vertex = vertexList.get(i);
            if (vertex.contains(p)) {
                return vertex;
            }
        }
        return null;
    }

    public List<Connection> applyPrimAlgorithm() {
        if (currentGraphType == GraphType.DIRECTED) {
            JOptionPane.showMessageDialog(
                    this,
                    "O algoritmo de Prim só pode ser aplicado em grafos não dirigidos!",
                    "Erro - Algoritmo de Prim",
                    JOptionPane.ERROR_MESSAGE
            );
            return null;
        }

        if (vertexList.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "O grafo não possui vértices!",
                    "Erro - Algoritmo de Prim",
                    JOptionPane.WARNING_MESSAGE
            );
            return null;
        }

        if (connectionList.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "O grafo não possui arestas!",
                    "Erro - Algoritmo de Prim",
                    JOptionPane.WARNING_MESSAGE
            );
            return null;
        }

        if (!Prim.isConnected(vertexList, connectionList)) {
            JOptionPane.showMessageDialog(
                    this,
                    "O grafo não é conexo! O algoritmo de Prim requer um grafo conexo.",
                    "Erro - Algoritmo de Prim",
                    JOptionPane.ERROR_MESSAGE
            );
            return null;
        }

        String[] vertexIds = vertexList.stream()
                .map(Vertex::getId)
                .toArray(String[]::new);

        String startVertexId = (String) JOptionPane.showInputDialog(
                this,
                "Selecione o vértice inicial para o algoritmo de Prim:",
                "Algoritmo de Prim - Vértice Inicial",
                JOptionPane.QUESTION_MESSAGE,
                null,
                vertexIds,
                vertexIds[0]
        );

        if (startVertexId == null) {
            return null;
        }

        Vertex startVertex = vertexList.stream()
                .filter(v -> v.getId().equals(startVertexId))
                .findFirst()
                .orElse(null);

        if (startVertex == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vértice inicial não encontrado!",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );
            return null;
        }

        Prim.PrimResult result = Prim.findMinimumSpanningTree(vertexList, connectionList, startVertex);

        if (result != null) {
            mstEdges = result.getMstEdges();
            mstTotalWeight = result.getTotalWeight();
            showMST = true;

            String edgesList = mstEdges.stream()
                    .map(c -> String.format("%s (%.2f)", c.getId(), c.getWeight()))
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");

            String message = String.format(
                    "✓ Árvore Geradora Mínima (AGM) encontrada!\n\n" +
                            "Vértice inicial: %s\n" +
                            "Número de arestas na AGM: %d\n" +
                            "Peso total da AGM: %.2f\n\n" +
                            "Arestas selecionadas:\n%s\n\n" +
                            "A AGM será destacada em verde no grafo.",
                    startVertexId,
                    result.getEdgeCount(),
                    result.getTotalWeight(),
                    edgesList
            );

            JOptionPane.showMessageDialog(
                    this,
                    message,
                    "Algoritmo de Prim - Resultado",
                    JOptionPane.INFORMATION_MESSAGE
            );

            repaint();

            System.out.printf("Prim executado com sucesso. AGM com %d arestas, peso total: %.2f%n",
                    result.getEdgeCount(), result.getTotalWeight());

            return result.getMstEdges();
        }

        return null;
    }

    public void clearMST() {
        mstEdges.clear();
        showMST = false;
        mstTotalWeight = 0.0;
        repaint();
    }

    public boolean isMSTVisible() {
        return showMST;
    }

    public List<Connection> getMSTEdges() {
        return new ArrayList<>(mstEdges);
    }


    public List<Connection> applyBreadthFirstSearch() {
        if (vertexList.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "O grafo não possui vértices!",
                    "Erro - Busca em Largura",
                    JOptionPane.WARNING_MESSAGE
            );
            return null;
        }

        String[] vertexIds = vertexList.stream()
                .map(Vertex::getId)
                .toArray(String[]::new);

        String startVertexId = (String) JOptionPane.showInputDialog(
                this,
                "Selecione o vértice inicial para a Busca em Largura:",
                "Busca em Largura (BFS) - Vértice Inicial",
                JOptionPane.QUESTION_MESSAGE,
                null,
                vertexIds,
                vertexIds[0]
        );

        if (startVertexId == null) {
            return null;
        }

        Vertex startVertex = vertexList.stream()
                .filter(v -> v.getId().equals(startVertexId))
                .findFirst()
                .orElse(null);

        if (startVertex == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vértice inicial não encontrado!",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );
            return null;
        }

        BreadthFirstSearch.BFSResult result = BreadthFirstSearch.performBFS(vertexList, connectionList, startVertex);

        if (result != null) {
            bfsTreeEdges = result.getTreeEdges();
            bfsVisitOrder = result.getVisitOrder();
            bfsStartVertex = result.getStartVertex();
            bfsDistances = result.getDistances();
            showBFS = true;

            String visitOrderText = bfsVisitOrder.stream()
                    .map(v -> v.getId() + "(d:" + bfsDistances.get(v) + ")")
                    .reduce((a, b) -> a + " → " + b)
                    .orElse("");

            String treeEdgesText = bfsTreeEdges.stream()
                    .map(Connection::getId)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("Nenhuma");

            String message = String.format(
                    "✓ Busca em Largura (BFS) executada!\n\n" +
                            "Vértice inicial: %s\n" +
                            "Vértices visitados: %d de %d\n" +
                            "Arestas na árvore BFS: %d\n\n" +
                            "Ordem de visita:\n%s\n\n" +
                            "Arestas da árvore BFS:\n%s\n\n" +
                            "A árvore BFS será destacada em azul no grafo.\n" +
                            "Os números nos vértices mostram a distância do vértice inicial.",
                    startVertexId,
                    result.getVisitedVertexCount(),
                    vertexList.size(),
                    result.getTreeEdgeCount(),
                    visitOrderText,
                    treeEdgesText
            );

            JOptionPane.showMessageDialog(
                    this,
                    message,
                    "Busca em Largura - Resultado",
                    JOptionPane.INFORMATION_MESSAGE
            );

            repaint();

            System.out.printf("BFS executada com sucesso. %d vértices visitados, %d arestas na árvore%n",
                    result.getVisitedVertexCount(), result.getTreeEdgeCount());

            return result.getTreeEdges();
        }

        return null;
    }

    public void clearBFS() {
        bfsTreeEdges.clear();
        bfsVisitOrder.clear();
        bfsDistances.clear();
        showBFS = false;
        bfsStartVertex = null;
        repaint();
    }

    public boolean isBFSVisible() {
        return showBFS;
    }

    public void clearComponents() {
        showComponents = false;
        componentMap.clear();
        numberOfComponents = 0;
        repaint();
    }

    public boolean isComponentsVisible() {
        return showComponents;
    }

    public void applyComponentAnalysis() {
        if (vertexList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "O grafo está vazio.", "Erro", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<List<Vertex>> components;
        String analysisTitle;
        String analysisDescription;

        if (currentGraphType == GraphType.UNDIRECTED) {
            analysisTitle = "Componentes Conexos (CC)";
            analysisDescription = String.format("Análise de Componentes Conexos (Grafo Não Direcionado) concluída.\n");
            components = BreadthFirstSearch.findConnectedComponents(vertexList, connectionList);
        } else {
            analysisTitle = "Componentes Fortemente Conexos (SCC)";
            analysisDescription = String.format("Análise de Componentes Fortemente Conexos (Grafo Direcionado) concluída.\n");
            components = KosarajuSCC.findSCCs(vertexList, connectionList);
        }

        componentMap.clear();
        numberOfComponents = components.size();
        int componentId = 0;
        for (List<Vertex> component : components) {
            for (Vertex vertex : component) {
                componentMap.put(vertex, componentId);
            }
            componentId++;
        }

        showComponents = true;

        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append(analysisDescription);
        messageBuilder.append(String.format("Número de componentes encontrados: %d\n\n", numberOfComponents));

        for (int i = 0; i < components.size(); i++) {
            messageBuilder.append(String.format("Componente %d: { ", i + 1));
            String componentVertices = components.get(i).stream()
                    .map(Vertex::getId)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
            messageBuilder.append(componentVertices).append(" }\n");
        }
        messageBuilder.append("\nOs componentes serão destacados por cores diferentes no grafo.");

        JOptionPane.showMessageDialog(
                this,
                messageBuilder.toString(),
                analysisTitle,
                JOptionPane.INFORMATION_MESSAGE
        );

        repaint();
    }
    public List<Connection> getBFSTreeEdges() {
        return new ArrayList<>(bfsTreeEdges);
    }


    public List<Connection> applyDepthFirstSearch() {
        if (vertexList.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "O grafo não possui vértices!",
                    "Erro - Busca em Profundidade",
                    JOptionPane.WARNING_MESSAGE
            );
            return null;
        }

        String[] vertexIds = vertexList.stream()
                .map(Vertex::getId)
                .toArray(String[]::new);

        String startVertexId = (String) JOptionPane.showInputDialog(
                this,
                "Selecione o vértice inicial para a Busca em Profundidade:",
                "Busca em Profundidade (DFS) - Vértice Inicial",
                JOptionPane.QUESTION_MESSAGE,
                null,
                vertexIds,
                vertexIds[0]
        );

        if (startVertexId == null) {
            return null;
        }

        Vertex startVertex = vertexList.stream()
                .filter(v -> v.getId().equals(startVertexId))
                .findFirst()
                .orElse(null);

        if (startVertex == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vértice inicial não encontrado!",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );
            return null;
        }

        DepthFirstSearch.DFSResult result = DepthFirstSearch.performDFS(vertexList, connectionList, startVertex);

        if (result != null) {
            dfsTreeEdges = result.getTreeEdges();
            dfsVisitOrder = result.getVisitOrder();
            dfsStartVertex = result.getStartVertex();
            dfsDiscoveryTimes = result.getDiscoveryTimes();
            dfsFinishTimes = result.getFinishTimes();
            showDFS = true;

            String visitOrderText = dfsVisitOrder.stream()
                    .map(v -> v.getId() + "(d:" + dfsDiscoveryTimes.get(v) + ",f:" + dfsFinishTimes.get(v) + ")")
                    .reduce((a, b) -> a + " → " + b)
                    .orElse("");

            String treeEdgesText = dfsTreeEdges.stream()
                    .map(Connection::getId)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("Nenhuma");

            String message = String.format(
                    "✓ Busca em Profundidade (DFS) executada!\n\n" +
                            "Vértice inicial: %s\n" +
                            "Vértices visitados: %d de %d\n" +
                            "Arestas na árvore DFS: %d\n\n" +
                            "Ordem de visita:\n%s\n\n" +
                            "Arestas da árvore DFS:\n%s\n\n" +
                            "A árvore DFS será destacada em roxo no grafo.\n" +
                            "Os números nos vértices mostram tempos de descoberta e finalização.",
                    startVertexId,
                    result.getVisitedVertexCount(),
                    vertexList.size(),
                    result.getTreeEdgeCount(),
                    visitOrderText,
                    treeEdgesText
            );

            JOptionPane.showMessageDialog(
                    this,
                    message,
                    "Busca em Profundidade - Resultado",
                    JOptionPane.INFORMATION_MESSAGE
            );

            repaint();

            System.out.printf("DFS executada com sucesso. %d vértices visitados, %d arestas na árvore%n",
                    result.getVisitedVertexCount(), result.getTreeEdgeCount());

            return result.getTreeEdges();
        }

        return null;
    }

    public void clearDFS() {
        dfsTreeEdges.clear();
        dfsVisitOrder.clear();
        dfsDiscoveryTimes.clear();
        dfsFinishTimes.clear();
        showDFS = false;
        dfsStartVertex = null;
        repaint();
    }

    public boolean isDFSVisible() {
        return showDFS;
    }

    public List<Connection> getDFSTreeEdges() {
        return new ArrayList<>(dfsTreeEdges);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (Connection connection : connectionList) {
            boolean isMSTEdge = showMST && mstEdges.contains(connection);
            boolean isBFSEdge = showBFS && bfsTreeEdges.contains(connection);
            boolean isDFSEdge = showDFS && dfsTreeEdges.contains(connection);
            boolean isAStarEdge = showAStarPath && aStarPathConnections.contains(connection);
            boolean isPCVEdge = showPCVPath && pcvPathConnections.contains(connection); // NOVO

            if (isPCVEdge) { // NOVO (Prioridade alta para desenhar por cima)
                drawPCVConnection(g2d, connection);
            }else if (isAStarEdge) {
                drawAStarConnection(g2d, connection);
            }else if (isMSTEdge) {
                drawMSTConnection(g2d, connection);
            } else if (isBFSEdge) {
                drawBFSConnection(g2d, connection);
            } else if (isDFSEdge) {
                drawDFSConnection(g2d, connection);
            } else {
                connection.draw(g);
            }
        }

        for (Vertex vertex : vertexList) {
            if (showComponents && componentMap.containsKey(vertex)) {
                drawComponentVertex(g2d, vertex);
            } else if (showBFS && bfsVisitOrder.contains(vertex)) {
                drawBFSVertex(g2d, vertex);
            } else if (showDFS && dfsVisitOrder.contains(vertex)) {
                drawDFSVertex(g2d, vertex);
            }else if (showColoring && vertexColors.containsKey(vertex)) {
                drawColoredVertex(g2d, vertex);
            }else {
                vertex.draw(g);
            }
        }



        if (showMST && !mstEdges.isEmpty()) {
            drawMSTInfo(g2d);
        }

        if (showColoring) {
            drawColoringInfo(g2d);
        }

        if (showBFS && !bfsTreeEdges.isEmpty()) {
            drawBFSInfo(g2d);
        }

        if (showDFS && !dfsTreeEdges.isEmpty()) {
            drawDFSInfo(g2d);
        }

        if (showComponents) {
            drawComponentsInfo(g2d);
        }

        if (showAStarPath) {
            drawAStarInfo(g2d);
        }

        if (showPCVPath) {
            drawPCVInfo(g2d);
        }

    }

    private void drawPCVConnection(Graphics2D g2d, Connection connection) {
        Stroke originalStroke = g2d.getStroke();
        Color originalColor = g2d.getColor();

        g2d.setColor(Color.MAGENTA); // Cor Magenta para o PCV
        g2d.setStroke(new BasicStroke(4.5f));

        g2d.drawLine(
                connection.getSource().getX(),
                connection.getSource().getY(),
                connection.getTarget().getX(),
                connection.getTarget().getY()
        );

        drawPCVWeightAndId(g2d, connection);

        g2d.setStroke(originalStroke);
        g2d.setColor(originalColor);
    }

    private void drawPCVWeightAndId(Graphics2D g2d, Connection connection) {
        int midX = (connection.getSource().getX() + connection.getTarget().getX()) / 2;
        int midY = (connection.getSource().getY() + connection.getTarget().getY()) / 2;

        g2d.setFont(new Font("Arial", Font.BOLD, 15));
        FontMetrics fm = g2d.getFontMetrics();

        String combinedText = connection.getId() + "(" + String.format("%.1f", connection.getWeight()) + ")";
        int textWidth = fm.stringWidth(combinedText);
        int textHeight = fm.getHeight();

        int paddingX = 6;
        int paddingY = 4;

        g2d.setColor(new Color(255, 200, 255)); // Fundo rosa claro
        g2d.fillRect(midX - textWidth / 2 - paddingX, midY - textHeight / 2 - paddingY,
                textWidth + (paddingX * 2), textHeight + (paddingY * 2));

        g2d.setColor(Color.MAGENTA); // Borda Magenta
        g2d.drawRect(midX - textWidth / 2 - paddingX, midY - textHeight / 2 - paddingY,
                textWidth + (paddingX * 2), textHeight + (paddingY * 2));

        g2d.setColor(new Color(100, 0, 100)); // Texto roxo escuro
        g2d.drawString(combinedText, midX - textWidth / 2, midY + textHeight / 4);
    }

    private void drawPCVInfo(Graphics2D g2d) {
        g2d.setColor(Color.MAGENTA);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        String info = String.format("Custo Total PCV: %.2f", pcvTotalWeight);
        g2d.drawString(info, 10, getHeight() - 130); // Posicionado logo acima do A*
    }

    private void drawAStarConnection(Graphics2D g2d, Connection connection) {
        Stroke originalStroke = g2d.getStroke();
        Color originalColor = g2d.getColor();

        g2d.setColor(new Color(255, 140, 0));
        g2d.setStroke(new BasicStroke(4.5f));

        g2d.drawLine(
                connection.getSource().getX(),
                connection.getSource().getY(),
                connection.getTarget().getX(),
                connection.getTarget().getY()
        );

        drawAStarWeightAndId(g2d, connection);

        g2d.setStroke(originalStroke);
        g2d.setColor(originalColor);
    }

    private void drawAStarWeightAndId(Graphics2D g2d, Connection connection) {
        int midX = (connection.getSource().getX() + connection.getTarget().getX()) / 2;
        int midY = (connection.getSource().getY() + connection.getTarget().getY()) / 2;

        g2d.setFont(new Font("Arial", Font.BOLD, 15));
        FontMetrics fm = g2d.getFontMetrics();

        String combinedText = connection.getId() + "(" + String.format("%.1f", connection.getWeight()) + ")";
        int textWidth = fm.stringWidth(combinedText);
        int textHeight = fm.getHeight();

        int paddingX = 6;
        int paddingY = 4;

        g2d.setColor(new Color(255, 230, 200));
        g2d.fillRect(midX - textWidth / 2 - paddingX, midY - textHeight / 2 - paddingY,
                textWidth + (paddingX * 2), textHeight + (paddingY * 2));

        g2d.setColor(new Color(255, 140, 0));
        g2d.drawRect(midX - textWidth / 2 - paddingX, midY - textHeight / 2 - paddingY,
                textWidth + (paddingX * 2), textHeight + (paddingY * 2));

        g2d.setColor(new Color(180, 80, 0));
        g2d.drawString(combinedText, midX - textWidth / 2, midY + textHeight / 4);
    }

    private void drawAStarInfo(Graphics2D g2d) {
        g2d.setColor(new Color(200, 100, 0));
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        String info = String.format("Custo do caminho A*: %.2f", aStarPathTotalWeight);
        g2d.drawString(info, 10, getHeight() - 110);
    }

    private void drawColoredVertex(Graphics2D g2d, Vertex vertex) {
        Color fillColor = vertexColors.get(vertex);
        if (fillColor == null) {
            vertex.draw(g2d);
            return;
        }

        int x = vertex.getX();
        int y = vertex.getY();
        int radius = 25;
        g2d.setColor(fillColor);
        g2d.fillOval(x - radius, y - radius, radius * 2, radius * 2);

        if (vertex.isSelected()) {
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(3));
        } else {
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
        }
        g2d.drawOval(x - radius, y - radius, radius * 2, radius * 2);

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(vertex.getId());
        g2d.drawString(vertex.getId(), x - textWidth / 2, y + fm.getAscent() / 2);

        g2d.setStroke(new BasicStroke(1));
    }


    private void drawColoringInfo(Graphics2D g2d) {
        g2d.setColor(new Color(60, 60, 60));
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        String info = String.format("Coloração: %d cores usadas", chromaticNumber);
        g2d.drawString(info, 10, getHeight() - 90);
    }

    private static final Color[] COMPONENT_COLORS = {
            new Color(255, 100, 100), new Color(100, 200, 100), new Color(100, 100, 255),
            new Color(255, 180, 0), new Color(0, 200, 200), new Color(200, 0, 200),
            new Color(180, 180, 0), new Color(120, 120, 120)
    };


    private void drawComponentVertex(Graphics2D g2d, Vertex vertex) {
        int componentId = componentMap.getOrDefault(vertex, 0);
        Color color = COMPONENT_COLORS[componentId % COMPONENT_COLORS.length];
        vertex.draw(g2d);
        Stroke originalStroke = g2d.getStroke();
        g2d.setColor(color.darker());
        g2d.setStroke(new BasicStroke(3));
        g2d.drawOval(vertex.getX() - vertex.getRadiusw() - 3, vertex.getY() - vertex.getRadiusw() - 3,
                (vertex.getRadiusw() + 3) * 2, (vertex.getRadiusw() + 3) * 2);
        g2d.setStroke(originalStroke);
    }

    private void drawComponentsInfo(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        String info = String.format("Componentes encontrados: %d", numberOfComponents);
        g2d.drawString(info, 10, getHeight() - 70);
    }

    public void clearAllAlgorithmVisualizations() {
        clearBFS();
        clearComponents();
        clearMST();
        clearDFS();
        clearColoring();
        clearAStarPath();
        clearPCV();
    }

    private void drawMSTConnection(Graphics2D g2d, Connection connection) {
        Color originalColor = g2d.getColor();
        Stroke originalStroke = g2d.getStroke();

        g2d.setColor(new Color(0, 150, 0));
        g2d.setStroke(new BasicStroke(4));

        g2d.drawLine(
                connection.getSource().getX(),
                connection.getSource().getY(),
                connection.getTarget().getX(),
                connection.getTarget().getY()
        );

        drawMSTWeightAndId(g2d, connection);

        g2d.setColor(originalColor);
        g2d.setStroke(originalStroke);
    }

    private void drawMSTWeightAndId(Graphics2D g2d, Connection connection) {
        int midX = (connection.getSource().getX() + connection.getTarget().getX()) / 2;
        int midY = (connection.getSource().getY() + connection.getTarget().getY()) / 2;

        g2d.setFont(new Font("Arial", Font.BOLD, 15));
        FontMetrics fm = g2d.getFontMetrics();

        String combinedText = connection.getId() + "(" + connection.getWeight() + ")";
        int textWidth = fm.stringWidth(combinedText);
        int textHeight = fm.getHeight();

        int paddingX = 6;
        int paddingY = 4;

        g2d.setColor(new Color(200, 255, 200));
        g2d.fillRect(midX - textWidth/2 - paddingX, midY - textHeight/2 - paddingY,
                textWidth + (paddingX * 2), textHeight + (paddingY * 2));

        g2d.setColor(new Color(0, 150, 0));
        g2d.drawRect(midX - textWidth/2 - paddingX, midY - textHeight/2 - paddingY,
                textWidth + (paddingX * 2), textHeight + (paddingY * 2));

        g2d.setColor(new Color(0, 100, 0));
        g2d.drawString(combinedText, midX - textWidth/2, midY + textHeight/4);
    }

    private void drawMSTInfo(Graphics2D g2d) {
        g2d.setColor(new Color(0, 100, 0));
        g2d.setFont(new Font("Arial", Font.BOLD, 14));

        String mstInfo = String.format("AGM: %d arestas, Peso total: %.2f",
                mstEdges.size(), mstTotalWeight);
        g2d.drawString(mstInfo, 10, getHeight() - 10);
    }

    private void drawBFSConnection(Graphics2D g2d, Connection connection) {
        Color originalColor = g2d.getColor();
        Stroke originalStroke = g2d.getStroke();

        g2d.setColor(new Color(0, 100, 200));
        g2d.setStroke(new BasicStroke(4));

        g2d.drawLine(
                connection.getSource().getX(),
                connection.getSource().getY(),
                connection.getTarget().getX(),
                connection.getTarget().getY()
        );

        drawBFSWeightAndId(g2d, connection);

        g2d.setColor(originalColor);
        g2d.setStroke(originalStroke);
    }

    private void drawBFSWeightAndId(Graphics2D g2d, Connection connection) {
        int midX = (connection.getSource().getX() + connection.getTarget().getX()) / 2;
        int midY = (connection.getSource().getY() + connection.getTarget().getY()) / 2;

        g2d.setFont(new Font("Arial", Font.BOLD, 15));
        FontMetrics fm = g2d.getFontMetrics();

        String combinedText = connection.getId() + "(" + connection.getWeight() + ")";
        int textWidth = fm.stringWidth(combinedText);
        int textHeight = fm.getHeight();

        int paddingX = 6;
        int paddingY = 4;

        g2d.setColor(new Color(200, 220, 255));
        g2d.fillRect(midX - textWidth/2 - paddingX, midY - textHeight/2 - paddingY,
                textWidth + (paddingX * 2), textHeight + (paddingY * 2));

        g2d.setColor(new Color(0, 100, 200));
        g2d.drawRect(midX - textWidth/2 - paddingX, midY - textHeight/2 - paddingY,
                textWidth + (paddingX * 2), textHeight + (paddingY * 2));

        g2d.setColor(new Color(0, 80, 160));
        g2d.drawString(combinedText, midX - textWidth/2, midY + textHeight/4);
    }

    private void drawBFSVertex(Graphics2D g2d, Vertex vertex) {
        vertex.draw(g2d);

        if (bfsDistances.containsKey(vertex)) {
            int distance = bfsDistances.get(vertex);

            int x = vertex.getX();
            int y = vertex.getY() - 35;

            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            FontMetrics fm = g2d.getFontMetrics();
            String distText = "d:" + distance;
            int textWidth = fm.stringWidth(distText);

            g2d.setColor(new Color(200, 220, 255, 200));
            g2d.fillOval(x - textWidth/2 - 3, y - 8, textWidth + 6, 16);

            g2d.setColor(new Color(0, 100, 200));
            g2d.drawOval(x - textWidth/2 - 3, y - 8, textWidth + 6, 16);

            g2d.setColor(new Color(0, 80, 160));
            g2d.drawString(distText, x - textWidth/2, y + 4);

            if (vertex.equals(bfsStartVertex)) {
                g2d.setStroke(new BasicStroke(3));
                g2d.setColor(new Color(0, 150, 255));
                g2d.drawOval(vertex.getX() - 27, vertex.getY() - 27, 54, 54);
            }
        }
    }

    private void drawBFSInfo(Graphics2D g2d) {
        g2d.setColor(new Color(0, 80, 160));
        g2d.setFont(new Font("Arial", Font.BOLD, 14));

        String bfsInfo = String.format("BFS: %d vértices visitados, %d arestas na árvore",
                bfsVisitOrder.size(), bfsTreeEdges.size());
        g2d.drawString(bfsInfo, 10, getHeight() - 30);
    }

    private void drawDFSConnection(Graphics2D g2d, Connection connection) {
        Color originalColor = g2d.getColor();
        Stroke originalStroke = g2d.getStroke();

        g2d.setColor(new Color(128, 0, 128));
        g2d.setStroke(new BasicStroke(4));

        g2d.drawLine(
                connection.getSource().getX(),
                connection.getSource().getY(),
                connection.getTarget().getX(),
                connection.getTarget().getY()
        );

        drawDFSWeightAndId(g2d, connection);

        g2d.setColor(originalColor);
        g2d.setStroke(originalStroke);
    }

    private void drawDFSWeightAndId(Graphics2D g2d, Connection connection) {
        int midX = (connection.getSource().getX() + connection.getTarget().getX()) / 2;
        int midY = (connection.getSource().getY() + connection.getTarget().getY()) / 2;

        g2d.setFont(new Font("Arial", Font.BOLD, 15));
        FontMetrics fm = g2d.getFontMetrics();

        String combinedText = connection.getId() + "(" + connection.getWeight() + ")";
        int textWidth = fm.stringWidth(combinedText);
        int textHeight = fm.getHeight();

        int paddingX = 6;
        int paddingY = 4;

        g2d.setColor(new Color(230, 200, 255));
        g2d.fillRect(midX - textWidth/2 - paddingX, midY - textHeight/2 - paddingY,
                textWidth + (paddingX * 2), textHeight + (paddingY * 2));

        g2d.setColor(new Color(128, 0, 128));
        g2d.drawRect(midX - textWidth/2 - paddingX, midY - textHeight/2 - paddingY,
                textWidth + (paddingX * 2), textHeight + (paddingY * 2));

        g2d.setColor(new Color(100, 0, 100));
        g2d.drawString(combinedText, midX - textWidth/2, midY + textHeight/4);
    }

    private void drawDFSVertex(Graphics2D g2d, Vertex vertex) {
        vertex.draw(g2d);

        if (dfsDiscoveryTimes.containsKey(vertex)) {
            int discoveryTime = dfsDiscoveryTimes.get(vertex);
            int finishTime = dfsFinishTimes.getOrDefault(vertex, -1);

            int x = vertex.getX();
            int y = vertex.getY() - 35;

            g2d.setFont(new Font("Arial", Font.BOLD, 11));
            FontMetrics fm = g2d.getFontMetrics();
            String timeText = "d:" + discoveryTime + "/f:" + finishTime;
            int textWidth = fm.stringWidth(timeText);

            g2d.setColor(new Color(230, 200, 255, 200));
            g2d.fillOval(x - textWidth/2 - 3, y - 8, textWidth + 6, 16);

            g2d.setColor(new Color(128, 0, 128));
            g2d.drawOval(x - textWidth/2 - 3, y - 8, textWidth + 6, 16);

            g2d.setColor(new Color(100, 0, 100));
            g2d.drawString(timeText, x - textWidth/2, y + 4);

            if (vertex.equals(dfsStartVertex)) {
                g2d.setStroke(new BasicStroke(3));
                g2d.setColor(new Color(180, 100, 200));
                g2d.drawOval(vertex.getX() - 27, vertex.getY() - 27, 54, 54);
            }
        }
    }

    private void drawDFSInfo(Graphics2D g2d) {
        g2d.setColor(new Color(100, 0, 100));
        g2d.setFont(new Font("Arial", Font.BOLD, 14));

        String dfsInfo = String.format("DFS: %d vértices visitados, %d arestas na árvore",
                dfsVisitOrder.size(), dfsTreeEdges.size());
        g2d.drawString(dfsInfo, 10, getHeight() - 50);
    }

    public int[][] getAdjacencyMatrix() {
        int n = vertexList.size();
        int[][] matrix = new int[n][n];

        java.util.Map<Vertex, Integer> vertexIndex = new java.util.HashMap<>();
        for (int i = 0; i < n; i++) {
            vertexIndex.put(vertexList.get(i), i);
        }

        for (Connection connection : connectionList) {
            int sourceIndex = vertexIndex.get(connection.getSource());
            int targetIndex = vertexIndex.get(connection.getTarget());

            matrix[sourceIndex][targetIndex] = 1;

            if (currentGraphType == GraphType.UNDIRECTED) {
                matrix[targetIndex][sourceIndex] = 1;
            }
        }

        return matrix;
    }

    public int[][] getIncidenceMatrix() {
        int n = vertexList.size();
        int m = connectionList.size();
        int[][] matrix = new int[n][m];

        java.util.Map<Vertex, Integer> vertexIndex = new java.util.HashMap<>();
        for (int i = 0; i < n; i++) {
            vertexIndex.put(vertexList.get(i), i);
        }

        for (int j = 0; j < m; j++) {
            Connection connection = connectionList.get(j);
            int sourceIndex = vertexIndex.get(connection.getSource());
            int targetIndex = vertexIndex.get(connection.getTarget());

            if (currentGraphType == GraphType.DIRECTED) {
                matrix[sourceIndex][j] = 1;
                matrix[targetIndex][j] = -1;
            } else {
                matrix[sourceIndex][j] = 1;
                matrix[targetIndex][j] = 1;
            }
        }

        return matrix;
    }


    public List<Vertex> getVertexList() { return vertexList; }
    public List<Connection> getConnectionList() { return new ArrayList<>(connectionList); }
    public GraphType getGraphType() { return currentGraphType; }

    public void setGraphType(GraphType graphType) {
        currentGraphType = graphType;
        ui.getToolbar().setOrientationButton(currentGraphType);
        convertExistingConnections();
        clearBFS();
        clearComponents();
        clearMST();
        clearDFS();
        repaint();
    }

    public String getGraphTypeDescription(){
        if(currentGraphType == GraphType.UNDIRECTED){
            return "não-dirigido";
        }
        return "dirigido";
    }

    private void convertExistingConnections() {
        List<Connection> oldConnections = new ArrayList<>(connectionList);
        connectionList.clear();

        for (Vertex vertex : vertexList) {
            vertex.getNeighbours().clear();
        }

        for (Connection oldConnection : oldConnections) {
            addConnection(
                    oldConnection.getId(),
                    oldConnection.getSource(),
                    oldConnection.getTarget(),
                    oldConnection.getWeight()
            );
        }
    }

    public void clearGraph() {

        for (Vertex vertex : vertexList) {
            vertex.getNeighbours().clear();
        }

        this.vertexList.clear();
        this.connectionList.clear();

        this.selectedNode = null;
        this.nextVertexIdCounter = 1;
        this.nextConnectionIdCounter = 1;

        clearAllAlgorithmVisualizations();

        repaint();

        System.out.println("O grafo foi completamente limpo e resetado.");
    }

    public void applyWelshPowellColoring() {
        if (vertexList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "O grafo não possui vértices para colorir.", "Erro", JOptionPane.WARNING_MESSAGE);
            return;
        }

        clearColoring();

        List<Vertex> sortedVertices = new ArrayList<>(vertexList);
        sortedVertices.sort((v1, v2) -> Integer.compare(v2.getNeighbours().size(), v1.getNeighbours().size()));

        int colorIndex = 0;
        List<Color> colorPalette = new ArrayList<>();

        while (vertexColors.size() < vertexList.size()) {
            Color currentColor = generateDistinctColor(colorPalette.size());
            colorPalette.add(currentColor);

            for (Vertex currentVertex : sortedVertices) {
                if (vertexColors.containsKey(currentVertex)) {
                    continue;
                }

                boolean conflict = false;
                for (Vertex neighbor : currentVertex.getNeighbours()) {
                    if (currentColor.equals(vertexColors.get(neighbor))) {
                        conflict = true;
                        break;
                    }
                }

                if (!conflict) {
                    vertexColors.put(currentVertex, currentColor);
                }
            }
            colorIndex++;
        }

        this.chromaticNumber = colorIndex;
        this.showColoring = true;

        String message = String.format(
                "✓ Coloração de Grafo (Welsh-Powell) concluída!\n\n" +
                        "Número de cores utilizadas (Número Cromático Heurístico): %d\n\n" +
                        "Os vértices foram coloridos e serão exibidos no grafo.",
                this.chromaticNumber
        );
        JOptionPane.showMessageDialog(this, message, "Coloração de Grafo - Resultado", JOptionPane.INFORMATION_MESSAGE);

        repaint();
        System.out.printf("Welsh-Powell executado. Número cromático encontrado: %d%n", this.chromaticNumber);
    }


    private Color generateDistinctColor(int index) {
        float hue = (index * 0.61803398875f) % 1.0f;
        return Color.getHSBColor(hue, 0.9f, 0.95f);
    }

    public void clearColoring() {
        showColoring = false;
        vertexColors.clear();
        chromaticNumber = 0;
        repaint();
    }

    public boolean hasThreeCycle() {
        if (vertexList.size() < 3) {
            return false;
        }

        if (currentGraphType == GraphType.UNDIRECTED) {
            for (Vertex u : vertexList) {
                List<Vertex> neighbors = u.getNeighbours();
                if (neighbors.size() < 2) {
                    continue;
                }
                for (int i = 0; i < neighbors.size(); i++) {
                    for (int j = i + 1; j < neighbors.size(); j++) {
                        Vertex v = neighbors.get(i);
                        Vertex w = neighbors.get(j);
                        if (areAdjacent(v, w)) {
                            System.out.println("Ciclo de 3 (não-dirigido) encontrado: " + u.getId() + " - " + v.getId() + " - " + w.getId());
                            return true;
                        }
                    }
                }
            }
        } else {
            for (Vertex u : vertexList) {
                for (Vertex v : u.getNeighbours()) { // u -> v
                    for (Vertex w : v.getNeighbours()) { // v -> w
                        if (areAdjacent(w, u)) {
                            if (!w.equals(u) && !w.equals(v)) {
                                System.out.println("Ciclo de 3 (dirigido) encontrado: " + u.getId() + " -> " + v.getId() + " -> " + w.getId() + " -> " + u.getId());
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public void applyPCVAlgorithm() {
        if (vertexList.size() < 3) {
            JOptionPane.showMessageDialog(this, "O grafo precisa ter pelo menos 3 vértices para o PCV.", "Erro - PCV", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // --- Configuração Simples ---
        // Você pode manter aquele JSpinner que fiz antes aqui se quiser,
        // mas vou simplificar os valores hardcoded pra focar na lógica.
        int popSize = 50;
        int maxGenerations = 20;
        double mutationRate = 0.01; // 1%
        int elitismCount = 2;

        // 1. População Inicial
        List<Individual> population = new ArrayList<>();
        List<Vertex> baseGenes = new ArrayList<>(vertexList);

        for (int i = 0; i < popSize; i++) {
            Collections.shuffle(baseGenes);
            // Passamos a lista de conexões do GraphPanel para o Indivíduo calcular seu custo
            population.add(new Individual(baseGenes, connectionList));
        }

        int currentGeneration = 0;
        boolean userWantsToContinue = true;

        while (userWantsToContinue && currentGeneration < maxGenerations) {
            currentGeneration++;
            Collections.sort(population); // Ordena pelo fitness (definido na classe Individual)

            // --- Feedback ao Usuário ---
            StringBuilder stats = new StringBuilder();
            stats.append("Geração: ").append(currentGeneration).append("\n");
            stats.append("Melhor Custo: ").append(String.format("%.2f", population.get(0).getFitness())).append("\n\n");
            stats.append("Top 5 Indivíduos:\n");
            for(int k=0; k < Math.min(5, population.size()); k++){
                stats.append(k+1).append(". ").append(population.get(k).toString()).append("\n");
            }
            stats.append("\nContinuar?");

            int choice = JOptionPane.showConfirmDialog(this, stats.toString(), "AG - Geração " + currentGeneration, JOptionPane.YES_NO_OPTION);
            if (choice != JOptionPane.YES_OPTION) break;

            // --- Nova Geração ---
            List<Individual> newPopulation = new ArrayList<>();

            // 1. Elitismo
            for (int i = 0; i < elitismCount; i++) newPopulation.add(population.get(i));

            // 2. Cruzamento e Mutação
            Random rand = new Random();
            while (newPopulation.size() < popSize) {
                Individual p1 = tournamentSelection(population);
                Individual p2 = tournamentSelection(population);

                List<Vertex> childGenes = pmxCrossover(p1.getGenes(), p2.getGenes());

                if (rand.nextDouble() < mutationRate) {
                    swapMutation(childGenes);
                }

                newPopulation.add(new Individual(childGenes, connectionList));
            }
            population = newPopulation;

            // Lógica de estender gerações (obrigatório)
            if (currentGeneration == maxGenerations) {
                int extend = JOptionPane.showConfirmDialog(this, "Adicionar +20 gerações?", "Fim", JOptionPane.YES_NO_OPTION);
                if (extend == JOptionPane.YES_OPTION) maxGenerations += 20;
            }
        }

        // Finalização
        Collections.sort(population);
        Individual best = population.get(0);
        reconstructPCVPath(best); // Visualiza a melhor rota
    }

    // Seleção por Torneio
    private Individual tournamentSelection(List<Individual> pop) {
        Random rand = new Random();
        Individual best = null;
        for (int i = 0; i < 3; i++) {
            Individual ind = pop.get(rand.nextInt(pop.size()));
            if (best == null || ind.getFitness() < best.getFitness()) {
                best = ind;
            }
        }
        return best;
    }

    // Mutação Swap (Troca simples)
    private void swapMutation(List<Vertex> genes) {
        Random rand = new Random();
        int i = rand.nextInt(genes.size());
        int j = rand.nextInt(genes.size());
        Collections.swap(genes, i, j);
    }

    // Cruzamento PMX (Lógica principal de corte e mapeamento)
    private List<Vertex> pmxCrossover(List<Vertex> p1, List<Vertex> p2) {
        int n = p1.size();
        Vertex[] childArr = new Vertex[n];
        Random rand = new Random();

        int cut1 = rand.nextInt(n);
        int cut2 = rand.nextInt(n);
        if (cut1 > cut2) { int temp = cut1; cut1 = cut2; cut2 = temp; }

        // Copia o miolo do pai 1
        for (int i = cut1; i <= cut2; i++) childArr[i] = p1.get(i);

        // Resolve conflitos do pai 2
        for (int i = cut1; i <= cut2; i++) {
            Vertex geneInP2 = p2.get(i);
            if (!containsVertex(childArr, geneInP2)) {
                int pos = i;
                Vertex valInP1 = p1.get(pos);
                while (true) {
                    int posInP2 = p2.indexOf(valInP1);
                    if (posInP2 >= cut1 && posInP2 <= cut2) {
                        valInP1 = p1.get(posInP2);
                    } else {
                        childArr[posInP2] = geneInP2;
                        break;
                    }
                }
            }
        }
        // Preenche buracos restantes
        for (int i = 0; i < n; i++) {
            if (childArr[i] == null) childArr[i] = p2.get(i);
        }
        return Arrays.asList(childArr);
    }

    private boolean containsVertex(Vertex[] arr, Vertex v) {
        for (Vertex x : arr) if (x != null && x.equals(v)) return true;
        return false;
    }

    // Prepara a visualização final
    private void reconstructPCVPath(Individual best) {
        pcvPathConnections.clear();

        List<Vertex> r = best.getGenes();

        for (int i = 0; i < r.size(); i++) {
            Vertex from = r.get(i);
            Vertex to = r.get((i + 1) % r.size());
            Connection c = findConnectionBetween(from, to);
            if (c != null) pcvPathConnections.add(c);
        }

        clearAllAlgorithmVisualizations();
        showPCVPath = true;
        repaint();
        pcvTotalWeight = best.getFitness();
        JOptionPane.showMessageDialog(this, "Melhor rota encontrada com custo: " + pcvTotalWeight);
    }

    public void applyAStarAlgorithm() {
        if (vertexList.size() < 2) {
            JOptionPane.showMessageDialog(this, "O grafo precisa de pelo menos dois vértices para o A*.", "Erro - A*", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean allHaveCoords = vertexList.stream().allMatch(Vertex::hasGeographicCoordinates);

        if (!allHaveCoords) {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    """
                            Alguns vértices não possuem coordenadas geográficas.
                            O algoritmo A* usará as coordenadas da tela como aproximação.
                            Deseja continuar?""",
                    "Aviso - Coordenadas Geográficas",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }

        // --- 1. Obter Vértice Inicial e Final do Usuário ---
        String[] vertexIds = vertexList.stream().map(Vertex::getId).toArray(String[]::new);

        String startVertexId = (String) JOptionPane.showInputDialog(
                this, "Selecione o vértice INICIAL:", "Algoritmo A* - Ponto de Partida",
                JOptionPane.QUESTION_MESSAGE, null, vertexIds, vertexIds[0]);
        if (startVertexId == null) return;

        String endVertexId = (String) JOptionPane.showInputDialog(
                this, "Selecione o vértice FINAL:", "Algoritmo A* - Ponto de Chegada",
                JOptionPane.QUESTION_MESSAGE, null, vertexIds, vertexIds.length > 1 ? vertexIds[1] : vertexIds[0]);
        if (endVertexId == null) return;

        if (startVertexId.equals(endVertexId)) {
            JOptionPane.showMessageDialog(this, "O vértice inicial e final devem ser diferentes.", "Erro - A*", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Vertex startNode = vertexList.stream().filter(v -> v.getId().equals(startVertexId)).findFirst().orElse(null);
        Vertex endNode = vertexList.stream().filter(v -> v.getId().equals(endVertexId)).findFirst().orElse(null);

        // Display h(n) table for the destination
        displayHeuristicTable(endNode);

        // --- 2. Inicialização do A* ---
        // openSet contém os nós a serem avaliados, priorizados pelo menor fScore
        Map<Vertex, Double> fScore = new HashMap<>();
        Map<Vertex, Double> gScore = new HashMap<>();
        Map<Vertex, Double> hScore = new HashMap<>(); // Store h(n) values

        PriorityQueue<Vertex> openSet = new PriorityQueue<>(Comparator.comparingDouble(fScore::get));

        // cameFrom armazena o nó anterior no caminho mais curto
        Map<Vertex, Vertex> cameFrom = new HashMap<>();

        // gScore é o custo do início até o nó atual
        for (Vertex v : vertexList) {
            gScore.put(v, Double.POSITIVE_INFINITY);
            fScore.put(v, Double.POSITIVE_INFINITY);
            hScore.put(v, heuristic(v, endNode));
        }
        gScore.put(startNode, 0.0);
        fScore.put(startNode, heuristic(startNode, endNode));
        openSet.add(startNode);

        // --- 3. Loop Principal do Algoritmo ---
        while (!openSet.isEmpty()) {
            Vertex current = openSet.poll(); // Pega o nó com o menor fScore

            if (current.equals(endNode)) {
                // Caminho encontrado!
                reconstructAStarPath(cameFrom, current, gScore, hScore);
                showAStarResultDialog(startNode, endNode, gScore, hScore);
                return;
            }

            for (Vertex neighbor : current.getNeighbours()) {
                Connection connection = findConnectionBetween(current, neighbor);
                if (connection == null) continue;
                double weight = connection.getWeight();

                double tentativeGScore = gScore.get(current) + weight;

                if (tentativeGScore < gScore.get(neighbor)) {
                    // Este é um caminho melhor para o vizinho. Registre-o.
                    cameFrom.put(neighbor, current);
                    gScore.put(neighbor, tentativeGScore);
                    fScore.put(neighbor, gScore.get(neighbor) + heuristic(neighbor, endNode));
                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }

        // Se o loop terminar, não há caminho
        JOptionPane.showMessageDialog(this, "Não foi possível encontrar um caminho de " + startVertexId + " para " + endVertexId + ".", "Caminho Não Encontrado", JOptionPane.WARNING_MESSAGE);
    }


    // Heurística: Distância de Manhattan usando coordenadas geográficas
    private double heuristic(Vertex a, Vertex b) {
        if (a.hasGeographicCoordinates() && b.hasGeographicCoordinates()) {
            // Use Manhattan distance with geographic coordinates
            // Convert to kilometers (approximation: 1 degree ≈ 111 km)
            double latDiff = Math.abs(a.getLatitude() - b.getLatitude()) * 111.0;
            double lonDiff = Math.abs(a.getLongitude() - b.getLongitude()) * 111.0;
            return latDiff + lonDiff;
        } else {
            // Fallback: use screen coordinates
            return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
        }
    }


    // Display h(n) table for all vertices relative to destination
    private void displayHeuristicTable(Vertex destination) {
        StringBuilder table = new StringBuilder();
        table.append("Tabela h(n) - Destino: ").append(destination.getId()).append("\n\n");
        table.append(String.format("%-20s | %s\n", "Cidade", "Distância (km)"));
        table.append("-------------------------------------------\n");

        // Sort vertices by heuristic value
        List<Vertex> sortedVertices = new ArrayList<>(vertexList);
        sortedVertices.sort(Comparator.comparingDouble(v -> heuristic(v, destination)));

        for (Vertex v : sortedVertices) {
            double h = heuristic(v, destination);
            table.append(String.format("%-20s | %.0f\n", v.getId(), h));
        }

        JTextArea textArea = new JTextArea(table.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        JOptionPane.showMessageDialog(
                this,
                scrollPane,
                "Tabela Heurística h(n)",
                JOptionPane.INFORMATION_MESSAGE
        );
    }


    // Reconstrói o caminho e preenche as variáveis de visualização
    private void reconstructAStarPath(Map<Vertex, Vertex> cameFrom, Vertex current,
                                      Map<Vertex, Double> gScore, Map<Vertex, Double> hScore) {
        aStarPathConnections.clear();
        aStarPathTotalWeight = 0.0;

        while (cameFrom.containsKey(current)) {
            Vertex previous = cameFrom.get(current);
            Connection connection = findConnectionBetween(previous, current);
            if (connection != null) {
                aStarPathConnections.add(0, connection);
                aStarPathTotalWeight += connection.getWeight();
            }
            current = previous;
        }

        showAStarPath = true;
        repaint();
    }


    // Exibe o diálogo com o resultado
    private void showAStarResultDialog(Vertex start, Vertex end,
                                       Map<Vertex, Double> gScore, Map<Vertex, Double> hScore) {
        StringBuilder pathString = new StringBuilder();
        pathString.append("Caminho mais curto encontrado:\n\n");

        pathString.append(start.getId());
        Vertex current = start;

        for (Connection conn : aStarPathConnections) {
            Vertex next = conn.getSource().equals(current) ? conn.getTarget() : conn.getSource();
            pathString.append(" → ").append(next.getId());
            pathString.append(String.format(" (%.2f)", conn.getWeight()));
            current = next;
        }

        pathString.append("\n\n");
        pathString.append(String.format("Custo Total g(n): %.2f\n", aStarPathTotalWeight));
        pathString.append(String.format("Heurística h(n) do destino: %.2f\n", hScore.get(end)));
        pathString.append(String.format("Número de conexões: %d\n", aStarPathConnections.size()));

        if (end.hasGeographicCoordinates()) {
            pathString.append("\nCoordenadas do destino:\n");
            pathString.append(String.format("  Latitude: %.4f\n", end.getLatitude()));
            pathString.append(String.format("  Longitude: %.4f\n", end.getLongitude()));
        }

        JOptionPane.showMessageDialog(
                this,
                pathString.toString(),
                "Resultado do Algoritmo A*",
                JOptionPane.INFORMATION_MESSAGE
        );
    }


    // Método de limpeza
    public void clearAStarPath() {
        showAStarPath = false;
        aStarPathConnections.clear();
        aStarPathTotalWeight = 0.0;
        repaint();
    }

    public void clearPCV() {
        this.showPCVPath = false;           // Para de desenhar
        this.pcvPathConnections.clear();    // Limpa a lista de arestas da rota
        this.pcvTotalWeight = 0.0;          // Zera o peso total
        repaint();                          // Atualiza a tela pra sumir o desenho
    }

}