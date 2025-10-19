package org.example.graph;

import java.awt.*;

public class Edge extends Connection {

    public Edge(String id, Vertex source, Vertex target, double weight) {
        super(id, source, target, weight);
    }

    @Override
    public boolean isDirected() {
        return false;
    }

    @Override
    public boolean connects(Vertex v1, Vertex v2) {
        return (source.equals(v1) && target.equals(v2)) || (source.equals(v2) && target.equals(v1));
    }

    @Override
    public Vertex getOtherVertex(Vertex vertex) {
        if (vertex.equals(source)) {
            return target;
        } else if (vertex.equals(target)) {
            return source;
        }
        return null;
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(2));

        if (selected) {
            g2d.setColor(Color.RED);
        } else {
            g2d.setColor(Color.BLACK);
        }

        g2d.drawLine(source.getX(), source.getY(), target.getX(), target.getY());
        drawWeightAndId(g2d);
    }
}