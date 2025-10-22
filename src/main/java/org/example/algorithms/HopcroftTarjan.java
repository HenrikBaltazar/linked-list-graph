package org.example.algorithms;

import org.example.graph.Connection;
import org.example.graph.Vertex;

import java.util.*;


public class HopcroftTarjan {


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


    public static PlanarityResult checkPlanarity(List<Vertex> vertices, List<Connection> connections) {


        if (vertices.size() <= 4) {
            return new PlanarityResult(true,
                    "Grafos com 4 ou menos vértices são sempre planares.");
        }

        List<Vertex> verts = vertices;
        int n = verts.size();
        Map<Vertex,Integer> id = new HashMap<>();
        for(int i = 0; i < n; i++) id.put(verts.get(i), i);

        List<List<Integer>> adj = new ArrayList<>();
        for(int i = 0; i < n; i++) adj.add(new ArrayList<>());

        for (Connection e : connections){
            int a = id.get(e.getSource());
            int b = id.get(e.getTarget());

            if (a == b) continue;
            if (!adj.get(a).contains(b)) adj.get(a).add(b);
            if (!adj.get(b).contains(a)) adj.get(b).add(a);
        }

        int[] parent = new int[n], order = new int[n], lowpt = new int[n], lowpt2 = new int[n];
        Arrays.fill(parent,-1);
        int[] time = {0};

        Deque<int[]> estack = new ArrayDeque<>();

        Map<Long, Boolean> left = new HashMap<>(); // true=L, false=R

        boolean isPlanar = true;
        try {
            for (int s = 0; s < n; s++){
                if (order[s] == 0){
                    if (!dfsHT(s, -1, adj, order, lowpt, lowpt2, parent, time, estack, left)) {
                        isPlanar = false;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new PlanarityResult(false,
                    "Uma exceção ocorreu durante a execução: " + e.getMessage());
        }


        if (isPlanar) {
            return new PlanarityResult(true,
                    "O algoritmo de teste de planaridade foi concluído sem encontrar conflitos.");
        } else {
            return new PlanarityResult(false,
                    "O algoritmo detectou um conflito de intercalação (interlacing),\n" +
                            "indicando que o grafo contém uma subdivisão de K5 ou K3,3.");
        }
    }


    private static boolean dfsHT(int v, int p, List<List<Integer>> adj, int[] ord, int[] low, int[] low2,
                                 int[] par, int[] time, Deque<int[]> estack, Map<Long,Boolean> left){
        ord[v] = ++time[0]; low[v] = ord[v]; low2[v] = ord[v]; par[v] = p;

        for (int w : adj.get(v)){
            if (w == p) continue;
            if (ord[w] == 0){
                estack.push(new int[]{v,w});
                if (!dfsHT(w, v, adj, ord, low, low2, par, time, estack, left)) return false;

                if (low[w] >= ord[v]){
                    List<int[]> block = new ArrayList<>();
                    int[] e;
                    do { e = estack.pop(); block.add(e); } while(!(e[0] == v && e[1] == w));

                    if (!embedBlockLR(block, ord, low, par, left)) return false;
                }

                if (low[w] < low[v]){
                    low2[v] = Math.min(low[v], Math.min(low2[v], low2[w]));
                    low[v]  = low[w];
                } else if (low[w] > low[v]) {
                    low2[v] = Math.min(low2[v], low[w]);
                } else {
                    low2[v] = Math.min(low2[v], low2[w]);
                }

            } else if (ord[w] < ord[v]) {
                estack.push(new int[]{v,w});
                if (ord[w] < low[v]) { low2[v] = low[v]; low[v] = ord[w]; }
                else if (ord[w] > low[v]) { low2[v] = Math.min(low2[v], ord[w]); }
            }
        }
        return true;
    }

    private static boolean embedBlockLR(List<int[]> block, int[] ord, int[] low, int[] par,
                                        Map<Long,Boolean> left) {
        Map<Integer, Boolean> side = new HashMap<>();

        for (int[] e : block) {
            int a = e[0], b = e[1];

            if (par[b] == a) {

                boolean wantLeft = (low[b] < ord[a]);

                Boolean already = side.get(a);
                if (already == null) {
                    side.put(a, wantLeft);
                } else if (already != wantLeft) {
                    return false;
                }
            }
        }
        return true;
    }

    private static int ancestor(int v, int[] par){
        return (v >= 0 && par[v] >= 0) ? par[par[v]] : -1;
    }
}