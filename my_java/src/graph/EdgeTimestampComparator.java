package src.graph;

import java.util.Comparator;

public class EdgeTimestampComparator implements Comparator<Edge> {

    @Override
    public int compare(Edge e1, Edge e2) {

        int cmp = Long.compare(
                e1.getTimestamp(),
                e2.getTimestamp()
        );

        if (cmp != 0) return cmp;

        // tie-breaker deterministico
        return Long.compare(e2.getId(), e1.getId());
    }
}