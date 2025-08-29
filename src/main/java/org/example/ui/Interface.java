package org.example.ui;

import org.example.graph.Graph;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Interface extends JFrame {
    private static final int W=800,H=600;
    private Graph graph;
    private Toolbar toolbar;
    private List<Node> nodes = new ArrayList<>();
    public Interface(Graph graph) {
        this.graph = graph;
        toolbar = new Toolbar(this);
        setMinimumSize(new Dimension(W,H));
        setTitle("Grafo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        getContentPane().add(toolbar, BorderLayout.PAGE_START);
    }

    public List<Node> getNodes(){
        return nodes;
    }

    public void addNode(Node node){
        nodes.add(node);
    }

    public Graph getGraph() {
        return graph;
    }

    public int getWidth() {
        return W;
    }
    public int getHeight() {
        return H;
    }

}
