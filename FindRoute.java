import java.io.*;
import java.util.*;

/**
 * FindRoute.java
 * Optimized Route Detection between two cities using A* and Greedy (best-first) search.
 * Output format mirrors the original C++: Nodes Popped / Expanded / Generated, Distance, and Route.
 *
 * Usage:
 *   java FindRoute <edgesFile> <startCity> <goalCity> [--heuristic <heurFile>] [--algo astar|greedy]
 * Defaults:
 *   - If no --heuristic is provided, all h(n)=0  => Uniform-Cost Search.
 *   - If --heuristic is provided but --algo omitted => A*.
 */
public class FindRoute {

    // Graph data
    private final Map<String, List<Edge>> adj = new HashMap<>();
    private final Map<String, Double> heuristic = new HashMap<>();

    // Counters (to mirror original output)
    private int nodesGenerated = 0;
    private int nodesPopped = 0;
    private int nodesExpanded = 0;

    // ===== Data types =====
    private static class Edge {
        final String to;
        final double cost;
        Edge(String to, double cost) { this.to = to; this.cost = cost; }
    }

    private static class Node {
        final String name;
        final Node parent;
        final double g;  // cumulative cost
        final double h;  // heuristic
        final int depth;

        Node(String name, Node parent, double g, double h, int depth) {
            this.name = name;
            this.parent = parent;
            this.g = g;
            this.h = h;
            this.depth = depth;
        }

        double fAStar() { return g + h; }
        double fGreedy() { return h; }
    }

    // ===== Loading utilities =====
    private void loadEdges(String edgesFile) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(edgesFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (line.equalsIgnoreCase("END") || line.equalsIgnoreCase("END OF INPUT")) break;
                String[] parts = line.split("\\s+");
                if (parts.length < 3) continue;
                String a = parts[0], b = parts[1];
                double d = Double.parseDouble(parts[2]);
                adj.computeIfAbsent(a, k -> new ArrayList<>()).add(new Edge(b, d));
                adj.computeIfAbsent(b, k -> new ArrayList<>()).add(new Edge(a, d)); // undirected
            }
        }
    }

    private void loadHeuristics(String heurFile) throws IOException {
        heuristic.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(heurFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (line.equalsIgnoreCase("END") || line.equalsIgnoreCase("END OF INPUT")) break;
                String[] parts = line.split("\\s+");
                if (parts.length < 2) continue;
                String city = parts[0];
                double h = Double.parseDouble(parts[1]);
                heuristic.put(city, h);
            }
        }
    }

    // ===== Search variants =====
    public Result search(String start, String goal, String algo) {
        // If no heuristics provided, default to 0
        double hStart = heuristic.getOrDefault(start, 0.0);

        Comparator<Node> cmp;
        if ("greedy".equalsIgnoreCase(algo)) {
            cmp = Comparator.comparingDouble(Node::fGreedy).thenComparing(n -> n.name);
        } else { // astar (default)
            cmp = Comparator.comparingDouble(Node::fAStar).thenComparing(n -> n.name);
        }

        PriorityQueue<Node> fringe = new PriorityQueue<>(cmp);
        Map<String, Double> bestG = new HashMap<>(); // best known cost for a city

        Node startNode = new Node(start, null, 0.0, hStart, 0);
        fringe.add(startNode);
        bestG.put(start, 0.0);
        // generated includes the root when it hits the fringe
        nodesGenerated = 1;
        nodesPopped = 0;
        nodesExpanded = 0;

        while (!fringe.isEmpty()) {
            Node cur = fringe.poll();
            nodesPopped++;

            if (cur.name.equals(goal)) {
                return reconstruct(cur);
            }

            nodesExpanded++;

            // closed-set-ish behavior via bestG: only expand if this is the current best path
            double recorded = bestG.getOrDefault(cur.name, Double.POSITIVE_INFINITY);
            if (cur.g > recorded + 1e-9) {
                // stale entry
                continue;
            }

            for (Edge e : adj.getOrDefault(cur.name, Collections.emptyList())) {
                double newG = cur.g + e.cost;
                double prevBest = bestG.getOrDefault(e.to, Double.POSITIVE_INFINITY);

                boolean better = newG + 1e-9 < prevBest;
                if (better) {
                    bestG.put(e.to, newG);
                    double h = heuristic.getOrDefault(e.to, 0.0);
                    Node next = new Node(e.to, cur, newG, h, cur.depth + 1);
                    fringe.add(next);
                    nodesGenerated++;
                }
            }
        }

        return Result.noRoute(nodesPopped, nodesExpanded, nodesGenerated);
    }

    // ===== Result & route reconstruction =====
    private Result reconstruct(Node goalNode) {
        List<Node> path = new ArrayList<>();
        Node cur = goalNode;
        while (cur != null) {
            path.add(cur);
            cur = cur.parent;
        }
        Collections.reverse(path);

        // Build segment breakdown
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < path.size() - 1; i++) {
            Node a = path.get(i);
            Node b = path.get(i + 1);
            lines.add(String.format("%s to %s, %.1f km", a.name, b.name, b.g - a.g));
        }

        return new Result(nodesPopped, nodesExpanded, nodesGenerated, goalNode.g, lines);
    }

    public static class Result {
        final int popped, expanded, generated;
        final double distance; // Infinity if unreachable
        final List<String> routeLines;

        static Result noRoute(int popped, int expanded, int generated) {
            return new Result(popped, expanded, generated, Double.POSITIVE_INFINITY, Collections.emptyList());
        }

        Result(int popped, int expanded, int generated, double distance, List<String> routeLines) {
            this.popped = popped;
            this.expanded = expanded;
            this.generated = generated;
            this.distance = distance;
            this.routeLines = routeLines;
        }

        void print() {
            System.out.println("\nNodes Popped: " + popped);
            System.out.println("Nodes Expanded: " + expanded);
            System.out.println("Nodes Generated: " + generated);
            if (!Double.isFinite(distance)) {
                System.out.println("Distance: Infinity");
                System.out.println("Route:");
                System.out.println("None");
            } else {
                System.out.printf("Distance: %.1f km%n", distance);
                System.out.println("Route:");
                for (String s : routeLines) System.out.println(s);
            }
        }
    }

    // ===== Main / CLI parsing =====
    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: java FindRoute <edgesFile> <startCity> <goalCity> [--heuristic <heurFile>] [--algo astar|greedy]");
            return;
        }

        String edgesFile = args[0];
        String start = args[1];
        String goal = args[2];
        String heurFile = null;
        String algo = "astar"; // default if heuristics are present

        for (int i = 3; i < args.length; i++) {
            if ("--heuristic".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                heurFile = args[++i];
            } else if ("--algo".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                algo = args[++i];
            }
        }

        FindRoute app = new FindRoute();
        try {
            app.loadEdges(edgesFile); // expects lines like: "CityA CityB 123", ending with "END OF INPUT".
            if (heurFile != null) {
                app.loadHeuristics(heurFile); // expects lines like: "City 200", ending with "END OF INPUT".
            } else {
                // no heuristic file -> all zeros => uniform-cost behavior regardless of algo flag
                app.heuristic.clear();
                algo = "astar";
            }

            Result r = app.search(start, goal, algo);
            r.print();

        } catch (IOException e) {
            System.err.println("File error: " + e.getMessage());
        }
    }
}
