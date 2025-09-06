package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class GraphPanel extends JPanel {

    private List<Vertex> vertexList = new ArrayList<>();

    public enum ToolMode { ADD_NODE, REMOVE, CONNECT, DISCONNECT }
    private ToolMode currentMode = ToolMode.ADD_NODE;
    private Vertex selectedNode = null;
    private int nextVertexIdCounter = 1;

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
                            // TODO: Chamar selectedNode.addNeighbour(clickedNode);
                            // TODO: Adicionar a aresta na lista de arestas para desenhar a linha
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
        for (Vertex vertex : vertexList) {
            vertex.draw(g);
        }
    }
}