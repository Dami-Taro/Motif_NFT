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
 * etichettati con lo stesso NFT
 */
public class Chain extends Pattern {

    public Chain(List<Edge> edges) {
        super(edges);
    }

    public String getNFT() {
        if (edges.isEmpty()) return null;
        if( !(edges.get(0) instanceof EdgeNFT)) return null;
        return ((EdgeNFT) edges.get(0)).getNftId();
    }

    @Override
    public void validate() throws PatternValidationException {
        if (edges.size() < 2) {
            throw new PatternValidationException("Chain must contain at least 2 edges");
        }
        
        for( Edge e : edges ) {
            if (!(e instanceof EdgeNFT)) {
                throw new PatternValidationException("Chain requires all edges to be EdgeNFT");
            }
        }

        for (int i = 0; i < edges.size() - 1; i++) {
            if (!edges.get(i).getTo().equals(edges.get(i + 1).getFrom())) {
                throw new PatternValidationException( "Chain's adjacent nodes don't match at position " + i);
            }
        }

        String nft= getNFT();

        for (int i = 0; i < edges.size() - 1; i++) {
            String currentNft= ((EdgeNFT) edges.get(i)).getNftId();
            if (!currentNft.equals(nft)) {
                throw new PatternValidationException("Chain's edges have different NFT IDs at position " + i);
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
            getFirstNode().getSimpleAddress(),
            getLastNode().getSimpleAddress(),
            getSize(),
            getDuration()
        );
    }
}
