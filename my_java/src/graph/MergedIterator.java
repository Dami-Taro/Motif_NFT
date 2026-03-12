package src.graph;

import java.util.Iterator;
import java.util.NavigableSet;
import java.util.NoSuchElementException;

public class MergedIterator implements Iterator<Edge> {

    private final Iterator<Edge> outgoingIterator;
    private final Iterator<Edge> incomingIterator;

    private Edge nextOutgoing;
    private Edge nextIncoming;

    public MergedIterator(NavigableSet<Edge> outgoingEdges,
                          NavigableSet<Edge> incomingEdges) {

        this.outgoingIterator = outgoingEdges.iterator();
        this.incomingIterator = incomingEdges.iterator();

        // inizializza i "peek"
        if (outgoingIterator.hasNext()) {
            nextOutgoing = outgoingIterator.next();
        }

        if (incomingIterator.hasNext()) {
            nextIncoming = incomingIterator.next();
        }
    }

    @Override
    public boolean hasNext() {
        return nextOutgoing != null || nextIncoming != null;
    }

    @Override
    public Edge next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more edges in MergedIterator");
        }

        Edge result;

        // se uno dei due è finito, prendi l'altro
        if (nextOutgoing == null) {
            result = nextIncoming;
            advanceIncoming();
            return result;
        }

        if (nextIncoming == null) {
            result = nextOutgoing;
            advanceOutgoing();
            return result;
        }

        // entrambi presenti → confronta timestamp
        if (nextOutgoing.getTimestamp() <= nextIncoming.getTimestamp()) {
            result = nextOutgoing;
            advanceOutgoing();
        } else {
            result = nextIncoming;
            advanceIncoming();
        }

        return result;
    }

    private void advanceOutgoing() {
        if (outgoingIterator.hasNext()) {
            nextOutgoing = outgoingIterator.next();
        } else {
            nextOutgoing = null;
        }
    }

    private void advanceIncoming() {
        if (incomingIterator.hasNext()) {
            nextIncoming = incomingIterator.next();
        } else {
            nextIncoming = null;
        }
    }
}
