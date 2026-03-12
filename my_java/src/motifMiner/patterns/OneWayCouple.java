package src.motifMiner.patterns;

import java.util.List;

import src.graph.Edge;
import src.graph.UserNode;

public class OneWayCouple extends Pattern {

    @Override
    public String getName() { return "OneWayCouple";}

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
    public void validate() throws PatternValidationException {
        if (edges.size() < 2) {
            throw new PatternValidationException(
                getName() + " must contain at least 2 edges"
            );
        }

        UserNode from = edges.get(0).getFrom();
        UserNode to = edges.get(0).getTo();

        for (Edge e : edges) {
            if (!e.getFrom().equals(from) || !e.getTo().equals(to)) {
                throw new PatternValidationException(
                    getName() + ": All edges must have the same source and destination"
                );
            }
        }
    }

    @Override
    public String toString() {
        return String.format(
            "%s: %s → %s (k=%d , Δt=%d sec)",
            getName(),
            getBuyer().getSimpleAddress(),
            getSeller().getSimpleAddress(),
            getSize(),
            getDuration()
        );
    }
}
