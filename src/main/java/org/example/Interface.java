package org.example;

import javax.swing.*;
import java.awt.*;

public class Interface extends JFrame {
    private static final int W = 900, H = 600;
    public int screenSize = getToolkit().getScreenResolution()/2;
    private Toolbar toolbar;
    private GraphPanel graphPanel;

    public Interface() {
        graphPanel = new GraphPanel();
        toolbar = new Toolbar(this);
        setMinimumSize(new Dimension(W, H));
        setTitle("Grafo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        getContentPane().add(toolbar, BorderLayout.PAGE_START);
        getContentPane().add(graphPanel, BorderLayout.CENTER);
    }

    public GraphPanel getGraphPanel() { return graphPanel; }
}