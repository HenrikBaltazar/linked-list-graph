package org.example;

import java.awt.*;

public class Edge {
    private String id;
    private double weight;
    private Vertex source;
    private Vertex target;
    private boolean selected = false;

    public Edge(String id, Vertex source, Vertex target, double weight) {
        this.id = id;
        this.source = source;
        this.target = target;
        this.weight = weight;
    }

    public String getId() { return id; }
    public double getWeight() { return weight; }
    public Vertex getSource() { return source; }
    public Vertex getTarget() { return target; }
    public boolean isSelected() { return selected; }

    public void setId(String id) { this.id = id; }
    public void setWeight(double weight) { this.weight = weight; }
    public void setSelected(boolean selected) { this.selected = selected; }

    // Retorna as extremidades da aresta
    public Vertex[] getEndpoints() {
        return new Vertex[]{source, target};
    }

    // Verifica se a aresta conecta dois vértices específicos
    public boolean connects(Vertex v1, Vertex v2) {
        return (source.equals(v1) && target.equals(v2)) ||
                (source.equals(v2) && target.equals(v1));
    }

    // Retorna o outro vértice da aresta dado um vértice
    public Vertex getOtherVertex(Vertex vertex) {
        if (vertex.equals(source)) {
            return target;
        } else if (vertex.equals(target)) {
            return source;
        }
        return null;
    }

    // Desenha a aresta
    public void draw(Graphics g, boolean isDirected) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(2));

        if (selected) {
            g2d.setColor(Color.RED);
        } else {
            g2d.setColor(Color.BLACK);
        }

        g2d.drawLine(source.getX(), source.getY(), target.getX(), target.getY());

        if (isDirected) {
            drawArrowHead(g2d);
        }

        // Desenha o peso no meio da aresta
        drawWeight(g2d);
    }

    private void drawArrowHead(Graphics2D g2d) {
        double dx = target.getX() - source.getX();
        double dy = target.getY() - source.getY();
        double length = Math.sqrt(dx * dx + dy * dy);

        if (length > 0) {
            // Normaliza o vetor direção
            dx /= length;
            dy /= length;

            // Calcula a posição da ponta da seta (próximo ao vértice target)
            double arrowLength = 15;
            double arrowWidth = 8;

            // Posição onde a seta deve terminar (borda do círculo do vértice)
            double endX = target.getX() - dx * 25; // 25 é o raio do vértice
            double endY = target.getY() - dy * 25;

            // Calcula os pontos da seta
            double arrowX1 = endX - dx * arrowLength - dy * arrowWidth;
            double arrowY1 = endY - dy * arrowLength + dx * arrowWidth;
            double arrowX2 = endX - dx * arrowLength + dy * arrowWidth;
            double arrowY2 = endY - dy * arrowLength - dx * arrowWidth;

            // Desenha a seta
            g2d.drawLine((int)endX, (int)endY, (int)arrowX1, (int)arrowY1);
            g2d.drawLine((int)endX, (int)endY, (int)arrowX2, (int)arrowY2);
        }
    }

    private void drawWeight(Graphics2D g2d) {
        // Posição no meio da aresta
        int midX = (source.getX() + target.getX()) / 2;
        int midY = (source.getY() + target.getY()) / 2;

        g2d.setColor(Color.BLUE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));

        String weightText = String.valueOf(weight);
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(weightText);
        int textHeight = fm.getHeight();

        // Desenha um fundo branco para o texto
        g2d.setColor(Color.WHITE);
        g2d.fillRect(midX - textWidth/2 - 2, midY - textHeight/2, textWidth + 4, textHeight);

        // Desenha o texto
        g2d.setColor(Color.BLUE);
        g2d.drawString(weightText, midX - textWidth/2, midY + textHeight/4);
    }
}