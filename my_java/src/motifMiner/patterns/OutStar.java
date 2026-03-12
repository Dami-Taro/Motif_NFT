package src.motifMiner.patterns;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import src.graph.Edge;
import src.graph.UserNode;

public class OutStar extends Pattern {

    @Override
    public String getName() { return "OutStar";}

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
    public void validate() throws PatternValidationException {
        if (edges.size() < 2) {
            throw new PatternValidationException(
                getName() + " must contain at least 2 edges"
            );
        }

        UserNode center = edges.get(0).getFrom();
        Set<UserNode> targets = new HashSet<>();

        for (Edge e : edges) {
            if (!e.getFrom().equals(center)) {
                throw new PatternValidationException(
                    getName() + ": All edges must originate from the same center node"
                );
            }

            if (!targets.add(e.getTo())) {
                throw new PatternValidationException(
                    getName() + ": Duplicate target node in OutStar"
                );
            }
        }
    }

    @Override
    public String toString() {
        return String.format(
            "%s: center %s (k=%d , Δt=%d sec)",
            getName(),
            getCenter().getSimpleAddress(),
            getSize(),
            getDuration()
        );
    }
}
