package org.example;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Vertex {
    private List<Vertex> neighbours;
    private String id;

    private int x,y;
    private boolean selected = false;
    public static final int NODE_RADIUS = 25;

    public Vertex(String id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.neighbours = new ArrayList<>();
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public List<Vertex> getNeighbours() { return neighbours; }

    public void setNeighbours(List<Vertex> neighbours) {this.neighbours = neighbours;}

    public void addNeighbour(Vertex neighbour) { neighbours.add(neighbour); }

    public int getX() { return x; }

    public int getY() { return y; }

    public void setPosition(int x, int y) { this.x = x; this.y = y; }

    public boolean isSelected() { return selected; }

    public void setSelected(boolean selected) { this.selected = selected; }

    public boolean contains(Point p) {
        int radiusSquared = NODE_RADIUS * NODE_RADIUS;
        int distanceSquared = (p.x - x) * (p.x - x) + (p.y - y) * (p.y - y);
        return distanceSquared <= radiusSquared;
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 20));
        if (selected) {
            g2d.setColor(Color.RED);
        } else {
            g2d.setColor(Color.BLUE);
        }
        g2d.fillOval(x - NODE_RADIUS, y - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);

        g2d.setColor(Color.WHITE);
        String label = getId();
        if (label == null) label = "?";
        if(label.length() > 9){
            g2d.drawString(label, x - 8, y + 5);
        }else {
            g2d.drawString(label, x - 4, y + 5);
        }
    }

}