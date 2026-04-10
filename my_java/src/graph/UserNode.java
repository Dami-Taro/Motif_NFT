package src.graph;


import java.util.NavigableSet;
import java.util.TreeSet;

public class UserNode {

    private final String address;
    private final NavigableSet<Edge> outgoingEdges;
    private final NavigableSet<Edge> incomingEdges;
    private String entity=null; 

    public UserNode(String address) {
        this.address = address.toLowerCase();
        EdgeTimestampComparator cmp = new EdgeTimestampComparator();
        this.outgoingEdges = new TreeSet<>(cmp);
        this.incomingEdges = new TreeSet<>(cmp);
        this.entity = null;
    }

    public String getAddress() { return address; }
    public String getSimpleAddress() {
        if (address == null) return null;

        int len = address.length();
        if (len <= 8) {
            return address;
        }
        return address.substring(len - 8);
    }
    public NavigableSet<Edge> getOutgoingEdges() { return outgoingEdges; }
    public NavigableSet<Edge> getIncomingEdges() { return incomingEdges; }

    void addOutgoingEdge(Edge e) { outgoingEdges.add(e); }
    void addIncomingEdge(Edge e) { incomingEdges.add(e); }

    public String getEntity() { return entity; } 

    public void setEntity(String newEntity) {
        // Caso 1: entity non ancora settata
        if (this.entity == null) {
            if (newEntity != null) {
                this.entity = newEntity;
            }
            return;
        }

        // Caso 2: entity già settata
        if (newEntity == null) {
            System.out.println("⚠️ Attempt to overwrite entity with null for address: " + address);
            return;
        }

        if (!this.entity.equals(newEntity)) {
            System.out.println("⚠️ Conflicting entity for address " + address +
                " | existing: " + this.entity +
                " | new: " + newEntity);
        }
    }

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