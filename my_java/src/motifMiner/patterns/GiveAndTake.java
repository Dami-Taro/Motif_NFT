package src.motifMiner.patterns;

import java.util.List;

import src.graph.Edge;
import src.graph.UserNode;

public class GiveAndTake extends Pattern {

    public GiveAndTake(List<Edge> edges) {
        super(edges);
    }

    public UserNode getToNode() {
        return getEdges().isEmpty() ? null : getEdges().get(0).getTo();
    }

    public UserNode getCenter() {
        return getEdges().isEmpty() ? null : getEdges().get(1).getTo();
    }
    
    public UserNode getFromNode() {
        return getEdges().isEmpty() ? null : getEdges().get(1).getFrom();
    }

    @Override
    public String toString() {
        return String.format(
            "GiveAndTake:  %s → [%s] → %s (k=%d , Δt=%d sec)",
            getFromNode().getAddress().substring(0, 8),
            getCenter().getAddress().substring(0, 8),
            getToNode().getAddress().substring(0, 8),
            getSize(),
            getDuration()
        );
    }
}
