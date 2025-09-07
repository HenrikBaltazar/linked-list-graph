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
    private final List<Connection> connectionList = new ArrayList<>(); // Pode ser arestas ou arcos

    public enum ToolMode { ADD_NODE, REMOVE, CONNECT, DISCONNECT }
    private ToolMode currentMode = ToolMode.ADD_NODE;
    private Vertex selectedNode = null;
    private int nextVertexIdCounter = 1;
    private int nextConnectionIdCounter = 1;
    private GraphType graphType = GraphType.UNDIRECTED;

    // Variáveis para visualização da AGM
    private List<Connection> mstEdges = new ArrayList<>();
    private boolean showMST = false;
    private double mstTotalWeight = 0.0;

    // Variáveis para visualização da BFS
    private List<Connection> bfsTreeEdges = new ArrayList<>();
    private List<Vertex> bfsVisitOrder = new ArrayList<>();
    private boolean showBFS = false;
    private Vertex bfsStartVertex = null;
    private Map<Vertex, Integer> bfsDistances = new HashMap<>();

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

        // Cria array com os IDs dos vértices para seleção
        String[] vertexIds = vertexList.stream()
                .map(Vertex::getId)
                .toArray(String[]::new);

        // Solicita o primeiro vértice
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

        // Solicita o segundo vértice
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

        // Busca os vértices pelos IDs
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

        // Verifica adjacência
        boolean adjacent = areAdjacent(vertex1, vertex2);

        String connectionType = graphType == GraphType.DIRECTED ? "arco" : "aresta";
        String message;

        if (adjacent) {
            // Busca informações sobre a conexão
            Connection connection = findConnectionBetween(vertex1, vertex2);
            if (connection != null) {
                String direction;
                if (graphType == GraphType.DIRECTED) {
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

    // AGM --------------------------------------------------------------------------------
    public List<Connection> applyPrimAlgorithm() {
        if (graphType == GraphType.DIRECTED) {
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

        // Solicita ao usuário o vértice inicial
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

        // Executa o algoritmo de Prim
        Prim.PrimResult result = Prim.findMinimumSpanningTree(vertexList, connectionList, startVertex);

        if (result != null) {
            // Armazena os resultados para visualização
            mstEdges = result.getMstEdges();
            mstTotalWeight = result.getTotalWeight();
            showMST = true;

            // Mostra resultado detalhado
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

    // FIM AGM ----------------------------------------------------------------------------

    // BFS --------------------------------------------------------------------------------
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

        // Executa a busca em largura
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

    public List<Connection> getBFSTreeEdges() {
        return new ArrayList<>(bfsTreeEdges);
    }

    // FIM BFS ----------------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Desenha as conexões primeiro (para ficarem atrás dos vértices)
        for (Connection connection : connectionList) {
            boolean isMSTEdge = showMST && mstEdges.contains(connection);
            boolean isBFSEdge = showBFS && bfsTreeEdges.contains(connection);

            if (isMSTEdge) {
                drawMSTConnection(g2d, connection);
            } else if (isBFSEdge) {
                drawBFSConnection(g2d, connection);
            } else {
                connection.draw(g);
            }
        }

        // Desenha os vértices
        for (Vertex vertex : vertexList) {
            if (showBFS && bfsVisitOrder.contains(vertex)) {
                drawBFSVertex(g2d, vertex);
            } else {
                vertex.draw(g);
            }
        }

        drawGraphTypeInfo(g2d);

        // Desenha informações da AGM se estiver visível
        if (showMST && !mstEdges.isEmpty()) {
            drawMSTInfo(g2d);
        }

        // Desenha informações da BFS se estiver visível
        if (showBFS && !bfsTreeEdges.isEmpty()) {
            drawBFSInfo(g2d);
        }
    }


    // Funções de draw ------------------------------------------------------------------------

    private void drawGraphTypeInfo(Graphics2D g2d) {
        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        String typeText = graphType == GraphType.DIRECTED ? "Dirigido" : "Não Dirigido";
        g2d.drawString("Tipo: " + typeText, 10, 20);
    }

    private void drawMSTConnection(Graphics2D g2d, Connection connection) {
        // Salva o estado original
        Color originalColor = g2d.getColor();
        Stroke originalStroke = g2d.getStroke();

        // Define estilo para arestas da AGM
        g2d.setColor(new Color(0, 150, 0)); // Verde escuro
        g2d.setStroke(new BasicStroke(4)); // Mais espesso

        // Desenha a linha da conexão
        g2d.drawLine(
                connection.getSource().getX(),
                connection.getSource().getY(),
                connection.getTarget().getX(),
                connection.getTarget().getY()
        );

        // Desenha o peso e ID com destaque
        drawMSTWeightAndId(g2d, connection);

        // Restaura o estado original
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

        // Fundo verde claro para destacar
        g2d.setColor(new Color(200, 255, 200));
        g2d.fillRect(midX - textWidth/2 - paddingX, midY - textHeight/2 - paddingY,
                textWidth + (paddingX * 2), textHeight + (paddingY * 2));

        // Borda verde escura
        g2d.setColor(new Color(0, 150, 0));
        g2d.drawRect(midX - textWidth/2 - paddingX, midY - textHeight/2 - paddingY,
                textWidth + (paddingX * 2), textHeight + (paddingY * 2));

        // Texto em verde escuro
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
        // Salva o estado original
        Color originalColor = g2d.getColor();
        Stroke originalStroke = g2d.getStroke();

        // Define estilo para arestas da árvore BFS
        g2d.setColor(new Color(0, 100, 200)); // Azul
        g2d.setStroke(new BasicStroke(4)); // Mais espesso

        // Desenha a linha da conexão
        g2d.drawLine(
                connection.getSource().getX(),
                connection.getSource().getY(),
                connection.getTarget().getX(),
                connection.getTarget().getY()
        );

        // Desenha o peso e ID com destaque azul
        drawBFSWeightAndId(g2d, connection);

        // Restaura o estado original
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

        // Fundo azul claro para destacar
        g2d.setColor(new Color(200, 220, 255));
        g2d.fillRect(midX - textWidth/2 - paddingX, midY - textHeight/2 - paddingY,
                textWidth + (paddingX * 2), textHeight + (paddingY * 2));

        // Borda azul escura
        g2d.setColor(new Color(0, 100, 200));
        g2d.drawRect(midX - textWidth/2 - paddingX, midY - textHeight/2 - paddingY,
                textWidth + (paddingX * 2), textHeight + (paddingY * 2));

        // Texto em azul escuro
        g2d.setColor(new Color(0, 80, 160));
        g2d.drawString(combinedText, midX - textWidth/2, midY + textHeight/4);
    }

    private void drawBFSVertex(Graphics2D g2d, Vertex vertex) {
        // Desenha o vértice normal primeiro
        vertex.draw(g2d);

        // Adiciona informações da BFS
        if (bfsDistances.containsKey(vertex)) {
            int distance = bfsDistances.get(vertex);

            // Posição para o texto da distância
            int x = vertex.getX();
            int y = vertex.getY() - 35; // Acima do vértice

            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            FontMetrics fm = g2d.getFontMetrics();
            String distText = "d:" + distance;
            int textWidth = fm.stringWidth(distText);

            // Fundo para melhor legibilidade
            g2d.setColor(new Color(200, 220, 255, 200));
            g2d.fillOval(x - textWidth/2 - 3, y - 8, textWidth + 6, 16);

            // Borda
            g2d.setColor(new Color(0, 100, 200));
            g2d.drawOval(x - textWidth/2 - 3, y - 8, textWidth + 6, 16);

            // Texto da distância
            g2d.setColor(new Color(0, 80, 160));
            g2d.drawString(distText, x - textWidth/2, y + 4);

            // Destaque especial para o vértice inicial
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

    // Matrizes -------------------------------------------------------------------------------

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

    // Getters para acesso às listas -------------------------------

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