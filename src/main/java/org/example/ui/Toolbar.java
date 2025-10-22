package org.example.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.InputStream;

public class Toolbar extends JToolBar {
    private static final String ICON_ADD = "/add.png";
    private static final String ICON_ADD_SELECTED = "/selected_add.png";
    private static final String ICON_REMOVE = "/remove.png";
    private static final String ICON_REMOVE_SELECTED = "/selected_remove.png";
    private static final String ICON_CONNECT = "/connect.png";
    private static final String ICON_CONNECT_SELECTED = "/selected_connect.png";
    private static final String ICON_DISCONNECT = "/disconnect.png";
    private static final String ICON_DISCONNECT_SELECTED = "/selected_disconnect.png";
    private static final String ICON_ORIENTATION = "/orientation.png";
    private static final String ICON_ORIENTATION_SELECTED = "/selected_orientation.png";
    private Font FONT_BUTTON;

    private final JToggleButton addButton, removeButton, connectButton, disconnectButton, orientationButton;
    private final Interface ui;

    public Toolbar(Interface ui) {
        this.ui = ui;
        FONT_BUTTON = new Font("Monospaced", Font.BOLD,ui.screenSize/2);
        setVisible(true);
        setBackground(new Color(159, 197, 232));

        addButton = new JToggleButton(loadIcon(ICON_ADD));
        removeButton = new JToggleButton(loadIcon(ICON_REMOVE));
        connectButton = new JToggleButton(loadIcon(ICON_CONNECT));
        disconnectButton = new JToggleButton(loadIcon(ICON_DISCONNECT));
        orientationButton = new JToggleButton(loadIcon(ICON_ORIENTATION));

        addButton.setSelectedIcon(loadIcon(ICON_ADD_SELECTED));
        removeButton.setSelectedIcon(loadIcon(ICON_REMOVE_SELECTED));
        connectButton.setSelectedIcon(loadIcon(ICON_CONNECT_SELECTED));
        disconnectButton.setSelectedIcon(loadIcon(ICON_DISCONNECT_SELECTED));
        orientationButton.setSelectedIcon(loadIcon(ICON_ORIENTATION_SELECTED));

        addButton.setToolTipText("Adicionar vértice");
        removeButton.setToolTipText("Remover vértice (Modo Seleção)");
        connectButton.setToolTipText("Conectar vértices (Criar arestas)");
        disconnectButton.setToolTipText("Desconectar vértices (Remover arestas)");
        orientationButton.setToolTipText("GRAFO NÃO DIRIGIDO");


        configureToggleAppearance(addButton);
        configureToggleAppearance(removeButton);
        configureToggleAppearance(connectButton);
        configureToggleAppearance(disconnectButton);
        configureToggleAppearance(orientationButton);

        ButtonGroup toolModeGroup = new ButtonGroup();
        toolModeGroup.add(addButton);
        toolModeGroup.add(removeButton);
        toolModeGroup.add(connectButton);
        toolModeGroup.add(disconnectButton);

        setupActionListeners();

        add(addButton);
        add(removeButton);
        add(connectButton);
        add(disconnectButton);
        add(orientationButton);

        addButton.setSelected(true);
        addButtonAction();
    }

    private void setupActionListeners() {
        addButton.addActionListener(_ -> addButtonAction());

        removeButton.addActionListener(_ -> removeButtonAction());

        connectButton.addActionListener(_ -> connectButtonAction());

        disconnectButton.addActionListener(_ -> disconnectButtonAction());

        orientationButton.addActionListener(_ -> orientationButtonAction());
    }

    private void configureToggleAppearance(JToggleButton button) {
        button.setBackground(new Color(159, 197, 232));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
    }

    public void addButtonAction() {
        ui.getGraphPanel().setToolMode(GraphPanel.ToolMode.ADD_NODE);
    }

    public void removeButtonAction() {
        ui.getGraphPanel().setToolMode(GraphPanel.ToolMode.REMOVE);
    }

    public void connectButtonAction() {
        ui.getGraphPanel().setToolMode(GraphPanel.ToolMode.CONNECT);
    }

    public void disconnectButtonAction() {
        ui.getGraphPanel().setToolMode(GraphPanel.ToolMode.DISCONNECT);
    }

    public void orientationButtonAction() {
        GraphPanel graphPanel = ui.getGraphPanel();
        graphPanel.clearAllAlgorithmVisualizations();
        if (graphPanel.getGraphType() == GraphPanel.GraphType.UNDIRECTED) {
                graphPanel.setGraphType(GraphPanel.GraphType.DIRECTED);
                ui.getUIMenuBar().setUndirectedFeatures(false);
        } else {
                graphPanel.setGraphType(GraphPanel.GraphType.UNDIRECTED);
                ui.getUIMenuBar().setUndirectedFeatures(true);
        }

        orientationButton.setSelected(graphPanel.getGraphType() == GraphPanel.GraphType.DIRECTED);

        updateTooltipsForGraphType();

        System.out.println("Grafo alterado para: " + graphPanel.getGraphTypeDescription());
    }



    private void updateTooltipsForGraphType() {
        String connectionType = ui.getGraphPanel().getGraphType() == GraphPanel.GraphType.DIRECTED ? "arcos" : "arestas";

        connectButton.setToolTipText("Conectar vértices (Criar " + connectionType + ")");
        disconnectButton.setToolTipText("Desconectar vértices (Remover " + connectionType + ")");
        orientationButton.setToolTipText(ui.getGraphPanel().getGraphType() == GraphPanel.GraphType.DIRECTED ? "GRAFO DIRIGIDO" : "GRAFO NÃO DIRIGIDO");
    }


    private ImageIcon loadIcon(String path) {
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            if (stream == null) {
                System.err.println("Erro: Não foi possível encontrar o recurso: " + path);
                return null;
            }
            return new ImageIcon(ImageIO.read(stream).getScaledInstance(ui.screenSize, ui.screenSize, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            System.err.println("Erro ao carregar imagem " + path + ": " + e.getMessage());
            return null;
        }
    }

}