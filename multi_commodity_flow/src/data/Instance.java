package data;

import java.util.List;

public class Instance {

    public final Graph graph;
    public final List<FlowRequirement> flowRequirements;
    public final int numCommodities;

    public Instance(Graph graph, List<FlowRequirement> flowRequirements) {
        this.graph = graph;
        this.flowRequirements = flowRequirements;
        this.numCommodities = flowRequirements.size();
    }
}
