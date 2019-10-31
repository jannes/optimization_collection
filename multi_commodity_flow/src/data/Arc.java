package data;

public class Arc {
    public final int from;
    public final int to;
    public final int cost;
    public final int capacity;

    public Arc(int from, int to, int cost, int capacity) {
        this.from = from;
        this.to = to;
        this.cost = cost;
        this.capacity = capacity;
    }
}
