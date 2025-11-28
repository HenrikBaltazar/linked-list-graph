package org.example.algorithms;

import org.example.graph.Connection;
import org.example.graph.Vertex;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Individual implements Comparable<Individual> {

    private List<Vertex> genes; // A sequência de cidades (rota)
    private double fitness;     // O custo total (quanto menor, melhor)

    // Construtor cria o indivíduo e já calcula o fitness se tivermos as conexões
    public Individual(List<Vertex> genes, List<Connection> allConnections) {
        this.genes = new ArrayList<>(genes);
        calculateFitness(allConnections);
    }

    public void calculateFitness(List<Connection> connections) {
        double cost = 0;
        int n = genes.size();

        for (int i = 0; i < n; i++) {
            Vertex from = genes.get(i);
            Vertex to = genes.get((i + 1) % n); // Volta para o primeiro no final

            Connection conn = findConnection(from, to, connections);

            if (conn != null) {
                cost += conn.getWeight();
            } else {
                // Penalidade para rota impossível (caminho inexistente)
                cost += 100000.0;
            }
        }
        this.fitness = cost;
    }

    // Função auxiliar para achar conexão na lista
    private Connection findConnection(Vertex v1, Vertex v2, List<Connection> connections) {
        for (Connection c : connections) {
            // Verifica conexão nos dois sentidos (assumindo grafo não direcionado ou tratando a direção na Connection)
            if (c.connects(v1, v2)) {
                return c;
            }
        }
        return null;
    }

    public List<Vertex> getGenes() {
        return genes;
    }

    public double getFitness() {
        return fitness;
    }

    @Override
    public int compareTo(Individual other) {
        return Double.compare(this.fitness, other.fitness);
    }

    @Override
    public String toString() {
        String route = genes.stream().map(Vertex::getId).collect(Collectors.joining(" -> "));
        return String.format("Custo: %.2f | Rota: %s", fitness, route);
    }
}