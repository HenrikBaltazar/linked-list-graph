package org.example;

import org.example.graph.Vertex;
import org.example.graph.Connection;
import org.example.ui.GraphPanel;
import org.example.ui.Interface;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.util.List;

public class FileManager {
    private Interface ui;
    
    public FileManager(Interface ui) {
        this.ui = ui;
    }
    
    public void saveFile() {
        GraphPanel graphPanel = ui.getGraphPanel();
        List<Vertex> vertices = graphPanel.getVertexList();
        List<Connection> connections = graphPanel.getConnectionList();
        
        if (vertices.isEmpty()) {
            JOptionPane.showMessageDialog(
                ui,
                "Não há grafo para salvar!",
                "Salvar Grafo",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar Grafo");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Graph Files (*.graph)", "graph"));
        
        int userSelection = fileChooser.showSaveDialog(ui);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            
            // Add .graph extension if not present
            if (!fileToSave.getName().endsWith(".graph")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".graph");
            }
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
                // Write graph type
                writer.write("GRAPH_TYPE:" + graphPanel.getGraphType().name());
                writer.newLine();
                
                // Write vertices
                writer.write("VERTICES:" + vertices.size());
                writer.newLine();
                for (Vertex vertex : vertices) {
                    writer.write(String.format("V:%s:%d:%d", 
                        vertex.getId(), 
                        vertex.getX(), 
                        vertex.getY()
                    ));
                    writer.newLine();
                }
                
                // Write connections
                writer.write("CONNECTIONS:" + connections.size());
                writer.newLine();
                for (Connection connection : connections) {
                    writer.write(String.format("C:%s:%s:%s:%.2f:%s", 
                        connection.getId(),
                        connection.getSource().getId(),
                        connection.getTarget().getId(),
                        connection.getWeight(),
                        connection.isDirected() ? "DIRECTED" : "UNDIRECTED"
                    ));
                    writer.newLine();
                }
                
                JOptionPane.showMessageDialog(
                    ui,
                    "Grafo salvo com sucesso em:\n" + fileToSave.getAbsolutePath(),
                    "Salvar Grafo",
                    JOptionPane.INFORMATION_MESSAGE
                );
                
            } catch (IOException e) {
                JOptionPane.showMessageDialog(
                    ui,
                    "Erro ao salvar arquivo:\n" + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
                );
                e.printStackTrace();
            }
        }
    }
    
    public void loadFile() {
        GraphPanel graphPanel = ui.getGraphPanel();
        
        // Check if current graph is not empty
        if (!graphPanel.getVertexList().isEmpty()) {
            int confirmation = JOptionPane.showConfirmDialog(
                ui,
                "O grafo atual será substituído. Deseja continuar?",
                "Abrir Grafo",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (confirmation != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Abrir Grafo");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Graph Files (*.graph)", "graph"));
        
        int userSelection = fileChooser.showOpenDialog(ui);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToOpen = fileChooser.getSelectedFile();
            
            try (BufferedReader reader = new BufferedReader(new FileReader(fileToOpen))) {
                // Clear current graph
                graphPanel.clearGraph();
                
                String line;
                GraphPanel.GraphType graphType = GraphPanel.GraphType.UNDIRECTED;
                
                // Read graph type
                line = reader.readLine();
                if (line != null && line.startsWith("GRAPH_TYPE:")) {
                    String typeStr = line.substring("GRAPH_TYPE:".length());
                    graphType = GraphPanel.GraphType.valueOf(typeStr);
                    graphPanel.setGraphType(graphType);
                }
                
                // Read vertices count
                line = reader.readLine();
                if (line != null && line.startsWith("VERTICES:")) {
                    int vertexCount = Integer.parseInt(line.substring("VERTICES:".length()));
                    
                    // Read each vertex
                    for (int i = 0; i < vertexCount; i++) {
                        line = reader.readLine();
                        if (line != null && line.startsWith("V:")) {
                            String[] parts = line.substring(2).split(":");
                            if (parts.length == 3) {
                                String id = parts[0];
                                int x = Integer.parseInt(parts[1]);
                                int y = Integer.parseInt(parts[2]);
                                
                                // Create vertex directly without dialog
                                Vertex vertex = new Vertex(id, x, y);
                                graphPanel.getVertexList().add(vertex);
                            }
                        }
                    }
                }
                
                // Read connections count
                line = reader.readLine();
                if (line != null && line.startsWith("CONNECTIONS:")) {
                    int connectionCount = Integer.parseInt(line.substring("CONNECTIONS:".length()));
                    
                    // Read each connection
                    for (int i = 0; i < connectionCount; i++) {
                        line = reader.readLine();
                        if (line != null && line.startsWith("C:")) {
                            String[] parts = line.substring(2).split(":");
                            if (parts.length == 5) {
                                String connectionId = parts[0];
                                String sourceId = parts[1];
                                String targetId = parts[2];
                                double weight = Double.parseDouble(parts[3]);
                                
                                // Find source and target vertices
                                Vertex source = findVertexById(graphPanel, sourceId);
                                Vertex target = findVertexById(graphPanel, targetId);
                                
                                if (source != null && target != null) {
                                    graphPanel.addConnection(connectionId, source, target, weight);
                                }
                            }
                        }
                    }
                }
                
                // Repaint the panel
                graphPanel.repaint();
                
                JOptionPane.showMessageDialog(
                    ui,
                    "Grafo carregado com sucesso!",
                    "Abrir Grafo",
                    JOptionPane.INFORMATION_MESSAGE
                );
                
            } catch (IOException e) {
                JOptionPane.showMessageDialog(
                    ui,
                    "Erro ao abrir arquivo:\n" + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
                );
                e.printStackTrace();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                    ui,
                    "Erro ao processar arquivo:\n" + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
                );
                e.printStackTrace();
            }
        }
    }
    
    private Vertex findVertexById(GraphPanel graphPanel, String id) {
        for (Vertex vertex : graphPanel.getVertexList()) {
            if (vertex.getId().equals(id)) {
                return vertex;
            }
        }
        return null;
    }
}
