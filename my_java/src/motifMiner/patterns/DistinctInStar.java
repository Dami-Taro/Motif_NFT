package src.motifMiner.patterns;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import src.graph.Edge;
import src.graph.UserNode;

public class DistinctInStar extends InStar {

    public DistinctInStar(List<Edge> edges) {
        super(edges);
    }

    @Override
    public void validate() throws PatternValidationException {
        super.validate();

        Set<UserNode> sources = new HashSet<>();

        for (Edge e : edges) {

            if (!sources.add(e.getFrom())) {
                throw new PatternValidationException(
                    "Duplicate source node in DistinctInStar"
                );
            }
        }
    }

    @Override
    public String toString() {
        return String.format(
            "DistinctInStar: center %s (k=%d , Δt=%d sec)",
            getCenter().getSimpleAddress(),
            getSize(),
            getDuration()
        );
    }
}

