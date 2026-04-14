package src.motifMiner.patterns;

import src.graph.Edge;

public class NoAnomalySameNFTCycle extends SameNFTCycle {

    @Override
    public String getName() { return "NoAnomalySameNFTCycle";}

    public NoAnomalySameNFTCycle(SameNFTCycle cycle) {
        super(cycle.getEdges(), cycle.getNft());
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
