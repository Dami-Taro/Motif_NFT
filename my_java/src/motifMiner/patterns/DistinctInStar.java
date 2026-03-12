package src.motifMiner.patterns;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import src.graph.Edge;
import src.graph.UserNode;

public class DistinctInStar extends InStar {

    @Override
    public String getName() { return "DistinctInStar";}

    public DistinctInStar(List<Edge> edges) {
        super(edges);
    }

    @Override
    public void validate() throws PatternValidationException {
        super.validate();

        Set<UserNode> sources = new HashSet<>();

        for (Edge e : edges) {

            if (!sources.add(e.getFrom())) {
                throw new PatternValidationException("Duplicate source node in " + getName());
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

