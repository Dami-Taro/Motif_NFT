package src.motifMiner.patterns;

import java.util.List;

import src.graph.Edge;

public class ConsecutiveOutStar extends OutStar {

    private final Edge firstBuy;
    private final Edge lastBuy;

    public ConsecutiveOutStar( List<Edge> edges, Edge firstBuy, Edge lastBuy) {
        super(edges);

        if (firstBuy == null || lastBuy == null) {
            throw new IllegalArgumentException("firstBuy and lastBuy cannot be null");
        }

        this.firstBuy = firstBuy;
        this.lastBuy = lastBuy;
    }

    public Edge getFirstBuy() {
        return firstBuy;
    }

    public Edge getLastBuy() {
        return lastBuy;
    }

    @Override
    public String toString() {
        return String.format(
            "ConsecutiveOutStar: center %s (k=%d , Δt=%d sec)",
            getCenter().getAddress().substring(0, 8),
            getSize(),
            getDuration()
        );
    }
}
