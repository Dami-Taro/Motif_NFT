package src.motifMiner.patterns;

import java.util.List;

import src.graph.Edge;

public class ConsecutiveInStar extends DistinctInStar {

    @Override
    public String getName() { return "ConsecutiveInStar";}
    private final Edge firstSell;
    private final Edge lastSell;

    public ConsecutiveInStar(List<Edge> edges, Edge firstSell, Edge lastSell) {
        super(edges);

        if (firstSell == null || lastSell == null) {
            throw new IllegalArgumentException(getName() + "'s firstSell and lastSell cannot be null");
        }

        this.firstSell = firstSell;
        this.lastSell = lastSell;
    }

    public Edge getFirstSell() {
        return firstSell;
    }

    public Edge getLastSell() {
        return lastSell;
    }

    @Override
    public String toString() {
        return String.format(
            "%s: center %s (k=%d , Δt=%d sec)",
            getName(),
            getCenter().getSimpleAddress(),
            getSize(),
            getDuration()
        );
    }
}