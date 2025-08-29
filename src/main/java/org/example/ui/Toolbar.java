package org.example.ui;

import org.example.graph.Vertex;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Toolbar extends JToolBar {
    private static final String resourcesPath = "src/main/resources/";
    private static final ImageIcon ICON_ADD = new ImageIcon(resourcesPath+"add.png");
    private static final ImageIcon ICON_REMOVE = new ImageIcon(resourcesPath+"remove.png");
    private static final ImageIcon ICON_CONNECT = new ImageIcon(resourcesPath+"connect.png");
    private static final ImageIcon ICON_DISCONNECT = new ImageIcon(resourcesPath+"disconnect.png");
    private static JButton addButton = new JButton(ICON_ADD), removeButton = new JButton(ICON_REMOVE), connectButton = new JButton(ICON_CONNECT), disconnectButton = new JButton(ICON_DISCONNECT);
    public Toolbar(Interface ui){
        setVisible(true);
        setBackground(new Color(159,197,232));

        addButton.setToolTipText("Adicionar vértice");
        removeButton.setToolTipText("Remover vértice");
        connectButton.setToolTipText("Conectar vértices");
        disconnectButton.setToolTipText("Disconectar vértices");

        addButton.setBackground(new Color(159,197,232));
        removeButton.setBackground(new Color(159,197,232));
        connectButton.setBackground(new Color(159,197,232));
        disconnectButton.setBackground(new Color(159,197,232));

        addButton.setBorderPainted(false);
        removeButton.setBorderPainted(false);
        connectButton.setBorderPainted(false);
        disconnectButton.setBorderPainted(false);

        addButton.setFocusPainted(false);
        removeButton.setFocusPainted(false);
        connectButton.setFocusPainted(false);
        disconnectButton.setFocusPainted(false);
        
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addButtonAction(ui);
            }
        });
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){removeButtonAction(ui);}
        });
        connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                connectButtonAction(ui);
            }
        });
        disconnectButton.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               disconnectButtonAction(ui);
           }
       });

        add(addButton);
        add(removeButton);
        add(connectButton);
        add(disconnectButton);

   }
   
   public void addButtonAction(Interface ui){
       ui.getGraph().addVertex(new Vertex());
       Node node = new Node(ui,ui.getGraph().getLatestVertexIndex());
       ui.addNode(node);
   }
   
   public void removeButtonAction(Interface ui){
        ui.getGraph().removeVertex(ui.getSelectedNode);
   }
   
   public void connectButtonAction(Interface ui){
       
   }
   
   public void disconnectButtonAction(Interface ui){
       
   }
}
