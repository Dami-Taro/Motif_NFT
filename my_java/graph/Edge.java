package graph;
import java.util.Objects;

public class Edge {

    // contatore globale (single-thread)
    private static long COUNTER = 0;

    private final long id;
    private final UserNode from;
    private final UserNode to;
    private final long timestamp;

    public Edge(UserNode from,
                UserNode to,
                long timestamp) {
        this.id = ++COUNTER;
        this.from = from;
        this.to = to;
        this.timestamp = timestamp;
    }

    public long getId() { return id; }
    public UserNode getFrom() { return from; }
    public UserNode getTo() { return to; }
    public long getTimestamp() { return timestamp; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Edge)) return false;
        Edge edge = (Edge) o;
        return id == edge.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}