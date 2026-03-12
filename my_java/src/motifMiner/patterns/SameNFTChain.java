package src.motifMiner.patterns;

import java.util.List;

import src.graph.Edge;
import src.graph.EdgeNFT;

/**
 * Pattern: SameNFTChain
 *
 * Sequenza temporale di archi A→B, B→C, C→D, ...
 * etichettati con lo stesso NFT
 */
public class SameNFTChain extends Chain {

    @Override
    public String getName() { return "SameNFTChain";}
    private final String nft;

    public SameNFTChain(List<Edge> edges, String nft) {
        super(edges);
        this.nft=nft;
    }

    public String getNft() {
        return this.nft;
    }

    @Override
    public void validate() throws PatternValidationException {

        super.validate();
        
        for( Edge e : edges ) {
            if ( !(e instanceof EdgeNFT) ) {
                throw new PatternValidationException(getName() + " requires all edges to be EdgeNFT");
            }
        }

        for (int i = 0; i < edges.size() - 1; i++) {
            String currentNft= ((EdgeNFT) edges.get(i)).getNftId();
            if ( !currentNft.equals( this.getNft() ) ) {
                throw new PatternValidationException(getName() + ": edges have different NFT IDs at position " + i);
            }
        }
    }

    @Override
    public String toString() {
        return String.format(
            "%s: %s → ... → %s nft:%s (k=%d , Δt=%d sec)",
            getName(),
            getFirstNode().getSimpleAddress(),
            getLastNode().getSimpleAddress(),
            getNft(),
            getSize(),
            getDuration()
        );
    }
}
