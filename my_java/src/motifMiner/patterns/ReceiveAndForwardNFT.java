package src.motifMiner.patterns;

import java.util.List;

import src.graph.Edge;
import src.graph.UserNode;

public class ReceiveAndForwardNFT extends ReceiveAndForward {

    public ReceiveAndForwardNFT(List<Edge> edges) {
        super(edges);
    }

    @Override
    public void validate() throws PatternValidationException {
        System.err.println("incoming ...");
        if (edges.size() < 2) {
            throw new PatternValidationException(
                "ReceiveAndForwardNFT must contain at least 2 edges"
            );
        }
        if (edges.size() % 2 != 0) {
            throw new PatternValidationException(
                "ReceiveAndForwardNFT must contain an even number of edges"
            );
        }

        UserNode center = getCenterNode();

        for (int i = 0; i < edges.size(); i++) {
            Edge e = edges.get(i);

            if (i % 2 == 0) {
                // acquisto: Y → center
                if (!e.getTo().equals(center)) {
                    throw new PatternValidationException(
                        "Expected buying edge at position " + i
                    );
                }
            } else {
                // vendita: center → X
                if (!e.getFrom().equals(center)) {
                    throw new PatternValidationException(
                        "Expected selling edge at position " + i
                    );
                }
            }
        }
    }

    @Override
    public String toString() {
        return String.format(
            "ReceiveAndForwardNFT: %s → [%s] → %s (k=%d , Δt=%d sec)",
            getReceiveFromNode().getSimpleAddress(),
            getCenterNode().getSimpleAddress(),
            getForwardToNode().getSimpleAddress(),
            getSize(),
            getDuration()
        );
    }
}
