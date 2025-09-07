package org.example.algorithms;

import org.example.*;
import java.util.*;

public class Prim {

    // Classe auxiliar para representar uma aresta com peso para o algoritmo de Prim
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

    // Resultado do algoritmo de Prim
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

    /**
     * Aplica o algoritmo de Prim para encontrar a Árvore Geradora Mínima (AGM)
     * @param vertexList Lista de vértices do grafo
     * @param connectionList Lista de conexões do grafo
     * @param startVertex Vértice inicial para o algoritmo
     * @return PrimResult com as arestas da AGM e informações adicionais, ou null se inválido
     */
    public static PrimResult findMinimumSpanningTree(List<Vertex> vertexList,
                                                     List<Connection> connectionList,
                                                     Vertex startVertex) {

        // Validações básicas
        if (vertexList == null || connectionList == null || startVertex == null) {
            return null;
        }

        if (vertexList.isEmpty() || connectionList.isEmpty()) {
            return null;
        }

        if (!vertexList.contains(startVertex)) {
            return null;
        }

        // Verifica se o grafo é conexo
        if (!isConnected(vertexList, connectionList)) {
            return null;
        }

        // Executa o algoritmo de Prim
        List<Connection> mst = primAlgorithm(vertexList, connectionList, startVertex);

        if (mst == null || mst.isEmpty()) {
            return null;
        }

        // Calcula o peso total
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

        // Adiciona o vértice inicial
        visited.add(startVertex);
        addAdjacentEdgesToQueue(startVertex, connectionList, visited, edgeQueue);

        while (!edgeQueue.isEmpty() && mst.size() < vertexList.size() - 1) {
            WeightedEdge minEdge = edgeQueue.poll();

            // Se o vértice de destino já foi visitado, pula esta aresta
            if (visited.contains(minEdge.to)) {
                continue;
            }

            // Adiciona a aresta à AGM
            mst.add(minEdge.connection);
            visited.add(minEdge.to);

            // Adiciona as arestas adjacentes do novo vértice à fila
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

            // Para arestas não dirigidas, verifica ambas as direções
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

    /**
     * Verifica se o grafo não dirigido é conexo usando BFS
     * @param vertexList Lista de vértices
     * @param connectionList Lista de conexões
     * @return true se o grafo for conexo, false caso contrário
     */
    public static boolean isConnected(List<Vertex> vertexList, List<Connection> connectionList) {
        if (vertexList.isEmpty()) return true;

        Set<Vertex> visited = new HashSet<>();
        Queue<Vertex> queue = new LinkedList<>();

        // Começa do primeiro vértice
        Vertex start = vertexList.get(0);
        queue.offer(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Vertex current = queue.poll();

            // Encontra todos os vizinhos através das conexões
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

        // Se visitamos todos os vértices, o grafo é conexo
        return visited.size() == vertexList.size();
    }

    /**
     * Verifica se uma lista de arestas forma uma árvore geradora válida
     * @param vertexCount Número de vértices no grafo
     * @param mstEdges Lista de arestas da suposta AGM
     * @return true se for uma árvore geradora válida
     */
    public static boolean isValidSpanningTree(int vertexCount, List<Connection> mstEdges) {
        // Uma árvore geradora deve ter exatamente V-1 arestas
        return mstEdges.size() == vertexCount - 1;
    }
}