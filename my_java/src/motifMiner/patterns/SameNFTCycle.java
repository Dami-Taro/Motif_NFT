package src.motifMiner.patterns;

import java.util.List;

import src.graph.Edge;

/**
 * Pattern: SameNFTChain
 *
 * Sequenza temporale di archi A→B, B→C, C→D, ...
 * etichettati con lo stesso NFT
 */
public class SameNFTCycle extends SameNFTChain {

    @Override
    public String getName() { return "SameNFTCycle";}

    public SameNFTCycle(List<Edge> edges, String nft) {
        super(edges,nft);
    }

    @Override
    public void validate() throws PatternValidationException {

        super.validate();
        
        if ( !this.getFirstNode().equals(this.getLastNode()) ){
            throw new PatternValidationException("a " + getName() + " requires first and last nodes to be the same");
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
