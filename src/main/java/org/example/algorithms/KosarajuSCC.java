package org.example.algorithms;// Crie uma nova classe, por exemplo: package org.example.algorithms;

import org.example.Arc;
import org.example.Connection; // Ajuste os imports conforme sua estrutura
import org.example.Edge;
import org.example.Vertex;   // Ajuste os imports conforme sua estrutura

import java.util.*;

public class KosarajuSCC {

    /**
     * Encontra todos os Componentes Fortemente Conexos (SCCs) em um grafo direcionado.
     * Implementação do algoritmo de Kosaraju.
     *
     * @param vertexList     Lista de todos os vértices do grafo.
     * @param connectionList Lista de todas as arestas (conexões) do grafo.
     * @return Uma lista de listas, onde cada lista interna representa um SCC.
     */
    public static List<List<Vertex>> findSCCs(List<Vertex> vertexList, List<Connection> connectionList) {
        // Passo 1: Primeira passagem do DFS no grafo original (G) para preencher a pilha de finalização.
        Stack<Vertex> finishStack = new Stack<>();
        Set<Vertex> visited = new HashSet<>();

        for (Vertex vertex : vertexList) {
            if (!visited.contains(vertex)) {
                dfsPass1(vertex, visited, finishStack, connectionList);
            }
        }

        // Passo 2: Transpor o grafo (criar G_T).
        List<Connection> transposedConnections = transposeGraph(connectionList);

        // Passo 3: Segunda passagem do DFS no grafo transposto (G_T), processando vértices na ordem da pilha.
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

    /**
     * Passagem 1 do DFS: preenche a pilha com os vértices na ordem de finalização (pós-ordem).
     */
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
        stack.push(u); // Adiciona à pilha após visitar todos os vizinhos
    }

    /**
     * Passagem 2 do DFS: coleta os vértices de um componente fortemente conexo no grafo transposto.
     */
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

    /**
     * Inverte a direção de todas as arestas do grafo.
     */
    private static List<Connection> transposeGraph(List<Connection> connectionList) {
        List<Connection> transposedList = new ArrayList<>();

        for (Connection originalConnection : connectionList) {
            Vertex source = originalConnection.getSource();
            Vertex target = originalConnection.getTarget();
            Connection transposedEdge = null; // Inicializa como nulo

            // Verifica o tipo de conexão para instanciar a classe concreta correta
            if (originalConnection instanceof Arc) {
                // Se for um Arco, crie um novo Arco com direção invertida.
                // Precisamos passar os parâmetros do construtor de Arc.
                transposedEdge = new Arc(
                        originalConnection.getId() + "_T", // Novo ID para a aresta transposta (opcional)
                        target, // Nova origem é o antigo destino
                        source, // Novo destino é a antiga origem
                        originalConnection.getWeight()
                );
            } else if (originalConnection instanceof Edge) {
                // Se for uma Aresta (grafo não direcionado), a transposição é conceitualmente
                // a própria aresta, mas para a lógica do Kosaraju funcionar (assumindo
                // que a busca DFS trata arestas como direcionadas durante a execução),
                // criamos uma nova instância invertida.
                transposedEdge = new Edge(
                        originalConnection.getId() + "_T",
                        target,
                        source,
                        originalConnection.getWeight()
                );
            } else {
                // Se houver outros tipos de conexão ou se Connection não for sempre Arc/Edge, trate aqui.
                System.err.println("Tipo de conexão desconhecido durante a transposição: " + originalConnection.getClass().getName());
            }

            if (transposedEdge != null) {
                transposedList.add(transposedEdge);
            }
        }
        return transposedList;
    }
}