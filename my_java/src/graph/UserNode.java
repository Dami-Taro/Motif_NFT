package src.graph;


import java.util.NavigableSet;
import java.util.TreeSet;

public class UserNode {

    private final String address;
    private final NavigableSet<Edge> outgoingEdges;
    private final NavigableSet<Edge> incomingEdges;

    public UserNode(String address) {
        this.address = address.toLowerCase();
        EdgeTimestampComparator cmp = new EdgeTimestampComparator();
        this.outgoingEdges = new TreeSet<>(cmp);
        this.incomingEdges = new TreeSet<>(cmp);
    }

    public String getAddress() { return address; }
    public NavigableSet<Edge> getOutgoingEdges() { return outgoingEdges; }
    public NavigableSet<Edge> getIncomingEdges() { return incomingEdges; }

    void addOutgoingEdge(Edge e) { outgoingEdges.add(e); }
    void addIncomingEdge(Edge e) { incomingEdges.add(e); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserNode)) return false;
        return address.equals(((UserNode) o).address);
    }

    @Override
    public int hashCode() {
        return address.hashCode();
    }
}