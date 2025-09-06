package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class GraphPanel extends JPanel {

    private List<Vertex> vertexList = new ArrayList<>();
    private List<Edge> edgeList = new ArrayList<>();

    public enum ToolMode { ADD_NODE, REMOVE, CONNECT, DISCONNECT }
    private ToolMode currentMode = ToolMode.ADD_NODE;
    private Vertex selectedNode = null;
    private int nextVertexIdCounter = 1;
    private int nextEdgeIdCounter = 1;

    // Por enquanto assumir que o grafo é não-dirigido (Implementar depois)
    private boolean isDirected = true;

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

    public Edge addEdge(String edgeId, Vertex source, Vertex target, double weight) {
        if (source == null || target == null) return null;

        if (hasEdgeBetween(source, target)) {
            System.out.println("Já existe uma aresta entre " + source.getId() + " e " + target.getId());
            return null;
        }

        if (edgeId == null || edgeId.isEmpty()) {
            edgeId = "E" + nextEdgeIdCounter++;
        }

        Edge newEdge = new Edge(edgeId, source, target, weight);
        edgeList.add(newEdge);

        // Atualiza as listas de adjacência dos vértices
        source.addNeighbour(target);
        if (!isDirected) {
            target.addNeighbour(source);
        }

        return newEdge;
    }

    public boolean removeEdge(Edge edge) {
        if (edge == null) return false;

        boolean removed = edgeList.remove(edge);

        if (removed) {
            // Remove da lista de adjacência
            edge.getSource().getNeighbours().remove(edge.getTarget());
            if (!isDirected) {
                edge.getTarget().getNeighbours().remove(edge.getSource());
            }
        }

        return removed;
    }

    public boolean removeEdgeBetween(Vertex v1, Vertex v2) {
        Edge edge = findEdgeBetween(v1, v2);
        return removeEdge(edge);
    }

    public boolean areAdjacent(Vertex v1, Vertex v2) {
        if (v1 == null || v2 == null) return false;
        return v1.getNeighbours().contains(v2);
    }

    public boolean hasEdgeBetween(Vertex v1, Vertex v2) {
        return findEdgeBetween(v1, v2) != null;
    }

    public Edge findEdgeBetween(Vertex v1, Vertex v2) {
        for (Edge edge : edgeList) {
            if (edge.connects(v1, v2)) {
                return edge;
            }
        }
        return null;
    }

    public double getEdgeWeight(Edge edge) {
        return edge != null ? edge.getWeight() : 0;
    }

    public Vertex[] getEdgeEndpoints(Edge edge) {
        return edge != null ? edge.getEndpoints() : null;
    }

    public void removeSelectedNode() {
        if (selectedNode != null) {
            this.vertexList.remove(selectedNode);
            selectedNode.setSelected(false); // Garante que a seleção seja limpa visualmente
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
                            System.out.println("Conectando " + selectedNode.getId() + " -> " + clickedNode.getId());

                            // Solicita o peso da aresta ao usuário
                            String weightStr = JOptionPane.showInputDialog(
                                    this,
                                    "Digite o peso da aresta:",
                                    "Peso da Aresta",
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

                            addEdge(null, selectedNode, clickedNode, weight);
                            selectNode(null); // Limpa seleção após conectar
                        }
                    }
                }
                break;

            case DISCONNECT:
                // TODO: Implementar lógica de desconexão (selecionar aresta ou dois nós)
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

        // Desenha as arestas primeiro (para ficarem atrás dos vértices)
        for (Edge edge : edgeList) {
            edge.draw(g, isDirected);
        }

        for (Vertex vertex : vertexList) {
            vertex.draw(g);
        }
    }

    // Getters para acesso às listas
    public List<Vertex> getVertexList() { return new ArrayList<>(vertexList); }
    public List<Edge> getEdgeList() { return new ArrayList<>(edgeList); }

    // Getter e setter para tipo do grafo (será usado depois)
    public boolean isDirected() { return isDirected; }
    public void setDirected(boolean directed) { this.isDirected = directed; }

    public void switchIsDirected() {
        System.out.println("directed: " + isDirected);
        isDirected = !isDirected;
    }
    
}