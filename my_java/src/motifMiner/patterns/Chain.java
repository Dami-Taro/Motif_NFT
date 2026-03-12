package src.motifMiner.patterns;

import java.util.Collections;
import java.util.List;

import src.graph.Edge;
import src.graph.EdgeNFT;
import src.graph.UserNode;

/**
 * Pattern: Chain
 *
 * Sequenza temporale di archi A→B, B→C, C→D, ...
 * Tutti gli archi sono ordinati temporalmente.
 */
public class Chain extends Pattern {

    public Chain(List<Edge> edges) {
        super(edges);
        validateChain();
    }

    
    //Verifica che gli archi formino una catena valida: to(i) == from(i+1)
    public void validateChain() {
        if (edges.size() < 2) {
            throw new IllegalArgumentException("Chain must contain at least 2 edges");
        }

        for (int i = 0; i < edges.size() - 1; i++) {
            UserNode currentTo = edges.get(i).getTo();
            UserNode nextFrom = edges.get(i + 1).getFrom();

            if (!currentTo.equals(nextFrom)) {
                throw new IllegalArgumentException(
                    "Invalid Chain: edge " + i +
                    " destination != edge " + (i + 1) + " source"
                );
            }
        }
    }

    public void validateChainNFT() {
        if (edges.size() < 2) {
            throw new IllegalArgumentException("Chain must contain at least 2 edges");
        }
        
        if (!(edges.get(0) instanceof EdgeNFT)) {
            throw new IllegalArgumentException("ChainNFT requires EdgeNFT edges only");
        }

        String nft= ((EdgeNFT) edges.get(0)).getNftId();

        for (int i = 0; i < edges.size() - 1; i++) {
            UserNode currentTo = edges.get(i).getTo();
            UserNode nextFrom = edges.get(i + 1).getFrom();
            String currentNft= ((EdgeNFT) edges.get(i)).getNftId();

            if (!currentNft.equals(nft)) {
                throw new IllegalArgumentException(
                    "Invalid ChainNFT: edge " + i +
                    " nft " + currentNft +
                    " != nft " + nft
                );
            }

            if (!currentTo.equals(nextFrom)) {
                throw new IllegalArgumentException(
                    "Invalid Chain: edge " + i +
                    " destination != edge " + (i + 1) + " source"
                );
            }
        }
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

    public UserNode getFirstNode() {
        return edges.isEmpty() ? null : edges.get(0).getFrom();
    }
    public UserNode getLastNode() {
        return edges.isEmpty() ? null : edges.get(edges.size() - 1).getTo();
    }

    @Override
    public String toString() {
        return String.format(
            "Chain: %s → ... → %s (k=%d , Δt=%d sec)",
            getFirstNode().getAddress().substring(0, 8),
            getLastNode().getAddress().substring(0, 8),
            getSize(),
            getDuration()
        );
    }
}
