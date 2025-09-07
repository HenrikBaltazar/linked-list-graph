
package org.example.algorithms;

import org.example.*;
import java.util.*;

public class DepthFirstSearch {

    // Resultado da busca em profundidade
    public static class DFSResult {
        private final List<Connection> treeEdges;
        private final List<Vertex> visitOrder;
        private final Map<Vertex, Integer> discoveryTimes;
        private final Map<Vertex, Integer> finishTimes;
        private final Map<Vertex, Vertex> parents;
        private final Vertex startVertex;

        public DFSResult(List<Connection> treeEdges, List<Vertex> visitOrder,
                         Map<Vertex, Integer> discoveryTimes, Map<Vertex, Integer> finishTimes,
                         Map<Vertex, Vertex> parents, Vertex startVertex) {
            this.treeEdges = new ArrayList<>(treeEdges);
            this.visitOrder = new ArrayList<>(visitOrder);
            this.discoveryTimes = new HashMap<>(discoveryTimes);
            this.finishTimes = new HashMap<>(finishTimes);
            this.parents = new HashMap<>(parents);
            this.startVertex = startVertex;
        }

        public List<Connection> getTreeEdges() {
            return new ArrayList<>(treeEdges);
        }

        public List<Vertex> getVisitOrder() {
            return new ArrayList<>(visitOrder);
        }

        public Map<Vertex, Integer> getDiscoveryTimes() {
            return new HashMap<>(discoveryTimes);
        }

        public Map<Vertex, Integer> getFinishTimes() {
            return new HashMap<>(finishTimes);
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

        public int getDiscoveryTime(Vertex vertex) {
            return discoveryTimes.getOrDefault(vertex, -1);
        }

        public int getFinishTime(Vertex vertex) {
            return finishTimes.getOrDefault(vertex, -1);
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
     * Executa busca em profundidade (DFS) a partir de um vértice inicial
     * @param vertexList Lista de vértices do grafo
     * @param connectionList Lista de conexões do grafo
     * @param startVertex Vértice inicial para a busca
     * @return DFSResult com a árvore gerada e informações da busca, ou null se inválido
     */
    public static DFSResult performDFS(List<Vertex> vertexList,
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

        // Estruturas de dados para DFS
        Set<Vertex> visited = new HashSet<>();
        List<Vertex> visitOrder = new ArrayList<>();
        List<Connection> treeEdges = new ArrayList<>();
        Map<Vertex, Integer> discoveryTimes = new HashMap<>();
        Map<Vertex, Integer> finishTimes = new HashMap<>();
        Map<Vertex, Vertex> parents = new HashMap<>();

        // Contador de tempo (para discovery e finish times)
        int[] time = {0};

        // Executa DFS recursivo
        dfsRecursive(startVertex, connectionList, visited, visitOrder, treeEdges,
                discoveryTimes, finishTimes, parents, time);

        return new DFSResult(treeEdges, visitOrder, discoveryTimes, finishTimes, parents, startVertex);
    }

    /**
     * Implementação recursiva do DFS
     */
    private static void dfsRecursive(Vertex current, List<Connection> connectionList,
                                     Set<Vertex> visited, List<Vertex> visitOrder,
                                     List<Connection> treeEdges, Map<Vertex, Integer> discoveryTimes,
                                     Map<Vertex, Integer> finishTimes, Map<Vertex, Vertex> parents,
                                     int[] time) {

        // Marca como visitado e registra tempo de descoberta
        visited.add(current);
        visitOrder.add(current);
        discoveryTimes.put(current, time[0]++);

        // Explora todos os vizinhos não visitados
        for (Connection connection : connectionList) {
            Vertex neighbor = getOtherVertex(connection, current);

            if (neighbor != null && !visited.contains(neighbor)) {
                // Adiciona aresta à árvore DFS
                treeEdges.add(connection);
                parents.put(neighbor, current);

                // Chamada recursiva
                dfsRecursive(neighbor, connectionList, visited, visitOrder, treeEdges,
                        discoveryTimes, finishTimes, parents, time);
            }
        }

        // Registra tempo de finalização
        finishTimes.put(current, time[0]++);
    }

    /**
     * Executa DFS iterativo usando pilha (Stack) - alternativa à implementação recursiva
     */
    public static DFSResult performDFSIterative(List<Vertex> vertexList,
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

        // Estruturas de dados para DFS iterativo
        Stack<Vertex> stack = new Stack<>();
        Set<Vertex> visited = new HashSet<>();
        List<Vertex> visitOrder = new ArrayList<>();
        List<Connection> treeEdges = new ArrayList<>();
        Map<Vertex, Integer> discoveryTimes = new HashMap<>();
        Map<Vertex, Integer> finishTimes = new HashMap<>();
        Map<Vertex, Vertex> parents = new HashMap<>();

        int time = 0;

        // Inicializa DFS
        stack.push(startVertex);

        while (!stack.isEmpty()) {
            Vertex current = stack.peek();

            if (!visited.contains(current)) {
                // Primeira visita ao vértice
                visited.add(current);
                visitOrder.add(current);
                discoveryTimes.put(current, time++);

                // Adiciona vizinhos não visitados à pilha
                boolean hasUnvisitedNeighbor = false;
                for (Connection connection : connectionList) {
                    Vertex neighbor = getOtherVertex(connection, current);

                    if (neighbor != null && !visited.contains(neighbor)) {
                        stack.push(neighbor);
                        parents.put(neighbor, current);
                        treeEdges.add(connection);
                        hasUnvisitedNeighbor = true;
                        break; // Adiciona apenas um vizinho por vez para manter ordem correta
                    }
                }

                // Se não há vizinhos não visitados, pode finalizar
                if (!hasUnvisitedNeighbor) {
                    stack.pop();
                    finishTimes.put(current, time++);
                }
            } else {
                // Vértice já processado, remove da pilha
                stack.pop();
                if (!finishTimes.containsKey(current)) {
                    finishTimes.put(current, time++);
                }
            }
        }

        return new DFSResult(treeEdges, visitOrder, discoveryTimes, finishTimes, parents, startVertex);
    }

    /**
     * Obtém o vértice oposto em uma conexão
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
     * Verifica quantos componentes conexos o grafo possui através de múltiplas DFS
     */
    public static List<List<Vertex>> findConnectedComponents(List<Vertex> vertexList,
                                                             List<Connection> connectionList) {
        List<List<Vertex>> components = new ArrayList<>();
        Set<Vertex> globalVisited = new HashSet<>();

        for (Vertex vertex : vertexList) {
            if (!globalVisited.contains(vertex)) {
                DFSResult result = performDFS(vertexList, connectionList, vertex);
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
     * Verifica se existe caminho entre dois vértices usando DFS
     */
    public static boolean hasPath(List<Vertex> vertexList, List<Connection> connectionList,
                                  Vertex source, Vertex target) {
        DFSResult result = performDFS(vertexList, connectionList, source);
        return result != null && result.getVisitOrder().contains(target);
    }

    /**
     * Detecta se há ciclo no grafo usando DFS (para grafos não dirigidos)
     */
    public static boolean hasCycle(List<Vertex> vertexList, List<Connection> connectionList) {
        Set<Vertex> visited = new HashSet<>();

        for (Vertex vertex : vertexList) {
            if (!visited.contains(vertex)) {
                if (hasCycleDFS(vertex, null, connectionList, visited)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean hasCycleDFS(Vertex current, Vertex parent,
                                       List<Connection> connectionList, Set<Vertex> visited) {
        visited.add(current);

        for (Connection connection : connectionList) {
            Vertex neighbor = getOtherVertex(connection, current);

            if (neighbor != null) {
                if (!visited.contains(neighbor)) {
                    if (hasCycleDFS(neighbor, current, connectionList, visited)) {
                        return true;
                    }
                } else if (!neighbor.equals(parent)) {
                    // Encontrou um vértice já visitado que não é o pai -> ciclo
                    return true;
                }
            }
        }

        return false;
    }
}