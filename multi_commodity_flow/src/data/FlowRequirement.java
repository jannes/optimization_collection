package data;

public class FlowRequirement {

    public final int from;
    public final int to;
    public final int requiredFlow;

    public FlowRequirement(int from, int to, int requiredFlow) {
        this.from = from;
        this.to = to;
        this.requiredFlow = requiredFlow;
    }
}
