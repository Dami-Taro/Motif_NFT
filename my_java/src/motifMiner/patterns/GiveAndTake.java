package src.motifMiner.patterns;

import java.util.List;

import src.graph.Edge;
import src.graph.UserNode;

public class GiveAndTake extends Pattern {

    @Override
    public String getName() { return "GiveAndTake";}

    public GiveAndTake(List<Edge> edges) {
        super(edges);
    }

    public UserNode getToNode() {
        return getEdges().isEmpty() ? null : getEdges().get(0).getTo();
    }

    public UserNode getCenterNode() {
        return getEdges().isEmpty() ? null : getEdges().get(1).getTo();
    }
    
    public UserNode getFromNode() {
        return getEdges().isEmpty() ? null : getEdges().get(1).getFrom();
    }

    @Override
    public void validate() throws PatternValidationException {
        if (edges.size() < 2) {
            throw new PatternValidationException( getName() + " must contain at least 2 edges");
        }
        if (edges.size() % 2 != 0) {
            throw new PatternValidationException( getName() + " must contain an even number of edges");
        }

        UserNode center = getCenterNode();

        for (int i = 0; i < edges.size(); i++) {
            Edge e = edges.get(i);

            if (i % 2 == 0) {
                // vendita: center → X
                if (!e.getFrom().equals(center)) {
                    throw new PatternValidationException( getName() + ": Expected selling edge at position " + i );
                }
            } else {
                // acquisto: Y → center
                if (!e.getTo().equals(center)) {
                    throw new PatternValidationException( getName() + ": Expected buying edge at position " + i );
                }
            }
        }
    }

    @Override
    public String toString() {
        return String.format(
            "%s:  %s → [%s] → %s (k=%d , Δt=%d sec)",
            getName(),
            getFromNode().getSimpleAddress(),
            getCenterNode().getSimpleAddress(),
            getToNode().getSimpleAddress(),
            getSize(),
            getDuration()
        );
    }
}
