package org.example.graph;

import java.awt.*;

public class Arc extends Connection {

    public Arc(String id, Vertex source, Vertex target, double weight) {
        super(id, source, target, weight);
    }

    @Override
    public boolean isDirected() {
        return true;
    }

    @Override
    public boolean connects(Vertex v1, Vertex v2) {
        return source.equals(v1) && target.equals(v2);
    }

    @Override
    public Vertex getOtherVertex(Vertex vertex) {
        if (vertex.equals(source)) {
            return target;
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
            g2d.setColor(Color.DARK_GRAY);
        }

        g2d.drawLine(source.getX(), source.getY(), target.getX(), target.getY());
        drawArrowHead(g2d);
        drawWeightAndId(g2d);
    }

    private void drawArrowHead(Graphics2D g2d) {
        double dx = target.getX() - source.getX();
        double dy = target.getY() - source.getY();
        double length = Math.sqrt(dx * dx + dy * dy);

        if (length > 0) {
            dx /= length;
            dy /= length;

            double arrowLength = 15;
            double arrowWidth = 8;

            double endX = target.getX() - dx * 25;
            double endY = target.getY() - dy * 25;

            double arrowX1 = endX - dx * arrowLength - dy * arrowWidth;
            double arrowY1 = endY - dy * arrowLength + dx * arrowWidth;
            double arrowX2 = endX - dx * arrowLength + dy * arrowWidth;
            double arrowY2 = endY - dy * arrowLength - dx * arrowWidth;

            g2d.drawLine((int)endX, (int)endY, (int)arrowX1, (int)arrowY1);
            g2d.drawLine((int)endX, (int)endY, (int)arrowX2, (int)arrowY2);
        }
    }
}
