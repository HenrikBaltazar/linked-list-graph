package org.example.graph;

import java.util.ArrayList;
import java.util.List;

public class Vertex {
    private List<Vertex> neighbours;
    private int index;
    public Vertex() {
        neighbours = new ArrayList<>();
    }

    public List<Vertex> getNeighbours() {
        return neighbours;
    }

    public void addNeighbour(Vertex neighbour) {
        neighbours.add(neighbour);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String toString() {
        if(neighbours.isEmpty()) return index + "";
        StringBuilder sb = new StringBuilder();
        sb.append(index).append(" -> ");
        for(Vertex v : neighbours){
            sb.append(v.getIndex()).append(" ");
        }
        return sb.toString();
    }
}
