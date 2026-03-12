package src.motifMiner.patterns;

import java.util.List;

import src.graph.Edge;

/**
 * Classe astratta base per tutti i pattern temporali.
 * Incapsula la lista di archi e la logica comune.
 */
public abstract class Pattern {

    protected final List<Edge> edges;

    protected Pattern(List<Edge> edges) {
        this.edges = edges;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public int getSize() {
        return edges.size();
    }

    public long getDuration() {
        if (edges.size() < 2) return 0;
        return edges.get(edges.size() - 1).getTimestamp()
             - edges.get(0).getTimestamp();
    }

    public String toString(){
        return String.format("Pattern (k=%d , Δt=%d sec)", getSize(), getDuration());
    }
    public abstract void validate() throws PatternValidationException;
}
