package src.motifMiner;

import java.util.*;

import src.graph.Edge;
import src.graph.Graph;
import src.graph.UserNode;
import src.motifMiner.patterns.*;

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
    public List<OneWayCouple> findOneWayCouples(int minSize) {
        List<OneWayCouple> allPatterns = new ArrayList<>();

        for (UserNode nodeA : graph.getNodes()) {
            Map<UserNode, List<Edge>> edgesByTarget = new HashMap<>();

            for (Edge edge : nodeA.getOutgoingEdges()) {
                UserNode target = edge.getTo();
                edgesByTarget
                    .computeIfAbsent(target, k -> new ArrayList<>())
                    .add(edge);
            }

            for (List<Edge> edges : edgesByTarget.values()) {
                if (edges.size() < minSize) continue;

                edges.sort(Comparator.comparingLong(Edge::getTimestamp));

                findValidOneWayCoupleSubsets(
                    edges,
                    minSize,
                    allPatterns,
                    OneWayCouple::new
                );
            }
        }

        return allPatterns;
    }
    
    private <T> void findValidOneWayCoupleSubsets(List<Edge> edges, int minSize,
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
            }
            if (validSubset.size() >= minSize) {
                results.add(constructor.apply(new ArrayList<>(validSubset)));
            }
        }
    }

    // ============ IN-STAR PATTERN ============
    
    /**
     * Pattern: InStar (A→X, B→X, C→X, ...)
     * Motif base: due archi A→X, B→X (A ≠ B)
     * Regola incrementale: aggiungere archi da nuovi nodi verso X
     * 
     * Significato: utente X che colleziona NFT da più utenti diversi
     */
    public List<InStar> findInStars(int minSize) {
        List<InStar> allPatterns = new ArrayList<>();

        for (UserNode nodeX : graph.getNodes()) {
            List<Edge> incomingEdges =
                new ArrayList<>(nodeX.getIncomingEdges());

            if (incomingEdges.size() < minSize) continue;

            incomingEdges.sort(Comparator.comparingLong(Edge::getTimestamp));

            findValidInStarSubsets(incomingEdges, minSize, allPatterns);
        }

        return allPatterns;
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
            }
            // Se abbiamo raggiunto la dimensione minima, salva il pattern
            if (currentPattern.size() >= minSize) {
                results.add(new InStar(new ArrayList<>(currentPattern)));
            }
        }
    }
    
   



}