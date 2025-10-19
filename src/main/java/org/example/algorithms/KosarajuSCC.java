package org.example.algorithms;

import org.example.graph.Arc;
import org.example.graph.Connection;
import org.example.graph.Edge;
import org.example.graph.Vertex;
import java.util.*;

public class KosarajuSCC {
    public static List<List<Vertex>> findSCCs(List<Vertex> vertexList, List<Connection> connectionList) {
        Stack<Vertex> finishStack = new Stack<>();
        Set<Vertex> visited = new HashSet<>();

        for (Vertex vertex : vertexList) {
            if (!visited.contains(vertex)) {
                dfsPass1(vertex, visited, finishStack, connectionList);
            }
        }

        List<Connection> transposedConnections = transposeGraph(connectionList);

        visited.clear();
        List<List<Vertex>> stronglyConnectedComponents = new ArrayList<>();

        while (!finishStack.isEmpty()) {
            Vertex vertexToExplore = finishStack.pop();
            if (!visited.contains(vertexToExplore)) {
                List<Vertex> currentSCC = new ArrayList<>();
                dfsPass2(vertexToExplore, visited, currentSCC, transposedConnections);
                stronglyConnectedComponents.add(currentSCC);
            }
        }

        return stronglyConnectedComponents;
    }


    private static void dfsPass1(Vertex u, Set<Vertex> visited, Stack<Vertex> stack, List<Connection> connectionList) {
        visited.add(u);

        for (Connection connection : connectionList) {
            if (connection.getSource().equals(u)) {
                Vertex v = connection.getTarget();
                if (!visited.contains(v)) {
                    dfsPass1(v, visited, stack, connectionList);
                }
            }
        }
        stack.push(u);
    }


    private static void dfsPass2(Vertex u, Set<Vertex> visited, List<Vertex> currentSCC, List<Connection> transposedConnectionList) {
        visited.add(u);
        currentSCC.add(u);

        for (Connection connection : transposedConnectionList) {
            if (connection.getSource().equals(u)) {
                Vertex v = connection.getTarget();
                if (!visited.contains(v)) {
                    dfsPass2(v, visited, currentSCC, transposedConnectionList);
                }
            }
        }
    }


    private static List<Connection> transposeGraph(List<Connection> connectionList) {
        List<Connection> transposedList = new ArrayList<>();

        for (Connection originalConnection : connectionList) {
            Vertex source = originalConnection.getSource();
            Vertex target = originalConnection.getTarget();
            Connection transposedEdge = null;

            if (originalConnection instanceof Arc) {
                transposedEdge = new Arc(
                        originalConnection.getId() + "_T",
                        target,
                        source,
                        originalConnection.getWeight()
                );
            } else if (originalConnection instanceof Edge) {
                transposedEdge = new Edge(
                        originalConnection.getId() + "_T",
                        target,
                        source,
                        originalConnection.getWeight()
                );
            } else {
                System.err.println("Tipo de conexão desconhecido durante a transposição: " + originalConnection.getClass().getName());
            }

            if (transposedEdge != null) {
                transposedList.add(transposedEdge);
            }
        }
        return transposedList;
    }
}