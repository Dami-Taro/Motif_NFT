package src.motifMiner.patterns;

import java.util.Collections;
import java.util.List;

import src.graph.Edge;
import src.graph.UserNode;

/**
 * Pattern: Chain
 *
 * Sequenza temporale di archi A→B, B→C, C→D, ...
 */
public class Chain extends Pattern {

    @Override
    public String getName() { return "Chain";}
    
    public Chain(List<Edge> edges) {
        super(edges);
    }

    public UserNode getFirstNode() {
        return edges.isEmpty() ? null : edges.get(0).getFrom();
    }

    public UserNode getLastNode() {
        return edges.isEmpty() ? null : edges.get(edges.size() - 1).getTo();
    }

    public List<UserNode> getNodeSequence() {
        if (edges.isEmpty()) return Collections.emptyList();

        List<UserNode> nodes = new java.util.ArrayList<>();
        nodes.add(edges.get(0).getFrom());

        for (Edge e : edges) {
            nodes.add(e.getTo());
        }
        return nodes;
    }

    @Override
    public void validate() throws PatternValidationException {
        if (edges.size() <= 0) {
            throw new PatternValidationException("this " + getName() + " is empty");
        }

        for (int i = 0; i < edges.size() - 1; i++) {
            if (!edges.get(i).getTo().equals(edges.get(i + 1).getFrom())) {
                throw new PatternValidationException( "The " + getName() + "'s adjacent nodes don't match at position " + i);
            }
        }

    }

    @Override
    public String toString() {
        return String.format(
            "%s: %s → ... → %s (k=%d , Δt=%d sec)",
            getName(),
            getFirstNode().getSimpleAddress(),
            getLastNode().getSimpleAddress(),
            getSize(),
            getDuration()
        );
    }
}
