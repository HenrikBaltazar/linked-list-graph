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

    private Double latitude = null;
    private Double longitude = null;

    public Vertex(String id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.neighbours = new ArrayList<>();
    }

    public Vertex(String id, int x, int y, double latitude, double longitude) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.latitude = latitude;
        this.longitude = longitude;
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
        if (id != null && id.length() > 1) {
            // For rounded rectangles, use approximate text-based bounds
            int textWidth = id.length() * 12; // Approximate width per character
            int padding = 15;
            int drawRadiusW = (textWidth / 2) + padding;
            int drawRadiusH = 20 + padding;
            
            // Check if point is within rectangle bounds
            return p.x >= x - drawRadiusW && p.x <= x + drawRadiusW &&
                   p.y >= y - drawRadiusH / 2 && p.y <= y + drawRadiusH / 2;
        } else {
            // For circles, use radius-based detection
            int radiusSquared = radiusw * radiusw;
            int distanceSquared = (p.x - x) * (p.x - x) + (p.y - y) * (p.y - y);
            return distanceSquared <= radiusSquared;
        }
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 20));
        
        // Get font metrics to measure text width
        FontMetrics fm = g2d.getFontMetrics();
        String displayId = (id == null) ? "?" : id;
        int textWidth = fm.stringWidth(displayId);
        int textHeight = fm.getAscent();
        
        // Calculate drawing dimensions based on text size
        int drawRadiusW;
        int drawRadiusH;
        int textX;
        int textY = y + textHeight / 3; // Vertical centering
        
        if (displayId.length() > 1) {
            // For multi-character names, use rounded rectangle
            int padding = 15; // Padding around text
            drawRadiusW = (textWidth / 2) + padding;
            drawRadiusH = textHeight + padding;
            
            // Set color
            if (selected) {
                g2d.setColor(Color.RED);
            } else {
                g2d.setColor(Color.BLUE);
            }
            
            // Draw rounded rectangle centered on (x, y)
            g2d.fillRoundRect(
                    x - drawRadiusW,
                    y - drawRadiusH / 2,
                    drawRadiusW * 2,
                    drawRadiusH,
                    15,
                    15
            );
            
            // Center text horizontally
            textX = x - textWidth / 2;
        } else {
            // For single character, use circle
            drawRadiusW = radiusw;
            drawRadiusH = radiush;
            
            // Set color
            if (selected) {
                g2d.setColor(Color.RED);
            } else {
                g2d.setColor(Color.BLUE);
            }
            
            g2d.fillOval(x - drawRadiusW, y - drawRadiusW, drawRadiusW * 2, drawRadiusH * 2);
            
            // Center text horizontally
            textX = x - textWidth / 2;
        }
        
        // Draw text in white
        g2d.setColor(Color.WHITE);
        g2d.drawString(displayId, textX, textY);
    }

    // Getters and setters --------------------

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public boolean hasGeographicCoordinates() {
        return latitude != null && longitude != null;
    }

}