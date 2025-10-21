package org.example.ui;

import org.example.FileManager;
import org.example.graph.Connection;
import org.example.graph.Vertex;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MenuBar extends JMenuBar {
    private Interface ui;
    private FileManager fileManager;
    private final JMenu jMenuFile, jMenuMatrix, jMenuAlg, jMenuSearch;
    private final JMenuItem jMenuItemOpen, jMenuItemSave, jMenuItemClear;
    private final JMenuItem jMenuItemAdjMatrix, jMenuItemIncMatrix;
    private final JMenuItem jMenuItemCheckAdjacency, jMenuItemPrim, jMenuItemBfs, jMenuItemDfs, jMenuItemConexo;
    private final JMenuItem jMenuItemPlanar, jMenuItemWP, jMenuItemAstar;
    public MenuBar(Interface ui) {
        this.ui = ui;
        fileManager = new FileManager();

        jMenuFile = new JMenu("Arquivo");
        jMenuFile.add(jMenuItemOpen = new JMenuItem("Abrir..."));
        jMenuFile.add(jMenuItemSave = new JMenuItem("Salvar"));
        jMenuFile.add(jMenuItemClear = new JMenuItem("Limpar"));
        add(jMenuFile);
        jMenuMatrix = new JMenu("Matrizes");
        jMenuMatrix.add(jMenuItemAdjMatrix = new JMenuItem("Matriz de adjacência"));
        jMenuMatrix.add(jMenuItemIncMatrix = new JMenuItem("Matriz de incidência"));
        add(jMenuMatrix);
        jMenuAlg = new JMenu("Algorítmos");
        jMenuAlg.add(jMenuItemCheckAdjacency = new JMenuItem("Verificar Adjacência"));
        jMenuAlg.add(jMenuItemPrim = new JMenuItem("PRIM (AGM)"));
        jMenuAlg.add(jMenuItemConexo = new JMenuItem("Componentes conexos"));
        jMenuAlg.add(jMenuItemPlanar = new JMenuItem("Planaridade"));
        jMenuAlg.add(jMenuItemWP = new JMenuItem("Welsh-Powell"));
        add(jMenuAlg);
        jMenuSearch = new JMenu("Busca");
        jMenuSearch.add(jMenuItemBfs = new JMenuItem("BFS"));
        jMenuSearch.add(jMenuItemDfs = new JMenuItem("DFS"));
        jMenuSearch.add(jMenuItemAstar = new JMenuItem("A*"));
        add(jMenuSearch);
        setupActionListeners();
    }

    private void setupActionListeners(){
        jMenuItemOpen.addActionListener(e -> fileManager.loadFile());

        jMenuItemSave.addActionListener(e -> fileManager.saveFile());

        jMenuItemClear.addActionListener(e -> clearGraph());

        jMenuItemAdjMatrix.addActionListener(_ -> adjMatrixButtonAction());

        jMenuItemIncMatrix.addActionListener(_ -> incMatrixButtonAction());

        jMenuItemCheckAdjacency.addActionListener(_ -> checkAdjacencyButtonAction());

        jMenuItemPrim.addActionListener(_ -> primButtonAction());

        jMenuItemBfs.addActionListener(_ -> bfsButtonAction());

        jMenuItemDfs.addActionListener(_ -> dfsButtonAction());

        jMenuItemConexo.addActionListener(_ -> conexoButtonAction());

        jMenuItemPlanar.addActionListener(_ -> planaridade());

        jMenuItemWP.addActionListener(_ -> welshPowell());

        jMenuItemAstar.addActionListener(_ -> aStar());
    }

    private void aStar(){
        GraphPanel graphPanel = ui.getGraphPanel();
        graphPanel.clearAllAlgorithmVisualizations();
    }

    private void welshPowell() {
        GraphPanel graphPanel = ui.getGraphPanel();
        graphPanel.clearAllAlgorithmVisualizations();
        graphPanel.applyWelshPowellColoring();
    }

    private void planaridade() {
        GraphPanel graphPanel = ui.getGraphPanel();
        graphPanel.clearAllAlgorithmVisualizations();

        int v = graphPanel.getVertexList().size();
        int e = graphPanel.getConnectionList().size();

        // Caso trivial: Grafos com menos de 3 vértices são sempre planares.
        if (v < 3) {
            JOptionPane.showMessageDialog(ui,
                    "O grafo é PLANAR. ✅\n\n(Grafos com menos de 3 vértices são sempre planares).",
                    "Verificação de Planaridade",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        boolean isPotentiallyPlanar = (e <= 3 * v - 6);

        String message;
        int messageType;

        if (isPotentiallyPlanar && graphPanel.hasThreeCycle()) {
            message = String.format(
                    "O grafo É PLANAR. ✅\n\n" +
                            "Detalhes da verificação:\n" +
                            " • Vértices (V): %d\n" +
                            " • Arestas (E): %d\n" +
                            " • Condição: E <= 3V - 6\n" +
                            " • Cálculo: %d <= 3 * %d - 6  =>  %d <= %d\n\n" +
                            "O grafo possui ciclos de comprimento 3 e a condição inicial foi satisfeita. ",
                    v, e, e, v, e, (3 * v - 6)
            );
            messageType = JOptionPane.INFORMATION_MESSAGE;
        } else if (isPotentiallyPlanar && !graphPanel.hasThreeCycle()) {
            isPotentiallyPlanar = (e <= 2 * v - 4);
            if (isPotentiallyPlanar) {
                message = String.format(
                        "O grafo É PLANAR. ✅\n\n" +
                                "Detalhes da verificação:\n" +
                                " • Vértices (V): %d\n" +
                                " • Arestas (E): %d\n" +
                                " • Condição: E <= 2V - 4\n" +
                                " • Cálculo: %d <= 2 * %d - 4  =>  %d <= %d\n\n" +
                                "O grafo NÃO possui ciclos de comprimento 3 mas a segunda condição foi satisfeita. ",
                        v, e, e, v, e, (3 * v - 6)
                );
                messageType = JOptionPane.INFORMATION_MESSAGE;
            }else {
                message = String.format(
                        "O grafo NÃO É PLANAR. ❌\n\n" +
                                "Detalhes da verificação:\n" +
                                " • Vértices (V): %d\n" +
                                " • Arestas (E): %d\n" +
                                " • Condição violada: E > 2V - 4\n" +
                                " • Cálculo: %d > 2 * %d - 4  =>  %d > %d\n\n" +
                                "O grafo possui arestas demais para ser desenhado em um plano sem cruzamentos.",
                        v, e, e, v, e, (3 * v - 6)
                );
                messageType = JOptionPane.WARNING_MESSAGE;
            }
        } else {
            message = String.format(
                    "O grafo NÃO É PLANAR. ❌\n\n" +
                            "Detalhes da verificação:\n" +
                            " • Vértices (V): %d\n" +
                            " • Arestas (E): %d\n" +
                            " • Condição violada: E > 3V - 6\n" +
                            " • Cálculo: %d > 3 * %d - 6  =>  %d > %d\n\n" +
                            "O grafo possui arestas demais para ser desenhado em um plano sem cruzamentos.",
                    v, e, e, v, e, (3 * v - 6)
            );
            messageType = JOptionPane.WARNING_MESSAGE;
        }

        JOptionPane.showMessageDialog(ui, message, "Verificação de Planaridade", messageType);
    }

    private void clearGraph(){
        if(ui.getGraphPanel().getVertexList().isEmpty()){
            JOptionPane.showMessageDialog(ui,
                    "O Grafo está vazio",
                    "Limpar grafo",
                    JOptionPane.INFORMATION_MESSAGE);
        }else {
            int confirmation = JOptionPane.showConfirmDialog(ui,
                    "O grafo atual será perdido e essa opção não poderá ser desfeita.\nDeseja prosseguir?",
                    "ATENÇÃO",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (confirmation == JOptionPane.YES_OPTION)
                ui.getGraphPanel().clearGraph();
        }
    }

    public void adjMatrixButtonAction() {
        GraphPanel graphPanel = ui.getGraphPanel();
        graphPanel.clearAllAlgorithmVisualizations();
        List<Vertex> vertices = graphPanel.getVertexList();

        if (vertices.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "O grafo não possui vértices!",
                    "Matriz de Adjacência",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int[][] matrix = graphPanel.getAdjacencyMatrix();
        String[] vertexLabels = vertices.stream()
                .map(Vertex::getId)
                .toArray(String[]::new);

        String title = "Matriz de Adjacência - " + graphPanel.getGraphTypeDescription();

        showMatrixFrame(matrix, vertexLabels, vertexLabels, title);
    }

    public void incMatrixButtonAction() {
        GraphPanel graphPanel = ui.getGraphPanel();
        graphPanel.clearAllAlgorithmVisualizations();
        List<Vertex> vertices = graphPanel.getVertexList();
        List<Connection> connections = graphPanel.getConnectionList();

        if (vertices.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "O grafo não possui vértices!",
                    "Matriz de Incidência",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (connections.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "O grafo não possui arestas/arcos!",
                    "Matriz de Incidência",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int[][] matrix = graphPanel.getIncidenceMatrix();
        String[] vertexLabels = vertices.stream()
                .map(Vertex::getId)
                .toArray(String[]::new);
        String[] connectionLabels = connections.stream()
                .map(Connection::getId)
                .toArray(String[]::new);

        String title = "Matriz de Incidência - " + graphPanel.getGraphTypeDescription();

        showMatrixFrame(matrix, vertexLabels, connectionLabels, title);
    }

    public void checkAdjacencyButtonAction() {
        ui.getGraphPanel().clearAllAlgorithmVisualizations();
        ui.getGraphPanel().checkVertexAdjacency();
    }

    public void primButtonAction() {
        GraphPanel graphPanel = ui.getGraphPanel();
        graphPanel.clearAllAlgorithmVisualizations();
        if (graphPanel.isMSTVisible()) {
            int option = JOptionPane.showConfirmDialog(
                    this,
                    "Uma AGM já está sendo exibida. Deseja calcular uma nova AGM?\n" +
                            "(Isso irá substituir a AGM atual)",
                    "AGM já exibida",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION) {
                return;
            } else if (option == JOptionPane.NO_OPTION) {
                graphPanel.clearMST();
                return;
            }
        }

        List<Connection> mst = graphPanel.applyPrimAlgorithm();

        if (mst != null && !mst.isEmpty()) {
            System.out.println("Algoritmo de Prim executado com sucesso!");
        }
    }

    public void bfsButtonAction() {
        GraphPanel graphPanel = ui.getGraphPanel();
        graphPanel.clearAllAlgorithmVisualizations();
        if (graphPanel.isBFSVisible()) {
            int option = JOptionPane.showConfirmDialog(
                    this,
                    "Uma árvore BFS já está sendo exibida. Deseja executar uma nova BFS?\n" +
                            "(Isso irá substituir a árvore BFS atual)",
                    "BFS já exibida",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION) {
                return;
            } else if (option == JOptionPane.NO_OPTION) {
                graphPanel.clearBFS();
                return;
            }
        }

        if (graphPanel.isMSTVisible()) {
            graphPanel.clearMST();
        }

        List<Connection> bfsTree = graphPanel.applyBreadthFirstSearch();

        if (bfsTree != null) {
            System.out.println("Busca em Largura executada com sucesso!");
        }
    }

    public void dfsButtonAction() {
        GraphPanel graphPanel = ui.getGraphPanel();
        graphPanel.clearAllAlgorithmVisualizations();
        if (graphPanel.isDFSVisible()) {
            int option = JOptionPane.showConfirmDialog(
                    this,
                    "Uma árvore DFS já está sendo exibida. Deseja executar uma nova DFS?\n" +
                            "(Isso irá substituir a árvore DFS atual)",
                    "DFS já exibida",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION) {
                return;
            } else if (option == JOptionPane.NO_OPTION) {
                graphPanel.clearDFS();
                return;
            }
        }

        if (graphPanel.isMSTVisible()) {
            graphPanel.clearMST();
        }
        if (graphPanel.isBFSVisible()) {
            graphPanel.clearBFS();
        }

        List<Connection> dfsTree = graphPanel.applyDepthFirstSearch();

        if (dfsTree != null) {
            System.out.println("Busca em Profundidade executada com sucesso!");
        }
    }

    private void conexoButtonAction() {
        GraphPanel graphPanel = ui.getGraphPanel();
        graphPanel.clearAllAlgorithmVisualizations();
        if (graphPanel.isComponentsVisible()) {
            int option = JOptionPane.showConfirmDialog(
                    this,
                    "A visualização de componentes já está ativa. Deseja executar uma nova análise?",
                    "Análise de Componentes",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION) {
                return;
            } else if (option == JOptionPane.NO_OPTION) {
                graphPanel.clearComponents();
                return;
            }
        }

        if (graphPanel.isMSTVisible()) {
            graphPanel.clearMST();
        }
        if (graphPanel.isBFSVisible()) {
            graphPanel.clearBFS();
        }
        if (graphPanel.isDFSVisible()) {
            graphPanel.clearDFS();
        }

        graphPanel.applyComponentAnalysis();
    }

    private void showMatrixFrame(int[][] matrix, String[] rowLabels, String[] colLabels, String title) {
        int n = matrix.length;
        int m = matrix[0].length;

        Object[][] data = new Object[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                data[i][j] = matrix[i][j];
            }
        }

        JTable table = new JTable(data, colLabels) {
            public String getRowName(int row) {
                return rowLabels[row];
            }
        };

        table.setEnabled(false);
        table.setRowHeight(30);

        JTable rowTable = new JTable(n, 1) {
            @Override
            public Object getValueAt(int row, int column) {
                return rowLabels[row];
            }
        };
        rowTable.setEnabled(false);
        rowTable.setPreferredScrollableViewportSize(new Dimension(50, 0));
        rowTable.setRowHeight(30);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setRowHeaderView(rowTable);

        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(scrollPane);
        frame.pack();
        frame.setLocationRelativeTo(this);
        frame.setVisible(true);
    }
}
