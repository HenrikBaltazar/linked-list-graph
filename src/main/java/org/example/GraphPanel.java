package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class GraphPanel extends JPanel {

    private List<Vertex> vertexList = new ArrayList<>();
    private List<Connection> connectionList = new ArrayList<>(); // Pode ser arestas ou arcos

    public enum ToolMode { ADD_NODE, REMOVE, CONNECT, DISCONNECT }
    private ToolMode currentMode = ToolMode.ADD_NODE;
    private Vertex selectedNode = null;
    private int nextVertexIdCounter = 1;
    private int nextConnectionIdCounter = 1;
    private GraphType graphType = GraphType.UNDIRECTED;

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

        // Remove todas as conexões (arestas/arcos) conectadas a este vértice
        connectionList.removeIf(connection ->
                connection.getSource().equals(vertex) || connection.getTarget().equals(vertex));

        // Remove o vértice da lista de adjacência dos outros vértices
        for (Vertex v : vertexList) {
            v.getNeighbours().remove(vertex);
        }

        // Remove o vértice
        boolean removed = vertexList.remove(vertex);

        if (selectedNode == vertex) {
            selectedNode = null;
        }

        return removed;
    }

    public Connection addConnection(String connectionId, Vertex source, Vertex target, double weight) {
        if (source == null || target == null) return null;

        // Para grafos não dirigidos, verifica se já existe uma aresta entre estes vértices
        if (graphType == GraphType.UNDIRECTED && hasConnectionBetween(source, target)) {
            System.out.println("Já existe uma aresta entre " + source.getId() + " e " + target.getId());
            return null;
        }

        // Para grafos dirigidos, verifica se já existe um arco com a mesma direção
        if (graphType == GraphType.DIRECTED && hasDirectedConnectionBetween(source, target)) {
            System.out.println("Já existe um arco de " + source.getId() + " para " + target.getId());
            return null;
        }

        if (connectionId == null || connectionId.isEmpty()) {
            String prefix = graphType == GraphType.DIRECTED ? "A" : "E"; // A para Arco, E para Edge
            connectionId = prefix + nextConnectionIdCounter++;
        }

        Connection newConnection;
        if (graphType == GraphType.DIRECTED) {
            newConnection = new Arc(connectionId, source, target, weight);
        } else {
            newConnection = new Edge(connectionId, source, target, weight);
        }

        connectionList.add(newConnection);

        // Atualiza as listas de adjacência dos vértices
        source.addNeighbour(target);
        if (graphType == GraphType.UNDIRECTED) {
            target.addNeighbour(source);
        }

        return newConnection;
    }

    public boolean removeConnection(Connection connection) {
        if (connection == null) return false;

        boolean removed = connectionList.remove(connection);

        if (removed) {
            // Remove da lista de adjacência
            connection.getSource().getNeighbours().remove(connection.getTarget());
            if (graphType == GraphType.UNDIRECTED) {
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
                    // Se o modo é SELECT (ou REMOVE), e clicamos em um nó, removemos ele.
                    // Primeiro selecionamos para dar feedback visual (se a remoção falhar)
                    selectNode(clickedNode);
                    removeSelectedNode(); // Executa a remoção imediatamente ao clicar no nó
                } else {
                    // Clicou fora de um nó, desmarca qualquer seleção
                    selectNode(null);
                }
                break;

            case CONNECT:
                if (clickedNode != null) {
                    if (selectedNode == null) {
                        // Seleciona o primeiro nó para a conexão
                        selectNode(clickedNode);
                    } else {
                        // Conecta o primeiro nó (selectedNode) ao segundo (clickedNode)
                        if (clickedNode != selectedNode) {
                            String connectionType = graphType == GraphType.DIRECTED ? "arco" : "aresta";
                            String direction = graphType == GraphType.DIRECTED ?
                                    selectedNode.getId() + " → " + clickedNode.getId() :
                                    selectedNode.getId() + " ↔ " + clickedNode.getId();

                            System.out.println("Criando " + connectionType + ": " + direction);

                            String weightStr = JOptionPane.showInputDialog(
                                    this,
                                    "Digite o peso da " + connectionType + ":",
                                    "Peso da " + (graphType == GraphType.DIRECTED ? "Arco" : "Aresta"),
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
                            String connectionType = graphType == GraphType.DIRECTED ? "arco" : "aresta";

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
        selectNode(null); // Limpa a seleção ao trocar de ferramenta
        System.out.println("Modo atual: " + mode); // Log de depuração
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Desenha as conexões primeiro (para ficarem atrás dos vértices)
        for (Connection connection : connectionList) {
            connection.draw(g);
        }

        for (Vertex vertex : vertexList) {
            vertex.draw(g);
        }

        drawGraphTypeInfo(g2d);
    }

    private void drawGraphTypeInfo(Graphics2D g2d) {
        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        String typeText = graphType == GraphType.DIRECTED ? "Dirigido" : "Não Dirigido";
        g2d.drawString("Tipo: " + typeText, 10, 20);
    }

    // ------------------------------- Matrizes
    public int[][] getAdjacencyMatrix() {
        int n = vertexList.size();
        int[][] matrix = new int[n][n];

        // Cria um mapa de vértice para índice
        java.util.Map<Vertex, Integer> vertexIndex = new java.util.HashMap<>();
        for (int i = 0; i < n; i++) {
            vertexIndex.put(vertexList.get(i), i);
        }

        // Preenche a matriz
        for (Connection connection : connectionList) {
            int sourceIndex = vertexIndex.get(connection.getSource());
            int targetIndex = vertexIndex.get(connection.getTarget());

            matrix[sourceIndex][targetIndex] = 1;

            // Se for não dirigido, marca também a posição simétrica
            if (graphType == GraphType.UNDIRECTED) {
                matrix[targetIndex][sourceIndex] = 1;
            }
        }

        return matrix;
    }

    public int[][] getIncidenceMatrix() {
        int n = vertexList.size();
        int m = connectionList.size();
        int[][] matrix = new int[n][m];

        // Cria um mapa de vértice para índice
        java.util.Map<Vertex, Integer> vertexIndex = new java.util.HashMap<>();
        for (int i = 0; i < n; i++) {
            vertexIndex.put(vertexList.get(i), i);
        }

        // Preenche a matriz
        for (int j = 0; j < m; j++) {
            Connection connection = connectionList.get(j);
            int sourceIndex = vertexIndex.get(connection.getSource());
            int targetIndex = vertexIndex.get(connection.getTarget());

            if (graphType == GraphType.DIRECTED) {
                // Para grafos dirigidos: +1 para saída, -1 para entrada
                matrix[sourceIndex][j] = 1;
                matrix[targetIndex][j] = -1;
            } else {
                // Para grafos não dirigidos: 1 para ambas as extremidades
                matrix[sourceIndex][j] = 1;
                matrix[targetIndex][j] = 1;
            }
        }

        return matrix;
    }

    // ------------------------------- Getters para acesso às listas
    public List<Vertex> getVertexList() { return new ArrayList<>(vertexList); }
    public List<Connection> getConnectionList() { return new ArrayList<>(connectionList); }
    public GraphType getGraphType() { return graphType; }

    public void setGraphType(GraphType graphType) {
        this.graphType = graphType;
        // Converte conexões existentes se necessário
        convertExistingConnections();
        repaint();
    }

    private void convertExistingConnections() {
        List<Connection> oldConnections = new ArrayList<>(connectionList);
        connectionList.clear();

        // Limpa listas de adjacência
        for (Vertex vertex : vertexList) {
            vertex.getNeighbours().clear();
        }

        // Recria as conexões com o novo tipo
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