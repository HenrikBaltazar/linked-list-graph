package org.example.algorithms;

import org.example.graph.Connection;
import org.example.graph.Vertex;

import java.util.*;

public class Prim {

    private static class WeightedEdge implements Comparable<WeightedEdge> {
        public final Vertex from;
        public final Vertex to;
        public final double weight;
        public final Connection connection;

        public WeightedEdge(Vertex from, Vertex to, double weight, Connection connection) {
            this.from = from;
            this.to = to;
            this.weight = weight;
            this.connection = connection;
        }

        @Override
        public int compareTo(WeightedEdge other) {
            return Double.compare(this.weight, other.weight);
        }
    }

    public static class PrimResult {
        private final List<Connection> mstEdges;
        private final double totalWeight;
        private final Vertex startVertex;

        public PrimResult(List<Connection> mstEdges, double totalWeight, Vertex startVertex) {
            this.mstEdges = new ArrayList<>(mstEdges);
            this.totalWeight = totalWeight;
            this.startVertex = startVertex;
        }

        public List<Connection> getMstEdges() { return new ArrayList<>(mstEdges); }
        public double getTotalWeight() { return totalWeight; }
        public Vertex getStartVertex() { return startVertex; }
        public int getEdgeCount() { return mstEdges.size(); }
    }

    public static PrimResult findMinimumSpanningTree(List<Vertex> vertexList,
                                                     List<Connection> connectionList,
                                                     Vertex startVertex) {

        if (vertexList == null || connectionList == null || startVertex == null) {
            return null;
        }

        if (vertexList.isEmpty() || connectionList.isEmpty()) {
            return null;
        }

        if (!vertexList.contains(startVertex)) {
            return null;
        }

        if (!isConnected(vertexList, connectionList)) {
            return null;
        }

        List<Connection> mst = primAlgorithm(vertexList, connectionList, startVertex);

        if (mst == null || mst.isEmpty()) {
            return null;
        }

        double totalWeight = mst.stream()
                .mapToDouble(Connection::getWeight)
                .sum();

        return new PrimResult(mst, totalWeight, startVertex);
    }

    private static List<Connection> primAlgorithm(List<Vertex> vertexList,
                                                  List<Connection> connectionList,
                                                  Vertex startVertex) {

        List<Connection> mst = new ArrayList<>();
        Set<Vertex> visited = new HashSet<>();
        PriorityQueue<WeightedEdge> edgeQueue = new PriorityQueue<>();

        visited.add(startVertex);
        addAdjacentEdgesToQueue(startVertex, connectionList, visited, edgeQueue);

        while (!edgeQueue.isEmpty() && mst.size() < vertexList.size() - 1) {
            WeightedEdge minEdge = edgeQueue.poll();

            if (visited.contains(minEdge.to)) {
                continue;
            }

            mst.add(minEdge.connection);
            visited.add(minEdge.to);

            addAdjacentEdgesToQueue(minEdge.to, connectionList, visited, edgeQueue);
        }

        return mst;
    }

    private static void addAdjacentEdgesToQueue(Vertex vertex,
                                                List<Connection> connectionList,
                                                Set<Vertex> visited,
                                                PriorityQueue<WeightedEdge> edgeQueue) {

        for (Connection connection : connectionList) {
            Vertex other = null;

            if (connection.getSource().equals(vertex) && !visited.contains(connection.getTarget())) {
                other = connection.getTarget();
            } else if (connection.getTarget().equals(vertex) && !visited.contains(connection.getSource())) {
                other = connection.getSource();
            }

            if (other != null) {
                edgeQueue.offer(new WeightedEdge(vertex, other, connection.getWeight(), connection));
            }
        }
    }

    public static boolean isConnected(List<Vertex> vertexList, List<Connection> connectionList) {
        if (vertexList.isEmpty()) return true;

        Set<Vertex> visited = new HashSet<>();
        Queue<Vertex> queue = new LinkedList<>();

        Vertex start = vertexList.get(0);
        queue.offer(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Vertex current = queue.poll();

            for (Connection connection : connectionList) {
                Vertex neighbor = null;

                if (connection.getSource().equals(current)) {
                    neighbor = connection.getTarget();
                } else if (connection.getTarget().equals(current)) {
                    neighbor = connection.getSource();
                }

                if (neighbor != null && !visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.offer(neighbor);
                }
            }
        }

        return visited.size() == vertexList.size();
    }


    public static boolean isValidSpanningTree(int vertexCount, List<Connection> mstEdges) {
        return mstEdges.size() == vertexCount - 1;
    }
}