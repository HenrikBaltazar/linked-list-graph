package org.example;

public enum GraphType {
    UNDIRECTED("Não Dirigido"),
    DIRECTED("Dirigido");

    private final String description;

    GraphType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }
}
