package src.motifMiner.patterns;

import java.util.List;

import src.graph.Edge;
import src.graph.EdgeNFT;

public class ReceiveAndForwardNFT extends ReceiveAndForward {

    @Override
    public String getName() { return "ReceiveAndForwardNFT";}

    public ReceiveAndForwardNFT(List<Edge> edges) {
        super(edges);
    }

    public String getNft() {
        if (edges.isEmpty()) {
            return null;
        }
        Edge firstEdge = edges.get(0);
        if (firstEdge instanceof EdgeNFT) {
            return ((EdgeNFT) firstEdge).getNftId();
        }
        return null;
    }

    @Override
    public void validate() throws PatternValidationException {
        System.err.println("incoming ... da controllare che tutti gli edge siano EdgeNFT e che abbiano lo stesso NFT ID");
        super.validate();

    }

    @Override
    public String toString() {
        return String.format(
            "%s: %s → [%s] → %s nft:%s (k=%d , Δt=%d sec)",
            getName(),
            getReceiveFromNode().getSimpleAddress(),
            getCenterNode().getSimpleAddress(),
            getForwardToNode().getSimpleAddress(),
            getNft(),
            getSize(),
            getDuration()
        );
    }
}
