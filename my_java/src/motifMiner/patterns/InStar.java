package src.motifMiner.patterns;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import src.graph.Edge;
import src.graph.UserNode;

public class InStar extends Pattern {

    public InStar(List<Edge> edges) {
        super(edges);
    }

    public UserNode getCenter() {
        return getEdges().isEmpty() ? null : getEdges().get(0).getTo();
    }

    public Set<UserNode> getSellers() {
        Set<UserNode> sellers = new HashSet<>();
        for (Edge e : getEdges()) {
            sellers.add(e.getFrom());
        }
        return sellers;
    }

    @Override
    public String toString() {
        return String.format(
            "InStar: center %s (k=%d , Δt=%d sec)",
            getCenter().getAddress().substring(0, 8),
            getSize(),
            getDuration()
        );
    }
}

