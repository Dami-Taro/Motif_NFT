package src.motifMiner.patterns;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import src.graph.Edge;
import src.graph.UserNode;

public class OutStar extends Pattern {

    public OutStar(List<Edge> edges) {
        super(edges);
    }

    public UserNode getCenter() {
        return getEdges().isEmpty() ? null : getEdges().get(0).getFrom();
    }

    public Set<UserNode> getBuyers() {
        Set<UserNode> buyers = new HashSet<>();
        for (Edge e : getEdges()) {
            buyers.add(e.getTo());
        }
        return buyers;
    }

    @Override
    public String toString() {
        return String.format(
            "OutStar: center %s (k=%d , Δt=%d sec)",
            getCenter().getAddress().substring(0, 8),
            getSize(),
            getDuration()
        );
    }
}
