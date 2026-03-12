package src.motifMiner.patterns;

import java.util.List;

import src.graph.Edge;
import src.graph.UserNode;

public class ReceiveAndForward extends Pattern {

    public ReceiveAndForward(List<Edge> edges) {
        super(edges);
    }

    public UserNode getReceiveFromNode() {
        return edges.get(0).getFrom();
    }

    public UserNode getCenterNode() {
        return edges.get(0).getTo();
    }

    public UserNode getForwardToNode() {
        return edges.get(edges.size() - 1).getTo();
    }

    @Override
    public String toString() {
        return String.format(
            "ReceiveAndForward: %s → [%s] → %s (k=%d , Δt=%d sec)",
            getReceiveFromNode(),
            getCenterNode(),
            getForwardToNode(),
            getSize(),
            getDuration()
        );
    }
}
