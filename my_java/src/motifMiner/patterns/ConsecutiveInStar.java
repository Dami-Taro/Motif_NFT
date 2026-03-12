package src.motifMiner.patterns;

import java.util.List;

import src.graph.Edge;

public class ConsecutiveInStar extends InStar {

    private final Edge firstSell;
    private final Edge lastSell;

    public ConsecutiveInStar(List<Edge> edges, Edge firstSell, Edge lastSell) {
        super(edges);

        if (firstSell == null || lastSell == null) {
            throw new IllegalArgumentException("firstSell and lastSell cannot be null");
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
            "ConsecutiveInStar: center %s (k=%d , Δt=%d sec)",
            getCenter().getAddress().substring(0, 8),
            getSize(),
            getDuration()
        );
    }
}
