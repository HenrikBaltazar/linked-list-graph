package org.example.algorithms;

import org.example.graph.Connection;
import org.example.graph.Vertex;

import java.util.*;

public class BreadthFirstSearch {
    public static class BFSResult {
        private final List<Connection> treeEdges;
        private final List<Vertex> visitOrder;
        private final Map<Vertex, Integer> distances;
        private final Map<Vertex, Vertex> parents;
        private final Vertex startVertex;

        public BFSResult(List<Connection> treeEdges, List<Vertex> visitOrder,
                         Map<Vertex, Integer> distances, Map<Vertex, Vertex> parents,
                         Vertex startVertex) {
            this.treeEdges = new ArrayList<>(treeEdges);
            this.visitOrder = new ArrayList<>(visitOrder);
            this.distances = new HashMap<>(distances);
            this.parents = new HashMap<>(parents);
            this.startVertex = startVertex;
        }

        public List<Connection> getTreeEdges() {
            return new ArrayList<>(treeEdges);
        }

        public List<Vertex> getVisitOrder() {
            return new ArrayList<>(visitOrder);
        }

        public Map<Vertex, Integer> getDistances() {
            return new HashMap<>(distances);
        }

        public Map<Vertex, Vertex> getParents() {
            return new HashMap<>(parents);
        }

        public Vertex getStartVertex() {
            return startVertex;
        }

        public int getTreeEdgeCount() {
            return treeEdges.size();
        }

        public int getVisitedVertexCount() {
            return visitOrder.size();
        }

        public int getDistanceToVertex(Vertex vertex) {
            return distances.getOrDefault(vertex, -1);
        }

        public List<Vertex> getPathToVertex(Vertex target) {
            List<Vertex> path = new ArrayList<>();
            Vertex current = target;

            while (current != null) {
                path.add(0, current);
                current = parents.get(current);
            }

            if (path.isEmpty() || !path.get(0).equals(startVertex)) {
                return new ArrayList<>();
            }

            return path;
        }
    }


    public static BFSResult performBFS(List<Vertex> vertexList,
                                       List<Connection> connectionList,
                                       Vertex startVertex) {

        if (vertexList == null || connectionList == null || startVertex == null) {
            return null;
        }

        if (vertexList.isEmpty()) {
            return null;
        }

        if (!vertexList.contains(startVertex)) {
            return null;
        }

        Queue<Vertex> queue = new LinkedList<>();
        Set<Vertex> visited = new HashSet<>();
        List<Vertex> visitOrder = new ArrayList<>();
        List<Connection> treeEdges = new ArrayList<>();
        Map<Vertex, Integer> distances = new HashMap<>();
        Map<Vertex, Vertex> parents = new HashMap<>();

        queue.offer(startVertex);
        visited.add(startVertex);
        visitOrder.add(startVertex);
        distances.put(startVertex, 0);
        parents.put(startVertex, null);

        while (!queue.isEmpty()) {
            Vertex current = queue.poll();
            int currentDistance = distances.get(current);

            for (Connection connection : connectionList) {
                Vertex neighbor = getOtherVertex(connection, current);

                if (neighbor != null && !visited.contains(neighbor)) {
                    visited.add(neighbor);
                    visitOrder.add(neighbor);

                    distances.put(neighbor, currentDistance + 1);
                    parents.put(neighbor, current);

                    treeEdges.add(connection);

                    queue.offer(neighbor);
                }
            }
        }

        return new BFSResult(treeEdges, visitOrder, distances, parents, startVertex);
    }


    private static Vertex getOtherVertex(Connection connection, Vertex vertex) {
        if (connection.getSource().equals(vertex)) {
            return connection.getTarget();
        } else if (connection.getTarget().equals(vertex) && !connection.isDirected()) {
            return connection.getSource();
        } else if (connection.getTarget().equals(vertex) && connection.isDirected()) {
            return null;
        }
        return null;
    }


    public static List<List<Vertex>> findConnectedComponents(List<Vertex> vertexList,
                                                             List<Connection> connectionList) {
        List<List<Vertex>> components = new ArrayList<>();
        Set<Vertex> globalVisited = new HashSet<>();

        for (Vertex vertex : vertexList) {
            if (!globalVisited.contains(vertex)) {
                BFSResult result = performBFS(vertexList, connectionList, vertex);
                if (result != null) {
                    List<Vertex> component = result.getVisitOrder();
                    components.add(component);
                    globalVisited.addAll(component);
                }
            }
        }

        return components;
    }


    public static boolean hasPath(List<Vertex> vertexList, List<Connection> connectionList,
                                  Vertex source, Vertex target) {
        BFSResult result = performBFS(vertexList, connectionList, source);
        return result != null && result.getVisitOrder().contains(target);
    }


    public static List<Vertex> findShortestPath(List<Vertex> vertexList, List<Connection> connectionList,
                                                Vertex source, Vertex target) {
        BFSResult result = performBFS(vertexList, connectionList, source);
        if (result != null) {
            return result.getPathToVertex(target);
        }
        return new ArrayList<>();
    }
}