package org.example.ui;

import javax.swing.*;
import java.awt.*;

public class Interface extends JFrame {
    private static final int W = 1280, H = 720;
    public int screenSize = getToolkit().getScreenResolution()/2;
    private Toolbar toolbar;
    private GraphPanel graphPanel;
    private MenuBar menuBar;

    public Interface() {
        graphPanel = new GraphPanel();
        toolbar = new Toolbar(this);
        menuBar = new MenuBar(this);
        setMinimumSize(new Dimension(W, H));
        setTitle("Grafo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setJMenuBar(menuBar);
        getContentPane().add(toolbar, BorderLayout.PAGE_START);
        getContentPane().add(graphPanel, BorderLayout.CENTER);
    }

    public GraphPanel getGraphPanel() { return graphPanel; }

    public MenuBar getUIMenuBar(){ return menuBar; }

}