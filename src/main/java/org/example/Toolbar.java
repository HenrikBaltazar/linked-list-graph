package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Toolbar extends JToolBar {
    private static final String resourcesPath = "src/main/resources/";
    private static final ImageIcon ICON_ADD = new ImageIcon(resourcesPath + "add.png");
    private static final ImageIcon ICON_ADD_SELECTED = new ImageIcon(resourcesPath + "selected_add.png");
    private static final ImageIcon ICON_REMOVE = new ImageIcon(resourcesPath + "remove.png");
    private static final ImageIcon ICON_REMOVE_SELECTED = new ImageIcon(resourcesPath + "selected_remove.png");
    private static final ImageIcon ICON_CONNECT = new ImageIcon(resourcesPath + "connect.png");
    private static final ImageIcon ICON_CONNECT_SELECTED = new ImageIcon(resourcesPath + "selected_connect.png");
    private static final ImageIcon ICON_DISCONNECT = new ImageIcon(resourcesPath + "disconnect.png");
    private static final ImageIcon ICON_DISCONNECT_SELECTED = new ImageIcon(resourcesPath + "selected_disconnect.png");
    private static final ImageIcon ICON_ORIENTATION = new ImageIcon(resourcesPath + "orientation.png");
    private static final ImageIcon ICON_ORIENTATION_SELECTED = new ImageIcon(resourcesPath + "selected_orientation.png");
    private static final ImageIcon ICON_ADJMATRIX = new ImageIcon(resourcesPath + "a.png");
    private static final ImageIcon ICON_INCMATRIX = new ImageIcon(resourcesPath + "i.png");

    private JToggleButton addButton, removeButton, connectButton, disconnectButton, orientationButton;
    private JButton adjMatrixButton, incMatrixButton;
    private Interface ui;

    public Toolbar(Interface ui) {
        this.ui = ui;

        setVisible(true);
        setBackground(new Color(159, 197, 232));

        addButton = new JToggleButton(ICON_ADD);
        removeButton = new JToggleButton(ICON_REMOVE);
        connectButton = new JToggleButton(ICON_CONNECT);
        disconnectButton = new JToggleButton(ICON_DISCONNECT);
        orientationButton = new JToggleButton(ICON_ORIENTATION);
        adjMatrixButton = new JButton(ICON_ADJMATRIX);
        incMatrixButton = new JButton(ICON_INCMATRIX);

        addButton.setSelectedIcon(ICON_ADD_SELECTED);
        removeButton.setSelectedIcon(ICON_REMOVE_SELECTED);
        connectButton.setSelectedIcon(ICON_CONNECT_SELECTED);
        disconnectButton.setSelectedIcon(ICON_DISCONNECT_SELECTED);
        orientationButton.setSelectedIcon(ICON_ORIENTATION_SELECTED);

        addButton.setToolTipText("Adicionar vértice");
        removeButton.setToolTipText("Remover vértice (Modo Seleção)");
        connectButton.setToolTipText("Conectar vértices");
        disconnectButton.setToolTipText("Desconectar vértices");
        orientationButton.setToolTipText("Mudar grafo orientado e não-orientado ");
        adjMatrixButton.setToolTipText("Gerar Matriz de Adjacencia");
        incMatrixButton.setToolTipText("Incluir Matriz de Incidencia");

        configureToggleAppearance(addButton);
        configureToggleAppearance(removeButton);
        configureToggleAppearance(connectButton);
        configureToggleAppearance(disconnectButton);
        configureToggleAppearance(orientationButton);
        configureButtonAppearance(adjMatrixButton);
        configureButtonAppearance(incMatrixButton);

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
        orientationButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                orientationButtonAction();
            }
        });

        adjMatrixButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                adjMatrixButtonAction();
            }
        });

        incMatrixButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                incMatrixButtonAction();
            }
        });

        MouseAdapter mouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toolModeGroup.clearSelection();
            }
        };
        addMouseListener(mouseListener);

        add(addButton);
        add(removeButton);
        add(connectButton);
        add(disconnectButton);
        add(orientationButton);
        add(adjMatrixButton);
        add(incMatrixButton);

        orientationButton.setSelected(true);
        orientationButtonAction();
    }


    private void configureButtonAppearance(JButton button) {
        button.setBackground(new Color(159, 197, 232));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
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
        ui.getGraphPanel().switchIsDirected();
    }

    public void adjMatrixButtonAction() {
        int[][] matriz = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };

        matrixFrame(matriz, "Matriz 3x3");
    }

    public void incMatrixButtonAction() {
        int[][] matriz = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };

        matrixFrame(matriz, "Matriz 3x3");
    }

    public void matrixFrame(int[][] matrix, String title){
        int n = matrix.length;
        int m = matrix[0].length;

        Object[][] data = new Object[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                data[i][j] = matrix[i][j];
            }
        }

        String[] colNames = new String[m];
        for (int j = 0; j < m; j++) {
            colNames[j] = "C" + (j + 1);
        }

        JTable table = new JTable(data, colNames);
        table.setEnabled(false);
        table.setRowHeight(30);

        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(new JScrollPane(table));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

}