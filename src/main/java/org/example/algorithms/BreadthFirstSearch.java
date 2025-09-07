package org.example.algorithms;

import org.example.*;
import java.util.*;

public class BreadthFirstSearch {

    // Resultado da busca em largura
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
                path.add(0, current); // Adiciona no início para ter o caminho correto
                current = parents.get(current);
            }

            // Se não conseguiu chegar até o vértice inicial, não há caminho
            if (path.isEmpty() || !path.get(0).equals(startVertex)) {
                return new ArrayList<>();
            }

            return path;
        }
    }

    /**
     * Executa busca em largura (BFS) a partir de um vértice inicial
     * @param vertexList Lista de vértices do grafo
     * @param connectionList Lista de conexões do grafo
     * @param startVertex Vértice inicial para a busca
     * @return BFSResult com a árvore gerada e informações da busca, ou null se inválido
     */
    public static BFSResult performBFS(List<Vertex> vertexList,
                                       List<Connection> connectionList,
                                       Vertex startVertex) {

        // Validações básicas
        if (vertexList == null || connectionList == null || startVertex == null) {
            return null;
        }

        if (vertexList.isEmpty()) {
            return null;
        }

        if (!vertexList.contains(startVertex)) {
            return null;
        }

        // Estruturas de dados para BFS
        Queue<Vertex> queue = new LinkedList<>();
        Set<Vertex> visited = new HashSet<>();
        List<Vertex> visitOrder = new ArrayList<>();
        List<Connection> treeEdges = new ArrayList<>();
        Map<Vertex, Integer> distances = new HashMap<>();
        Map<Vertex, Vertex> parents = new HashMap<>();

        // Inicializa BFS
        queue.offer(startVertex);
        visited.add(startVertex);
        visitOrder.add(startVertex);
        distances.put(startVertex, 0);
        parents.put(startVertex, null);

        // Executa BFS
        while (!queue.isEmpty()) {
            Vertex current = queue.poll();
            int currentDistance = distances.get(current);

            // Encontra todos os vizinhos não visitados
            for (Connection connection : connectionList) {
                Vertex neighbor = getOtherVertex(connection, current);

                if (neighbor != null && !visited.contains(neighbor)) {
                    // Marca como visitado
                    visited.add(neighbor);
                    visitOrder.add(neighbor);

                    // Atualiza distância e pai
                    distances.put(neighbor, currentDistance + 1);
                    parents.put(neighbor, current);

                    // Adiciona aresta à árvore BFS
                    treeEdges.add(connection);

                    // Adiciona à fila para próxima exploração
                    queue.offer(neighbor);
                }
            }
        }

        return new BFSResult(treeEdges, visitOrder, distances, parents, startVertex);
    }

    /**
     * Obtém o vértice oposto em uma conexão
     * @param connection A conexão
     * @param vertex O vértice conhecido
     * @return O outro vértice da conexão, ou null se o vértice não estiver na conexão
     */
    private static Vertex getOtherVertex(Connection connection, Vertex vertex) {
        if (connection.getSource().equals(vertex)) {
            return connection.getTarget();
        } else if (connection.getTarget().equals(vertex) && !connection.isDirected()) {
            // Para grafos não dirigidos, pode ir em ambas as direções
            return connection.getSource();
        } else if (connection.getTarget().equals(vertex) && connection.isDirected()) {
            // Para grafos dirigidos, só pode seguir se for o target de uma aresta que chega
            return null;
        }
        return null;
    }

    /**
     * Verifica quantos componentes conexos o grafo possui através de múltiplas BFS
     * @param vertexList Lista de vértices
     * @param connectionList Lista de conexões
     * @return Lista de listas, cada uma representando um componente conexo
     */
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

    /**
     * Verifica se existe caminho entre dois vértices
     * @param vertexList Lista de vértices
     * @param connectionList Lista de conexões
     * @param source Vértice origem
     * @param target Vértice destino
     * @return true se existe caminho, false caso contrário
     */
    public static boolean hasPath(List<Vertex> vertexList, List<Connection> connectionList,
                                  Vertex source, Vertex target) {
        BFSResult result = performBFS(vertexList, connectionList, source);
        return result != null && result.getVisitOrder().contains(target);
    }

    /**
     * Encontra o menor caminho entre dois vértices (em número de arestas)
     * @param vertexList Lista de vértices
     * @param connectionList Lista de conexões
     * @param source Vértice origem
     * @param target Vértice destino
     * @return Lista de vértices representando o caminho, ou lista vazia se não houver caminho
     */
    public static List<Vertex> findShortestPath(List<Vertex> vertexList, List<Connection> connectionList,
                                                Vertex source, Vertex target) {
        BFSResult result = performBFS(vertexList, connectionList, source);
        if (result != null) {
            return result.getPathToVertex(target);
        }
        return new ArrayList<>();
    }
}