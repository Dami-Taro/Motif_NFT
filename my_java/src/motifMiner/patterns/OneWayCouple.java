package src.motifMiner.patterns;

import java.util.List;

import src.graph.Edge;
import src.graph.UserNode;

public class OneWayCouple extends Pattern {

    public OneWayCouple(List<Edge> edges) {
        super(edges);
    }

    public UserNode getBuyer() {
        return getEdges().isEmpty() ? null : getEdges().get(0).getFrom();
    }

    public UserNode getSeller() {
        return getEdges().isEmpty() ? null : getEdges().get(0).getTo();
    }

    @Override
    public String toString() {
        return String.format(
            "OneWayCouple: %s → %s (k=%d , Δt=%d sec)",
            getBuyer().getAddress().substring(0, 8),
            getSeller().getAddress().substring(0, 8),
            getSize(),
            getDuration()
        );
    }
}
