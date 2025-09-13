package org.example;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;
import java.util.List;

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
    private static final String ICON_ADJMATRIX = "/a.png";
    private static final String ICON_INCMATRIX = "/i.png";
    private static final String ICON_CHECK_ADJACENCY = "/adjacent.png";
    private static final String ICON_PRIM = "/prim.png";
    private Font FONT_BUTTON;

    private final JToggleButton addButton, removeButton, connectButton, disconnectButton, orientationButton;
    private final JButton adjMatrixButton, incMatrixButton, checkAdjacencyButton, primButton, bfsButton, dfsButton, conexoButton;
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
        adjMatrixButton = new JButton(loadIcon(ICON_ADJMATRIX));
        incMatrixButton = new JButton(loadIcon(ICON_INCMATRIX));
        checkAdjacencyButton = new JButton(loadIcon(ICON_CHECK_ADJACENCY));
        primButton = new JButton("PRIM");
        primButton.setFont(FONT_BUTTON);
        bfsButton = new JButton("BFS");
        bfsButton.setFont(FONT_BUTTON);
        dfsButton = new JButton("DFS");
        dfsButton.setFont(FONT_BUTTON);
        conexoButton = new JButton("C.CONEXAS");
        conexoButton.setFont(FONT_BUTTON);

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
        adjMatrixButton.setToolTipText("Gerar Matriz de Adjacência");
        incMatrixButton.setToolTipText("Gerar Matriz de Incidência");
        checkAdjacencyButton.setToolTipText("Verificar se vértices são adjacentes");
        primButton.setToolTipText("Aplicar algoritmo de Prim (AGM)");
        bfsButton.setToolTipText("Aplicar Busca em Largura (BFS)");
        dfsButton.setToolTipText("Aplicar Busca em Profundidade (DFS)");
        conexoButton.setToolTipText("Determinar componentes conexas / fortemente conexas");

        configureToggleAppearance(addButton);
        configureToggleAppearance(removeButton);
        configureToggleAppearance(connectButton);
        configureToggleAppearance(disconnectButton);
        configureToggleAppearance(orientationButton);
        configureButtonAppearance(adjMatrixButton);
        configureButtonAppearance(incMatrixButton);
        configureButtonAppearance(checkAdjacencyButton);
        configureButtonAppearance(primButton);
        configureButtonAppearance(bfsButton);
        configureButtonAppearance(dfsButton);
        configureButtonAppearance(conexoButton);

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
        add(adjMatrixButton);
        add(incMatrixButton);
        add(checkAdjacencyButton);
        add(primButton);
        add(bfsButton);
        add(dfsButton);
        add(conexoButton);

        addButton.setSelected(true);
        addButtonAction();
    }

    private void setupActionListeners() {
        addButton.addActionListener(_ -> addButtonAction());

        removeButton.addActionListener(_ -> removeButtonAction());

        connectButton.addActionListener(_ -> connectButtonAction());

        disconnectButton.addActionListener(_ -> disconnectButtonAction());

        orientationButton.addActionListener(_ -> orientationButtonAction());

        adjMatrixButton.addActionListener(_ -> adjMatrixButtonAction());

        incMatrixButton.addActionListener(_ -> incMatrixButtonAction());

        checkAdjacencyButton.addActionListener(_ -> checkAdjacencyButtonAction());

        primButton.addActionListener(_ -> primButtonAction());

        bfsButton.addActionListener(_ -> bfsButtonAction());

        dfsButton.addActionListener(_ -> dfsButtonAction());

        conexoButton.addActionListener(_ -> conexoButtonAction());

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
        GraphPanel graphPanel = ui.getGraphPanel();
        graphPanel.clearAllAlgorithmVisualizations();
        if (graphPanel.getGraphType() == GraphPanel.GraphType.UNDIRECTED) {
                graphPanel.setGraphType(GraphPanel.GraphType.DIRECTED);
        } else {
                graphPanel.setGraphType(GraphPanel.GraphType.UNDIRECTED);
        }

        orientationButton.setSelected(graphPanel.getGraphType() == GraphPanel.GraphType.DIRECTED);

        updateTooltipsForGraphType();

        System.out.println("Grafo alterado para: " + graphPanel.getGraphTypeDescription());
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

    private void updateTooltipsForGraphType() {
        String connectionType = ui.getGraphPanel().getGraphType() == GraphPanel.GraphType.DIRECTED ? "arcos" : "arestas";

        connectButton.setToolTipText("Conectar vértices (Criar " + connectionType + ")");
        disconnectButton.setToolTipText("Desconectar vértices (Remover " + connectionType + ")");
        orientationButton.setToolTipText(ui.getGraphPanel().getGraphType() == GraphPanel.GraphType.DIRECTED ? "GRAFO DIRIGIDO" : "GRAFO NÃO DIRIGIDO");
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