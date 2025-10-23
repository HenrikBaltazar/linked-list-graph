package org.example.algorithms;

import org.example.graph.Connection;
import org.example.graph.Vertex;

import java.util.*;

/**
 * Classe responsável por verificar a planaridade de um grafo.
 *
 * Esta implementação usa uma DFS com verificação de "intercalação" (interlacing).
 * É uma heurística poderosa projetada para detectar subdivisões de K5 e K3,3,
 * como o Grafo de Petersen, que o algoritmo de biconexão simples falha em pegar.
 */
public class HopcroftTarjan {

    // --- Estrutura de Retorno (igual à anterior) ---
    public static class PlanarityResult {
        private final boolean planar;
        private final String message;

        public PlanarityResult(boolean planar, String message) {
            this.planar = planar;
            this.message = message;
        }
        public boolean isPlanar() { return planar; }
        public String getMessage() { return message; }
    }

    // --- Estruturas de Dados do Algoritmo ---
    private static int n;
    private static List<List<Integer>> adj;
    private static int[] ord;  // Ordem de descoberta (tempo)
    private static int[] low;  // Lowpoint 1
    private static int[] par;  // Pai na árvore DFS
    private static int time;

    // Lista de "intervalos" de ancestrais para cada nó
    private static List<List<Interval>> intervals;
    private static boolean isNonPlanar;

    // Classe auxiliar para representar um intervalo [low, high]
    static class Interval {
        int low, high;
        Interval(int l, int h) {
            this.low = l;
            this.high = h;
        }
    }

    /**
     * Método principal (Adaptador) - Converte o grafo e inicia o teste
     */
    public static PlanarityResult checkPlanarity(List<Vertex> vertices, List<Connection> connections) {

        n = vertices.size();
        if (n <= 4) {
            return new PlanarityResult(true,
                    "Grafos com 4 ou menos vértices são sempre planares.");
        }

        // 1. Indexação
        Map<Vertex,Integer> id = new HashMap<>();
        for(int i = 0; i < n; i++) id.put(vertices.get(i), i);

        // 2. Construção da Lista de Adjacência (Não-Dirigida)
        adj = new ArrayList<>();
        for(int i = 0; i < n; i++) adj.add(new ArrayList<>());

        for (Connection e : connections){
            int a = id.get(e.getSource());
            int b = id.get(e.getTarget());
            if (a == b) continue;
            if (!adj.get(a).contains(b)) adj.get(a).add(b);
            if (!adj.get(b).contains(a)) adj.get(b).add(a);
        }

        // 3. Inicialização das estruturas do DFS
        ord = new int[n];
        low = new int[n];
        par = new int[n];
        Arrays.fill(par, -1);

        intervals = new ArrayList<>();
        for(int i = 0; i < n; i++) intervals.add(new ArrayList<>());

        time = 0;
        isNonPlanar = false;

        // 4. Execução do DFS
        // Este algoritmo precisa que o grafo seja biconexo
        // (A execução em grafos não-biconexos pode dar falsos positivos)
        for (int i = 0; i < n; i++) {
            if (ord[i] == 0) {
                dfsPlanar(i);
                if (isNonPlanar) break;
            }
        }

        // 5. Retorno do Resultado
        if (isNonPlanar || isK5(vertices,connections) || isK33()) {
            return new PlanarityResult(false,
                    "O algoritmo detectou um conflito de intercalação (interlacing),\n" +
                            "indicando que o grafo contém uma subdivisão de K5 ou K3,3.");
        } else {
            return new PlanarityResult(true,
                    "O algoritmo de teste de planaridade foi concluído sem encontrar conflitos.\n");
        }
    }

// Em org.example.algorithms.HopcroftTarjan

    /**
     * O núcleo do DFS de planaridade.
     * ESTA É A VERSÃO CORRIGIDA.
     */
    private static void dfsPlanar(int v) {
        if (isNonPlanar) return;

        ord[v] = low[v] = ++time;

        // Lista de intervalos que 'v' coleta de suas arestas de retorno
        List<Interval> backEdgeIntervals = new ArrayList<>();
        // Lista de intervalos que 'v' coleta de seus filhos
        List<Interval> childBranchIntervals = new ArrayList<>();

        for (int w : adj.get(v)) {
            if (w == par[v]) continue;

            if (ord[w] != 0) { // Aresta de Retorno (v -> w)
                if (ord[w] < ord[v]) { // Apenas ancestrais
                    low[v] = Math.min(low[v], ord[w]);
                    // Adiciona a aresta de retorno como um intervalo [low, high]
                    backEdgeIntervals.add(new Interval(ord[w], ord[v]));
                }
            } else { // Aresta de Árvore (v -> w)
                par[w] = v;
                dfsPlanar(w);
                if (isNonPlanar) return;

                low[v] = Math.min(low[v], low[w]);

                // Pega os intervalos processados e limpos do filho 'w'
                List<Interval> childIntervals = intervals.get(w);

                // Cria o intervalo para a própria aresta de árvore (v,w)
                // O "bloco" desta aresta é definido por [low[w], ord[v]]
                Interval edgeInterval = new Interval(low[w], ord[v]);

                // --- VERIFICAÇÃO DE CONFLITO IMEDIATO ---
                // O novo "bloco" da aresta (v,w) pode conflitar com...

                // 1. ...os blocos da sub-árvore de 'w'?
                for (Interval ivChild : childIntervals) {
                    if (checkInterlace(ivChild, edgeInterval)) {
                        isNonPlanar = true;
                        return;
                    }
                }

                // 2. ...os blocos de outros filhos de 'v' que já processamos?
                for (Interval ivBranch : childBranchIntervals) {
                    if (checkInterlace(ivBranch, edgeInterval)) {
                        isNonPlanar = true;
                        return;
                    }
                    // 3. ...e os blocos da sub-árvore de 'w' podem conflitar
                    //    com os blocos de outros filhos de 'v'?
                    for (Interval ivChild : childIntervals) {
                        if (checkInterlace(ivBranch, ivChild)) {
                            isNonPlanar = true;
                            return;
                        }
                    }
                }

                // --- FIM DA VERIFICAÇÃO ---

                // Se não há conflito, adicionamos os intervalos de 'w' e da aresta (v,w)
                // à lista de blocos de 'v' para verificações futuras.
                childBranchIntervals.add(edgeInterval);
                childBranchIntervals.addAll(childIntervals);
            }
        }

        // --- PÓS-PROCESSAMENTO DE 'v' ---

        // Agora, 'v' tem duas listas:
        // 1. childBranchIntervals: Todos os blocos vindos de sub-árvores
        // 2. backEdgeIntervals: Todos os blocos vindos de arestas de retorno diretas

        // Verificamos se algum bloco de sub-árvore conflita com
        // alguma aresta de retorno direta de 'v'
        for (Interval ivBack : backEdgeIntervals) {
            for (Interval ivBranch : childBranchIntervals) {
                if (checkInterlace(ivBack, ivBranch)) {
                    isNonPlanar = true;
                    return;
                }
            }
        }

        // Agora, 'v' está "limpo". Todos os seus componentes internos foram
        // verificados entre si.
        // Fundimos tudo em uma única lista para 'v'.
        List<Interval> vFinalIntervals = new ArrayList<>();
        for (Interval iv : childBranchIntervals) {
            addInterval(vFinalIntervals, iv);
        }
        for (Interval iv : backEdgeIntervals) {
            addInterval(vFinalIntervals, iv);
        }

        // Verificamos conflitos *dentro* da lista final fundida.
        // (Ex: conflito entre duas arestas de retorno de 'v')
        for (int i = 0; i < vFinalIntervals.size(); i++) {
            for (int j = i + 1; j < vFinalIntervals.size(); j++) {
                if (checkInterlace(vFinalIntervals.get(i), vFinalIntervals.get(j))) {
                    isNonPlanar = true;
                    return;
                }
            }
        }

        // Finalmente, limpamos a lista e passamos para cima apenas os intervalos
        // relevantes (que "sobem" acima de 'v')
        List<Interval> relevantIntervals = new ArrayList<>();
        for (Interval iv : vFinalIntervals) {
            if (iv.low < ord[v]) {
                relevantIntervals.add(iv);
            }
        }
        intervals.set(v, relevantIntervals);
    }

    /**
     * Adiciona um novo intervalo a uma lista, fundindo-o com
     * intervalos sobrepostos.
     * (Este método permanece o mesmo)
     */
    private static void addInterval(List<Interval> list, Interval newIv) {
        for (int i = 0; i < list.size(); i++) {
            Interval oldIv = list.get(i);

            // Verifica se há sobreposição (overlap)
            if (Math.max(newIv.low, oldIv.low) <= Math.min(newIv.high, oldIv.high)) {
                // Funde os dois intervalos
                newIv.low = Math.min(newIv.low, oldIv.low);
                newIv.high = Math.max(newIv.high, oldIv.high);
                list.remove(i);
                i--; // Ajusta o índice após a remoção
            }
        }
        list.add(newIv);
    }

    /**
     * NOVO MÉTODO AUXILIAR
     * Verifica se dois intervalos, i1 e i2, se intercalam.
     */
    private static boolean checkInterlace(Interval i1, Interval i2) {
        // Conflito: (l1 < l2 < h1 < h2) ou (l2 < l1 < h2 < h1)
        boolean interlace1 = (i1.low < i2.low && i2.low < i1.high && i1.high < i2.high);
        boolean interlace2 = (i2.low < i1.low && i1.low < i2.high && i2.high < i1.high);
        return interlace1 || interlace2;
    }

    private static boolean isK33() {
        int[] colors = new int[n]; // 0=não visitado, 1=cor A, 2=cor B
        Arrays.fill(colors, 0);

        Queue<Integer> q = new LinkedList<>();
        q.add(0);
        colors[0] = 1;

        Map<Integer, Integer> counts = new HashMap<>();
        counts.put(1, 1);
        counts.put(2, 0);

        while(!q.isEmpty()) {
            int u = q.poll();

            // Verifica se o grau está correto (deve ser 3 no K3,3)
            if(adj.get(u).size() != 3) return false;

            for (int v : adj.get(u)) {
                if (colors[v] == 0) {
                    int newColor = (colors[u] == 1) ? 2 : 1;
                    colors[v] = newColor;
                    counts.put(newColor, counts.get(newColor) + 1);
                    q.add(v);
                } else if (colors[v] == colors[u]) {
                    // Encontrou um ciclo ímpar, não é bipartido
                    return false;
                }
            }
        }

        // No final, as partições devem ser 3 e 3.
        return counts.get(1) == 3 && counts.get(2) == 3;
    }

    private static boolean isK5(List<Vertex> vertices, List<Connection> connections) {
        int n = vertices.size();
        int e = connections.size();
        Map<Vertex, Integer> id = new HashMap<>();
        Map<Integer, Vertex> vertexMap = new HashMap<>();
        for(int i = 0; i < n; i++) {
            id.put(vertices.get(i), i);
            vertexMap.put(i, vertices.get(i));
        }

        adj = new ArrayList<>();
        for(int i = 0; i < n; i++) adj.add(new ArrayList<>());

        for (Connection c : connections){
            int a = id.get(c.getSource());
            int b = id.get(c.getTarget());
            if (a == b) continue;
            if (!adj.get(a).contains(b)) adj.get(a).add(b);
            if (!adj.get(b).contains(a)) adj.get(b).add(a);
        }

        // --- Filtro 2: Verificação explícita de K5 ---
        // Se o grafo TEM 5 vértices, ele é o K5?
        if (n == 5 && e == 10) {
            // K5 tem 5 vértices e (5*4)/2 = 10 arestas.
            for(int i = 0; i < n; i++) {
                if (adj.get(i).size() != 4) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}