package org.example.graph;

import org.example.ui.Node;

import java.util.ArrayList;
import java.util.List;

public class Graph {
    private List<Vertex> graph;
    public Graph(){
        graph = new ArrayList<>();
    }

    public void addVertex(Vertex vertex){
        vertex.setIndex(graph.size()+1);
        graph.add(vertex);
    }

    public void removeVertex(int index){
        graph.remove(getVertex(index));
    }

    public List<Vertex> getGraph(){
        return graph;
    }

    public Vertex getVertex(int index){
        return graph.get(index);
    }

    public int getLatestVertexIndex(){
        if(graph.isEmpty()) return 0;
        return graph.indexOf(graph.getLast())+1;
    }

    public String toString(){
        if(graph.isEmpty()) return "Graph is empty";
        StringBuilder sb = new StringBuilder();
        for(Vertex vertex : graph){
            sb.append(vertex.toString()).append("\n");
        }
        return sb.toString();
    }

}
