package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

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
    private static final ImageIcon ICON_CHECK_ADJACENCY = new ImageIcon(resourcesPath + "adjacent.png");
    private static final ImageIcon ICON_PRIM = new ImageIcon(resourcesPath + "prim.png");
    private static final ImageIcon ICON_BFS = new ImageIcon(resourcesPath + "prim.png");
    private static final ImageIcon ICON_DFS = new ImageIcon(resourcesPath + "prim.png");


    private final JToggleButton addButton, removeButton, connectButton, disconnectButton, orientationButton;
    private final JButton adjMatrixButton, incMatrixButton, checkAdjacencyButton, primButton, bfsButton, dfsButton;
    private final Interface ui;

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
        checkAdjacencyButton = new JButton(ICON_CHECK_ADJACENCY);
        primButton = new JButton(ICON_PRIM);
        bfsButton = new JButton(ICON_BFS);
        dfsButton = new JButton(ICON_DFS);

        addButton.setSelectedIcon(ICON_ADD_SELECTED);
        removeButton.setSelectedIcon(ICON_REMOVE_SELECTED);
        connectButton.setSelectedIcon(ICON_CONNECT_SELECTED);
        disconnectButton.setSelectedIcon(ICON_DISCONNECT_SELECTED);
        orientationButton.setSelectedIcon(ICON_ORIENTATION_SELECTED);

        addButton.setToolTipText("Adicionar vértice");
        removeButton.setToolTipText("Remover vértice (Modo Seleção)");
        connectButton.setToolTipText("Conectar vértices");
        disconnectButton.setToolTipText("Desconectar vértices");
        orientationButton.setToolTipText("Mudar grafo orientado e não-orientado");
        adjMatrixButton.setToolTipText("Gerar Matriz de Adjacência");
        incMatrixButton.setToolTipText("Gerar Matriz de Incidência");
        checkAdjacencyButton.setToolTipText("Verificar se vértices são adjacentes");
        primButton.setToolTipText("Aplicar algoritmo de Prim (AGM)");
        bfsButton.setToolTipText("Aplicar Busca em Largura (BFS)");
        dfsButton.setToolTipText("Aplicar Busca em Profundidade (DFS)");

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

        ButtonGroup toolModeGroup = new ButtonGroup();
        toolModeGroup.add(addButton);
        toolModeGroup.add(removeButton);
        toolModeGroup.add(connectButton);
        toolModeGroup.add(disconnectButton);

        setupActionListeners();

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
        add(checkAdjacencyButton);
        add(primButton);
        add(bfsButton);
        add(dfsButton);

        addButton.setSelected(true);
        addButtonAction();
    }

    // Listeners -------------------------------------------------------------------------------------

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

    }

    // Appearance ------------------------------------------------------------------------------------

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

    // Button action ---------------------------------------------------------------------------------
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

        // Alterna entre os tipos de grafo
        if (graphPanel.getGraphType() == GraphType.DIRECTED) {
            graphPanel.setGraphType(GraphType.UNDIRECTED);
        } else {
            graphPanel.setGraphType(GraphType.DIRECTED);
        }

        // Atualiza o estado visual do botão baseado no tipo atual
        boolean isDirected = graphPanel.getGraphType() == GraphType.DIRECTED;
        orientationButton.setSelected(isDirected);

        // Atualiza tooltips baseado no tipo atual
        updateTooltipsForGraphType();

        System.out.println("Grafo alterado para: " + graphPanel.getGraphType().getDescription());
    }

    public void adjMatrixButtonAction() {
        GraphPanel graphPanel = ui.getGraphPanel();
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

        String title = "Matriz de Adjacência - " +
                (graphPanel.getGraphType() == GraphType.DIRECTED ? "Dirigido" : "Não Dirigido");

        showMatrixFrame(matrix, vertexLabels, vertexLabels, title);
    }

    public void incMatrixButtonAction() {
        GraphPanel graphPanel = ui.getGraphPanel();
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

        String title = "Matriz de Incidência - " +
                (graphPanel.getGraphType() == GraphType.DIRECTED ? "Dirigido" : "Não Dirigido");

        showMatrixFrame(matrix, vertexLabels, connectionLabels, title);
    }

    public void checkAdjacencyButtonAction() {
        ui.getGraphPanel().checkVertexAdjacency();
    }

    public void primButtonAction() {
        GraphPanel graphPanel = ui.getGraphPanel();

        // Se já está mostrando uma AGM, pergunta se quer limpar ou calcular nova
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
            // Se YES, continua para calcular nova AGM
        }

        List<Connection> mst = graphPanel.applyPrimAlgorithm();

        if (mst != null && !mst.isEmpty()) {
            System.out.println("Algoritmo de Prim executado com sucesso!");
        }
    }

    public void bfsButtonAction() {
        GraphPanel graphPanel = ui.getGraphPanel();

        // Se já está mostrando uma BFS, pergunta se quer limpar ou calcular nova
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
            // Se YES, continua para calcular nova BFS
        }

        // Limpa AGM se estiver visível para evitar confusão visual
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

        // Se já está mostrando uma DFS, pergunta se quer limpar ou calcular nova
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
            // Se YES, continua para calcular nova DFS
        }

        // Limpa AGM e BFS se estiverem visíveis para evitar confusão visual
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

    // Outros métodos --------------------------------------------------------------------------------
    private void updateTooltipsForGraphType() {
        GraphType currentType = ui.getGraphPanel().getGraphType();
        String connectionType = currentType == GraphType.DIRECTED ? "arcos" : "arestas";

        connectButton.setToolTipText("Conectar vértices (Criar " + connectionType + ")");
        disconnectButton.setToolTipText("Desconectar vértices (Remover " + connectionType + ")");
    }

    private void showMatrixFrame(int[][] matrix, String[] rowLabels, String[] colLabels, String title) {
        int n = matrix.length;
        int m = matrix[0].length;

        // Converte a matriz para Object[][] para a JTable
        Object[][] data = new Object[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                data[i][j] = matrix[i][j];
            }
        }

        // Cria a tabela com labels personalizados
        JTable table = new JTable(data, colLabels) {
            public String getRowName(int row) {
                return rowLabels[row];
            }
        };

        table.setEnabled(false);
        table.setRowHeight(30);

        // Adiciona os nomes das linhas
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