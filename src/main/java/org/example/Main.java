package org.example;

import org.example.graph.Graph;
import org.example.graph.Vertex;
import org.example.ui.Interface;

public class Main {
    public static void main(String[] args) {
        Graph graph = new Graph();
        Vertex v1 = new Vertex();
        Vertex v2 = new Vertex();
        Vertex v3 = new Vertex();
        Vertex v4 = new Vertex();
        v1.addNeighbour(v2);
        v1.addNeighbour(v3);
        v2.addNeighbour(v4);
        v3.addNeighbour(v1);
        v3.addNeighbour(v4);
        graph.addVertex(v1);
        graph.addVertex(v2);
        graph.addVertex(v3);
        graph.addVertex(v4);
        System.out.println(graph.toString());

        Interface ui = new Interface(graph);
        ui.setVisible(args.length == 0 || !args[0].equals("-h"));

    }
}