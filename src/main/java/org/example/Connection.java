package org.example;

import java.awt.*;

public abstract class Connection {
    protected String id;
    protected double weight;
    protected Vertex source;
    protected Vertex target;
    protected boolean selected = false;

    public Connection(String id, Vertex source, Vertex target, double weight) {
        this.id = id;
        this.source = source;
        this.target = target;
        this.weight = weight;
    }

    // Getters
    public String getId() { return id; }
    public double getWeight() { return weight; }
    public Vertex getSource() { return source; }
    public Vertex getTarget() { return target; }
    public boolean isSelected() { return selected; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setWeight(double weight) { this.weight = weight; }
    public void setSelected(boolean selected) { this.selected = selected; }

    /**
     * Retorna as extremidades da conexão
     * @return Vértice com fonte e destino
     */
    public Vertex[] getEndpoints() {
        return new Vertex[]{source, target};
    }

    // Métodos abstratos que serão implementados pelas subclasses
    public abstract boolean connects(Vertex v1, Vertex v2);
    public abstract Vertex getOtherVertex(Vertex vertex);
    public abstract void draw(Graphics g);
    public abstract boolean isDirected();

    protected void drawWeightAndId(Graphics2D g2d) {
        // Posição no meio da conexão
        int midX = (source.getX() + target.getX()) / 2;
        int midY = (source.getY() + target.getY()) / 2;

        g2d.setFont(new Font("Arial", Font.BOLD, 15));
        FontMetrics fm = g2d.getFontMetrics();

        String combinedText = id + "(" + weight + ")";
        int textWidth = fm.stringWidth(combinedText);
        int textHeight = fm.getHeight();

        int paddingX = 6;
        int paddingY = 4;

        // Fundo branco para o texto
        g2d.setColor(Color.WHITE);
        g2d.fillRect(midX - textWidth/2 - paddingX, midY - textHeight/2 - paddingY,textWidth + (paddingX * 2), textHeight + (paddingY * 2));
        g2d.setColor(Color.BLACK);
        g2d.drawRect(midX - textWidth/2 - paddingX, midY - textHeight/2 - paddingY,textWidth + (paddingX * 2), textHeight + (paddingY * 2));

        // ID em azul escuro
        g2d.setColor(new Color(0, 0, 150));
        g2d.drawString(combinedText, midX - textWidth/2, midY + textHeight/4);
    }
}