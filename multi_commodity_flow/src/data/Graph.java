package data;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Graph {

    private List<Integer> nodes;
    // arc order should be adhered to by the rmp and subproblems
    private List<Arc> arcs;

    public Graph(List<Arc> arcs, int numNodes) {
        this.arcs = arcs;
        this.nodes = IntStream.range(1, numNodes + 1).boxed().collect(Collectors.toList());
    }

    public Set<Arc> getOutArcs(int node) {
        return arcs.stream()
                .filter(a -> a.from == node)
                .collect(Collectors.toSet());
    }

    public Set<Arc> getInArcs(int node) {
        return arcs.stream()
                .filter(a -> a.to == node)
                .collect(Collectors.toSet());
    }

    public List<Integer> getNodes() {
        return nodes;
    }

    public List<Arc> getArcs() {
        return arcs;
    }

    public int numArcs() {
        return arcs.size();
    }
}
