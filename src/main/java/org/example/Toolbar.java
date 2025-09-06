package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Toolbar extends JToolBar {
    private static final String resourcesPath = "src/main/resources/";
    private static final ImageIcon ICON_ADD = new ImageIcon(resourcesPath + "add.png");
    private static final ImageIcon ICON_REMOVE = new ImageIcon(resourcesPath + "remove.png");
    private static final ImageIcon ICON_CONNECT = new ImageIcon(resourcesPath + "connect.png");
    private static final ImageIcon ICON_DISCONNECT = new ImageIcon(resourcesPath + "disconnect.png");

    private JToggleButton addButton, removeButton, connectButton, disconnectButton;

    private Interface ui;

    public Toolbar(Interface ui) {
        this.ui = ui;

        setVisible(true);
        setBackground(new Color(159, 197, 232));

        addButton = new JToggleButton(ICON_ADD);
        removeButton = new JToggleButton(ICON_REMOVE);
        connectButton = new JToggleButton(ICON_CONNECT);
        disconnectButton = new JToggleButton(ICON_DISCONNECT);

        addButton.setToolTipText("Adicionar vértice");
        removeButton.setToolTipText("Remover vértice (Modo Seleção)");
        connectButton.setToolTipText("Conectar vértices");
        disconnectButton.setToolTipText("Desconectar vértices");

        configureButtonAppearance(addButton);
        configureButtonAppearance(removeButton);
        configureButtonAppearance(connectButton);
        configureButtonAppearance(disconnectButton);

        ButtonGroup toolModeGroup = new ButtonGroup();
        toolModeGroup.add(addButton);
        toolModeGroup.add(removeButton);
        toolModeGroup.add(connectButton);
        toolModeGroup.add(disconnectButton);


        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addButtonAction();
            }
        });
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeButtonAction();
            }
        });
        connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                connectButtonAction();
            }
        });
        disconnectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                disconnectButtonAction();
            }
        });

        add(addButton);
        add(removeButton);
        add(connectButton);
        add(disconnectButton);

        addButton.setSelected(true);
        addButtonAction();
    }


    private void configureButtonAppearance(JToggleButton button) {
        button.setBackground(new Color(159, 197, 232));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        // button.setSelectedIcon(...); // Opcional: ícone diferente quando selecionado
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
}