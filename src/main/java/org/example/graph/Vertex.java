package org.example.graph;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Vertex {
    private List<Vertex> neighbours;
    private String id;

    private int x,y;
    private boolean selected = false;
    public int radiusw = 25, radiush=25;

    public Vertex(String id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.neighbours = new ArrayList<>();
    }

    public int getRadiusw(){
        return radiusw;
    }

    public void setRadiusw(int radiusw){
        this.radiusw = radiusw;
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
        int radiusSquared = radiusw * radiusw;
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
        if (id != null && id.length() > 1) {
            radiusw =radiusw +id.length()*10;
            radiush*=2;
            g2d.fillRoundRect(
                    x - radiusw,
                    y - radiusw,
                    radiusw,
                    radiush,
                    15,
                    15
            );
        } else {
            g2d.fillOval(x - radiusw, y - radiusw, radiusw * 2, radiush * 2);
        }
        g2d.setColor(Color.WHITE);
        if (id == null) id = "?";
        if(id.length() > 1 && id.length()<3){
            g2d.drawString(id, x - 15, y + 5);
        }else if(id.length() >= 3){
            g2d.drawString(id, x - 20, y + 5);
        }else {
            g2d.drawString(id, x - 4, y + 5);
        }
    }

}