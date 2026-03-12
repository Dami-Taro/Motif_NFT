package src.motifMiner.patterns;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import src.graph.Edge;
import src.graph.UserNode;

public class InStar extends Pattern {

    @Override
    public String getName() { return "InStar";}

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
    public void validate() throws PatternValidationException {
        if (edges.size() < 2) {
            throw new PatternValidationException( getName() + " must contain at least 2 edges");
        }

        UserNode center = edges.get(0).getTo();

        for (Edge e : edges) {
            if (!e.getTo().equals(center)) {
                throw new PatternValidationException( getName() + ": All edges must point to the same center node");
            }
        }
    }

    @Override
    public String toString() {
        return String.format(
            "%s: center %s (k=%d , Δt=%d sec)",
            getName(),
            getCenter().getAddress(),
            getSize(),
            getDuration()
        );
    }
}

