package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.algorithms.*;

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

    private boolean showComponents = false; // Substitua showConexo por showComponents
    private Map<Vertex, Integer> componentMap = new HashMap<>();
    private int numberOfComponents = 0;

    public GraphPanel() {
        MouseAdapter mouseListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMousePress(e.getPoint());
            }
        };
        addMouseListener(mouseListener);

        // addVertex(100, 100);
    }

    public void addVertex(int x, int y) {
        String id = String.valueOf(nextVertexIdCounter++);
        Vertex newVertex = new Vertex(id, x, y); // Usando o construtor unificado de Vertex
        this.vertexList.add(newVertex);
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
            return; // Usuário cancelou
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
            return; // Usuário cancelou
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
            // Busca informações sobre a conexão
            Connection connection = findConnectionBetween(vertex1, vertex2);
            if (connection != null) {
                String direction;
                if (currentGraphType == GraphType.DIRECTED) {
                    // Para grafos dirigidos, verifica a direção
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

                            double weight = 1.0; // peso padrão
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
                            selectNode(null); // Limpa seleção após conectar
                        }
                    }
                }
                break;

            case DISCONNECT:
                if (clickedNode != null) {
                    if (selectedNode == null) {
                        selectNode(clickedNode);
                    } else {
                        // Remove conexão entre selectedNode e clickedNode
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

        // Solicita ao usuário o vértice inicial
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
            return null; // Usuário cancelou
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
            // Armazena os resultados para visualização
            bfsTreeEdges = result.getTreeEdges();
            bfsVisitOrder = result.getVisitOrder();
            bfsStartVertex = result.getStartVertex();
            bfsDistances = result.getDistances();
            showBFS = true;

            // Mostra resultado detalhado
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

        // --- Decisão do Algoritmo ---
        if (currentGraphType == GraphType.UNDIRECTED) {
            analysisTitle = "Componentes Conexos (CC)";
            analysisDescription = String.format("Análise de Componentes Conexos (Grafo Não Direcionado) concluída.\n");
            components = BreadthFirstSearch.findConnectedComponents(vertexList, connectionList);
        } else {
            analysisTitle = "Componentes Fortemente Conexos (SCC)";
            analysisDescription = String.format("Análise de Componentes Fortemente Conexos (Grafo Direcionado) concluída.\n");
            components = KosarajuSCC.findSCCs(vertexList, connectionList);
        }

        // Processar os resultados para visualização (mesma lógica da resposta anterior)
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

        // Preparar a mensagem de resultado para o JOptionPane
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
            return null; // Usuário cancelou
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

            if (isMSTEdge) {
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
                // --- NOVO BLOCO DE RENDERIZAÇÃO ---
                drawComponentVertex(g2d, vertex);
            } else if (showBFS && bfsVisitOrder.contains(vertex)) {
                drawBFSVertex(g2d, vertex);
            } else if (showDFS && dfsVisitOrder.contains(vertex)) {
                drawDFSVertex(g2d, vertex);
            } else {
                vertex.draw(g);
            }
        }



        if (showMST && !mstEdges.isEmpty()) {
            drawMSTInfo(g2d);
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

    }

    // Em GraphPanel.java

    // Array de cores para diferenciar os componentes
    private static final Color[] COMPONENT_COLORS = {
            new Color(255, 100, 100), new Color(100, 200, 100), new Color(100, 100, 255),
            new Color(255, 180, 0), new Color(0, 200, 200), new Color(200, 0, 200),
            new Color(180, 180, 0), new Color(120, 120, 120)
    };

    /**
     * Desenha o vértice e destaca sua cor com base no ID do componente.
     */
    private void drawComponentVertex(Graphics2D g2d, Vertex vertex) {
        // Pega a cor baseada no ID do componente
        int componentId = componentMap.getOrDefault(vertex, 0);
        Color color = COMPONENT_COLORS[componentId % COMPONENT_COLORS.length];

        // Passa a cor de destaque para o método draw do vértice (assumindo que Vertex.draw foi modificado para aceitar cor)
        // Se Vertex.draw não aceita cor, você teria que desenhar o vértice aqui manualmente.
        // Exemplo de modificação sugerida para Vertex.draw(g, highlightColor):
        // vertex.draw(g, color);

        // Se Vertex.draw() não puder ser modificado, faça um override simples aqui:
        // 1. Chame o desenho padrão:
        vertex.draw(g2d);
        // 2. Adicione um contorno colorido extra:
        Stroke originalStroke = g2d.getStroke();
        g2d.setColor(color.darker());
        g2d.setStroke(new BasicStroke(3));
        g2d.drawOval(vertex.getX() - Vertex.NODE_RADIUS - 3, vertex.getY() - Vertex.NODE_RADIUS - 3,
                (Vertex.NODE_RADIUS + 3) * 2, (Vertex.NODE_RADIUS + 3) * 2);
        g2d.setStroke(originalStroke);
    }

    /**
     * Desenha informações sobre a contagem de componentes na tela.
     */
    private void drawComponentsInfo(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        String info = String.format("Componentes encontrados: %d", numberOfComponents);
        g2d.drawString(info, 10, getHeight() - 70); // Ajuste a posição Y conforme necessário
    }

    public void clearAllAlgorithmVisualizations() {
        clearBFS();
        clearComponents();
        clearMST();
        clearDFS();
        // Repaint é chamado dentro de cada clear individual, ou chame aqui uma vez.
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
            int y = vertex.getY() - 35; // Acima do vértice

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


    public List<Vertex> getVertexList() { return new ArrayList<>(vertexList); }
    public List<Connection> getConnectionList() { return new ArrayList<>(connectionList); }
    public GraphType getGraphType() { return currentGraphType; }

    public void setGraphType(GraphType graphType) {
        currentGraphType = graphType;
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

}