package graph;

import java.util.*;

/**
 * Classe per il mining di pattern temporali incrementali.
 * Ogni pattern è definito da un motif di base e una regola incrementale.
 * Vincolo: tutti gli archi del pattern devono avere timestamp in [t1, t1+delta].
 */
public class TemporalMotifMiner {
    
    private final Graph graph;
    private final long delta; // Finestra temporale massima (in secondi)
    
    public TemporalMotifMiner(Graph graph, long delta) {
        this.graph = graph;
        this.delta = delta;
    }
    
    // ============ ONE WAY COUPLE PATTERN ============
    
    /**
     * Pattern: One Way Couple (A→B, A→B, ...)
     * Motif base: due archi A→B
     * Regola incrementale: aggiungere altri archi A→B
     * 
     * Significato: utente A che acquista ripetutamente NFT da B
     */
    public PatternResult<OneWayCouple> findOneWayCouples(int minSize) {
        List<OneWayCouple> allPatterns = new ArrayList<>();
        
        for (UserNode nodeA : graph.getNodes()) {
            // Raggruppa archi per destinazione
            Map<UserNode, List<Edge>> edgesByTarget = new HashMap<>();
            
            for (Edge edge : nodeA.getOutgoingEdges()) {
                UserNode target = edge.getTo();
                edgesByTarget.computeIfAbsent(target, k -> new ArrayList<>()).add(edge);
            }
            
            // Per ogni coppia (A, B), verifica se ci sono abbastanza archi
            for (Map.Entry<UserNode, List<Edge>> entry : edgesByTarget.entrySet()) {
                List<Edge> edges = entry.getValue();
                
                if (edges.size() < minSize) continue;
                
                // Ordina per timestamp
                edges.sort(Comparator.comparingLong(Edge::getTimestamp));
                
                // Trova tutti i sottoinsiemi che rispettano il vincolo delta
                findValidSubsets(edges, minSize, allPatterns, 
                    (validEdges) -> new OneWayCouple(validEdges));
            }
        }
        
        return groupBySize(allPatterns, OneWayCouple::getSize);
    }
    
    // ============ IN-STAR PATTERN ============
    
    /**
     * Pattern: InStar (A→X, B→X, C→X, ...)
     * Motif base: due archi A→X, B→X (A ≠ B)
     * Regola incrementale: aggiungere archi da nuovi nodi verso X
     * 
     * Significato: utente X che colleziona NFT da più utenti diversi
     */
    public PatternResult<InStar> findInStars(int minSize) {
        List<InStar> allPatterns = new ArrayList<>();
        
        for (UserNode nodeX : graph.getNodes()) {
            List<Edge> incomingEdges = new ArrayList<>(nodeX.getIncomingEdges());
            
            if (incomingEdges.size() < minSize) continue;
            
            // Ordina per timestamp
            incomingEdges.sort(Comparator.comparingLong(Edge::getTimestamp));
            
            // Trova tutti i sottoinsiemi validi con nodi sorgente distinti
            findValidInStarSubsets(incomingEdges, minSize, allPatterns);
        }
        
        return groupBySize(allPatterns, InStar::getSize);
    }
    
    private void findValidInStarSubsets(List<Edge> edges, int minSize, 
                                        List<InStar> results) {
        // Inizia dal primo arco e costruisci incrementalmente
        for (int start = 0; start <= edges.size() - minSize; start++) {
            Edge firstEdge = edges.get(start);
            long t1 = firstEdge.getTimestamp();
            long tMax = t1 + delta;
            
            List<Edge> currentPattern = new ArrayList<>();
            Set<UserNode> usedSources = new HashSet<>();
            
            currentPattern.add(firstEdge);
            usedSources.add(firstEdge.getFrom());
            
            // Aggiungi archi incrementalmente
            for (int i = start + 1; i < edges.size(); i++) {
                Edge candidate = edges.get(i);
                
                // Verifica vincolo temporale
                if (candidate.getTimestamp() > tMax) break;
                
                // Verifica che la sorgente sia diversa
                if (usedSources.contains(candidate.getFrom())) continue;
                
                currentPattern.add(candidate);
                usedSources.add(candidate.getFrom());
                
                // Se abbiamo raggiunto la dimensione minima, salva il pattern
                if (currentPattern.size() >= minSize) {
                    results.add(new InStar(new ArrayList<>(currentPattern)));
                }
            }
        }
    }
    
    // ============ GIVE AND TAKE PATTERN ============
    
    /**
     * Pattern: GiveAndTake (B→C, A→B, B→D, E→B, ...)
     * Motif base: due archi B→C, A→B (B vende poi compra)
     * Regola incrementale: aggiungere altri archi alternando vendite (B→X) e acquisti (Y→B)
     * 
     * Significato: utente B che alterna vendite e acquisti
     * Gli archi devono alternarsi: vendita (outgoing), acquisto (incoming), vendita, acquisto, ...
     */
    public PatternResult<GiveAndTake> findGiveAndTakes(int minSize) {
        List<GiveAndTake> allPatterns = new ArrayList<>();
        
        for (UserNode nodeB : graph.getNodes()) {
            List<Edge> outgoing = new ArrayList<>(nodeB.getOutgoingEdges());
            List<Edge> incoming = new ArrayList<>(nodeB.getIncomingEdges());
            
            if (outgoing.isEmpty() || incoming.isEmpty()) continue;
            
            // Ordina per timestamp
            outgoing.sort(Comparator.comparingLong(Edge::getTimestamp));
            incoming.sort(Comparator.comparingLong(Edge::getTimestamp));
            
            // Cerca pattern alternati "vendi poi compra poi vendi..."
            findGiveAndTakePatterns(nodeB, outgoing, incoming, minSize, allPatterns);
        }
        
        return groupBySize(allPatterns, GiveAndTake::getSize);
    }
    
    private void findGiveAndTakePatterns(UserNode nodeB, 
                                         List<Edge> outgoing,
                                         List<Edge> incoming,
                                         int minSize,
                                         List<GiveAndTake> results) {
        
        // Per ogni arco uscente B→C (vendita iniziale)
        for (int sellIdx = 0; sellIdx < outgoing.size(); sellIdx++) {
            Edge firstSell = outgoing.get(sellIdx);
            long t1 = firstSell.getTimestamp();
            long tMax = t1 + delta;
            
            List<Edge> pattern = new ArrayList<>();
            pattern.add(firstSell);
            
            // Cerca pattern alternato: vendi, compra, vendi, compra, ...
            boolean lookingForBuy = true;
            long lastTimestamp = firstSell.getTimestamp();
            
            int nextSellIdx = sellIdx + 1;
            int buyIdx = 0;
            
            while (pattern.size() < minSize * 2) { // minSize conta le coppie
                Edge nextEdge = null;
                
                if (lookingForBuy) {
                    // Cerca prossimo acquisto A→B dopo lastTimestamp
                    while (buyIdx < incoming.size()) {
                        Edge candidate = incoming.get(buyIdx);
                        if (candidate.getTimestamp() > lastTimestamp && 
                            candidate.getTimestamp() <= tMax) {
                            nextEdge = candidate;
                            buyIdx++;
                            break;
                        }
                        if (candidate.getTimestamp() > tMax) break;
                        buyIdx++;
                    }
                } else {
                    // Cerca prossima vendita B→X dopo lastTimestamp
                    while (nextSellIdx < outgoing.size()) {
                        Edge candidate = outgoing.get(nextSellIdx);
                        if (candidate.getTimestamp() > lastTimestamp && 
                            candidate.getTimestamp() <= tMax) {
                            nextEdge = candidate;
                            nextSellIdx++;
                            break;
                        }
                        if (candidate.getTimestamp() > tMax) break;
                        nextSellIdx++;
                    }
                }
                
                if (nextEdge == null) break;
                
                pattern.add(nextEdge);
                lastTimestamp = nextEdge.getTimestamp();
                lookingForBuy = !lookingForBuy;
                
                // Salva il pattern se ha raggiunto la dimensione minima
                // minSize è il numero minimo di archi totali
                if (pattern.size() >= minSize) {
                    results.add(new GiveAndTake(new ArrayList<>(pattern)));
                }
            }
        }
    }
    
    // ============ METODI UTILITY ============
    
    private <T> void findValidSubsets(List<Edge> edges, int minSize,
                                      List<T> results,
                                      java.util.function.Function<List<Edge>, T> constructor) {
        
        for (int start = 0; start <= edges.size() - minSize; start++) {
            long t1 = edges.get(start).getTimestamp();
            long tMax = t1 + delta;
            
            List<Edge> validSubset = new ArrayList<>();
            
            for (int i = start; i < edges.size(); i++) {
                Edge edge = edges.get(i);
                
                if (edge.getTimestamp() > tMax) break;
                
                validSubset.add(edge);
                
                if (validSubset.size() >= minSize) {
                    results.add(constructor.apply(new ArrayList<>(validSubset)));
                }
            }
        }
    }
    
    private <T> PatternResult<T> groupBySize(List<T> patterns, 
                                              java.util.function.ToIntFunction<T> sizeExtractor) {
        Map<Integer, List<T>> bySize = new TreeMap<>();
        
        for (T pattern : patterns) {
            int size = sizeExtractor.applyAsInt(pattern);
            bySize.computeIfAbsent(size, k -> new ArrayList<>()).add(pattern);
        }
        
        return new PatternResult<>(bySize);
    }
    
    // ============ CLASSE RISULTATO ============
    
    public static class PatternResult<T> {
        private final Map<Integer, List<T>> patternsBySize;
        
        public PatternResult(Map<Integer, List<T>> patternsBySize) {
            this.patternsBySize = patternsBySize;
        }
        
        public Map<Integer, List<T>> getPatternsBySize() {
            return patternsBySize;
        }
        
        public List<T> getPatternsOfSize(int size) {
            return patternsBySize.getOrDefault(size, new ArrayList<>());
        }
        
        public int getTotalPatterns() {
            return patternsBySize.values().stream()
                .mapToInt(List::size)
                .sum();
        }
        
        public Set<Integer> getSizes() {
            return patternsBySize.keySet();
        }
        
        public List<T> getAllPatterns() {
            List<T> all = new ArrayList<>();
            for (List<T> patterns : patternsBySize.values()) {
                all.addAll(patterns);
            }
            return all;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("PatternResult{");
            for (Map.Entry<Integer, List<T>> entry : patternsBySize.entrySet()) {
                sb.append("\n  size ").append(entry.getKey())
                  .append(": ").append(entry.getValue().size())
                  .append(" pattern(s)");
            }
            sb.append("\n  Total: ").append(getTotalPatterns()).append(" patterns");
            sb.append("\n}");
            return sb.toString();
        }
    }
    
    // ============ CLASSI PER I PATTERN ============
    
    public static class OneWayCouple {
        private final List<Edge> edges;
        
        public OneWayCouple(List<Edge> edges) {
            this.edges = edges;
        }
        
        public List<Edge> getEdges() { return edges; }
        public int getSize() { return edges.size(); }
        
        public UserNode getBuyer() { 
            return edges.isEmpty() ? null : edges.get(0).getFrom(); 
        }
        
        public UserNode getSeller() { 
            return edges.isEmpty() ? null : edges.get(0).getTo(); 
        }
        
        public long getDuration() {
            if (edges.size() < 2) return 0;
            return edges.get(edges.size() - 1).getTimestamp() - 
                   edges.get(0).getTimestamp();
        }
        
        @Override
        public String toString() {
            return String.format("OneWayCouple: %s → %s (%d acquisti, Δt=%d sec)",
                getBuyer().getAddress().substring(0, 8),
                getSeller().getAddress().substring(0, 8),
                edges.size(),
                getDuration()
            );
        }
    }
    
    public static class InStar {
        private final List<Edge> edges;
        
        public InStar(List<Edge> edges) {
            this.edges = edges;
        }
        
        public List<Edge> getEdges() { return edges; }
        public int getSize() { return edges.size(); }
        
        public UserNode getCollector() { 
            return edges.isEmpty() ? null : edges.get(0).getTo(); 
        }
        
        public Set<UserNode> getSellers() {
            Set<UserNode> sellers = new HashSet<>();
            for (Edge e : edges) {
                sellers.add(e.getFrom());
            }
            return sellers;
        }
        
        public long getDuration() {
            if (edges.size() < 2) return 0;
            return edges.get(edges.size() - 1).getTimestamp() - 
                   edges.get(0).getTimestamp();
        }
        
        @Override
        public String toString() {
            return String.format("InStar: %s colleziona da %d venditori (%d NFT, Δt=%d sec)",
                getCollector().getAddress().substring(0, 8),
                getSellers().size(),
                edges.size(),
                getDuration()
            );
        }
    }
    
    public static class GiveAndTake {
        private final List<Edge> edges;
        
        public GiveAndTake(List<Edge> edges) {
            this.edges = edges;
        }
        
        public List<Edge> getEdges() { return edges; }
        public int getSize() { return edges.size(); }
        
        public UserNode getTrader() { 
            return edges.isEmpty() ? null : edges.get(0).getFrom(); 
        }
        
        public long getDuration() {
            if (edges.size() < 2) return 0;
            return edges.get(edges.size() - 1).getTimestamp() - 
                   edges.get(0).getTimestamp();
        }
        
        public int getSellCount() {
            int count = 0;
            for (int i = 0; i < edges.size(); i++) {
                if (i % 2 == 0) count++; // Indici pari sono vendite
            }
            return count;
        }
        
        public int getBuyCount() {
            int count = 0;
            for (int i = 0; i < edges.size(); i++) {
                if (i % 2 == 1) count++; // Indici dispari sono acquisti
            }
            return count;
        }
        
        @Override
        public String toString() {
            return String.format("GiveAndTake: %s (%d archi: %d vendite, %d acquisti, Δt=%d sec)",
                getTrader().getAddress().substring(0, 8),
                edges.size(),
                getSellCount(),
                getBuyCount(),
                getDuration()
            );
        }
    }
}