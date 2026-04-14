package src.motifMiner.patterns;

import src.graph.Edge;

public class NoAnomalySameNFTChain extends SameNFTChain {

    @Override
    public String getName() { return "NoAnomalySameNFTChain"; }

    public NoAnomalySameNFTChain(SameNFTChain chain) {
        super(chain.getEdges(), chain.getNft());
    }

    @Override
    public void validate() throws PatternValidationException {

        super.validate();

        for (int i = 0; i < edges.size() - 1; i++) {

            Edge current = edges.get(i);
            Edge next = edges.get(i + 1);

            if (!current.getTo().equals(next.getFrom())) {
                throw new PatternValidationException( getName() + ": anomaly at position " + i + ": " + current.getTo().getSimpleAddress() + " != " + next.getFrom().getSimpleAddress() );
            }
        }
    }
}