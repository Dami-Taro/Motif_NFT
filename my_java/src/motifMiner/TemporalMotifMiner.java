package src.motifMiner;

import java.util.*;

import src.graph.Edge;
import src.graph.EdgeNFT;
import src.graph.Graph;
import src.graph.MergedIterator;
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

    // ============ DISTINCT IN-STAR PATTERN ============
    
    /**
     * Pattern: DistinctInStar (A→X, B→X, C→X, ...)
     * Motif base: due archi A→X, B→X (A ≠ B)
     * Regola incrementale: aggiungere archi da nuovi nodi verso X
     * 
     * Significato: utente X che colleziona NFT da più utenti diversi
     */
    public List<DistinctInStar> findDistinctInStars(int minSize) {
        List<DistinctInStar> allPatterns = new ArrayList<>();

        for (UserNode nodeX : graph.getNodes()) {
            List<Edge> incomingEdges =
                new ArrayList<>(nodeX.getIncomingEdges());

            if (incomingEdges.size() < minSize) continue;

            incomingEdges.sort(Comparator.comparingLong(Edge::getTimestamp));

            findValidDistinctInStarSubsets(incomingEdges, minSize, allPatterns);
        }

        return allPatterns;
    }

    private void findValidDistinctInStarSubsets(List<Edge> edges, int minSize, 
                                        List<DistinctInStar> results) {
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
                results.add(new DistinctInStar(new ArrayList<>(currentPattern)));
            }
        }
    }
    
    // ============ MERGED IN-STAR PATTERN ============

    /**
     * Pattern: Consecutive InStar (A→X, B→X, C→X, ...)
     * Motif base: due archi A→X, B→X
     * Regola incrementale: aggiungere archi verso X
     * 
     * Significato: utente X che colleziona NFT in un intervallo di tempo ristretto
     */
    public List<InStar> findMergedInStars(int minSize) {

        List<InStar> results = new ArrayList<>();

        for (UserNode center : graph.getNodes()) {

            MergedIterator mergedIt = new MergedIterator(
                center.getOutgoingEdges(),
                center.getIncomingEdges()
            );

            findValidMergedInStarSubsets(
                center,
                mergedIt,
                minSize,
                results
            );
        }

        return results;
    }

    private void findValidMergedInStarSubsets(
            UserNode center,
            MergedIterator mergedIt,
            int minSize,
            List<InStar> results) {

        List<Edge> currentPattern = new ArrayList<>();
        long startTime = -1;
        long endTime = -1;

        while (mergedIt.hasNext()) {

            Edge edge = mergedIt.next();

            // 1) FINE PER DELTA
            if ( !currentPattern.isEmpty() && edge.getTimestamp() > endTime ) {

                if (currentPattern.size() >= minSize) {
                    results.add(new InStar(new ArrayList<>(currentPattern)));
                }

                currentPattern.clear();
                startTime = -1;
                endTime = -1;
                // NOT continue → l’arco va rivalutato
            }

            // 2) FINE PER ARCO NON INCREMENTALE
            if ( !edge.getTo().equals(center) ) {

                if ( !currentPattern.isEmpty() ) {
                    if (currentPattern.size() >= minSize) {
                        results.add(new InStar(new ArrayList<>(currentPattern)));
                    }
                    currentPattern.clear();
                    startTime = -1;
                    endTime = -1;
                }

                continue;
            }

            // 3) ARCO INCREMENTALE
            if (edge.getTo().equals(center)) {

                if (currentPattern.isEmpty()) {
                    startTime = edge.getTimestamp();
                    endTime = startTime + delta;
                }

                currentPattern.add(edge);
                continue;
            }

            System.err.println("Unexpected case in findValidMergedInStarSubsets");
        }

        // EVENTUALE PATTERN FINALE
        if (currentPattern.size() >= minSize) {
            results.add(new InStar(new ArrayList<>(currentPattern)));
        }
        currentPattern.clear();
        startTime = -1;
        endTime = -1;
        
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


    // ============ MERGED GIVE AND TAKE PATTERN ============
    /**
     * Pattern: GiveAndTake (B→C, A→B, B→D, E→B, ...) con algoritmo merged
     * Motif base: due archi B→C, A→B (B vende poi compra)
     * Regola incrementale: aggiungere altri archi alternando vendite (B→X) e acquisti (Y→B)
     */
    public List<GiveAndTake> findMergedGiveAndTakes(int minSize) {

        List<GiveAndTake> results = new ArrayList<>();

        for (UserNode center : graph.getNodes()) {

            MergedIterator mergedIt = new MergedIterator(
                center.getOutgoingEdges(),
                center.getIncomingEdges()
            );

            findMergedGiveAndTakePatterns(
                center,
                mergedIt,
                minSize,
                results
            );
        }

        return results;
    }
  
    private void findMergedGiveAndTakePatterns(
            UserNode center,
            MergedIterator mergedIt,
            int minSize,
            List<GiveAndTake> results) {

        List<Edge> currentPattern = new ArrayList<>();

        boolean expectingOutgoing = true; // il primo deve essere vendita
        long startTime = -1;
        long endTime = -1;

        while (mergedIt.hasNext()) {

            Edge edge = mergedIt.next();

            // 1) FINE PER DELTA
            if ( !currentPattern.isEmpty() && edge.getTimestamp() > endTime ) {

                if (!expectingOutgoing) {
                    currentPattern.remove(currentPattern.size() - 1);
                }
                if (currentPattern.size() >= minSize) {
                    results.add(new GiveAndTake(new ArrayList<>(currentPattern)));
                }

                currentPattern.clear();
                startTime = -1;
                endTime = -1;
                expectingOutgoing = true;
                // NOT continue → l’arco va rivalutato
            }

            // 2) FINE PER ARCO NON INCREMENTALE
            if(  (expectingOutgoing && !edge.getFrom().equals(center))  || 
                 (!expectingOutgoing && !edge.getTo().equals(center)) ) {

                if(currentPattern.isEmpty()) {
                    continue;
                }

                if (!expectingOutgoing) {
                    currentPattern.remove(currentPattern.size() - 1);
                }

                if (currentPattern.size() >= minSize) {
                    results.add(new GiveAndTake(new ArrayList<>(currentPattern)));
                }
                currentPattern.clear();
                startTime = -1;
                endTime = -1;
                expectingOutgoing = true;

                continue;
            }

            // 3) ARCO INCREMENTALE
            if( (expectingOutgoing && edge.getFrom().equals(center))  || 
                (!expectingOutgoing && edge.getTo().equals(center)) ) {

                if (currentPattern.isEmpty()) {
                    startTime = edge.getTimestamp();
                    endTime = startTime + delta;
                }

                currentPattern.add(edge);
                expectingOutgoing = !expectingOutgoing;

                continue;
            }

            System.err.println("Unexpected case in findMergedGiveAndTakePatterns");

        }
        // EVENTUALE PATTERN FINALE
        if (!currentPattern.isEmpty() && !expectingOutgoing) {
            currentPattern.remove(currentPattern.size() - 1);
        }
        if (currentPattern.size() >= minSize) {
            results.add(new GiveAndTake(new ArrayList<>(currentPattern)));
        }
    }

    // ============ MERGED RECEIVE AND FORWARD NFT PATTERN ============
    /**
     * Pattern: ReceiveAndForward (A→B, B→C, D→B, B→E, ...) con algoritmo merged
     * Motif base: due archi B→C, A→B (B vende poi compra)
     * Regola incrementale: aggiungere altri archi alternando acquisti (Y→B) e vendite (B→X) dello stesso NFT
     */
    public List<ReceiveAndForward> findMergedReceiveAndForward(int minSize) {

        List<ReceiveAndForward> results = new ArrayList<>();

        for (UserNode center : graph.getNodes()) {

            MergedIterator mergedIt = new MergedIterator(
                center.getOutgoingEdges(),
                center.getIncomingEdges()
            );

            findMergedReceiveAndForwardPatterns(
                center,
                mergedIt,
                minSize,
                results
            );
        }

        return results;
    }
  
    private void findMergedReceiveAndForwardPatterns(
            UserNode center,
            MergedIterator mergedIt,
            int minSize,
            List<ReceiveAndForward> results) {

        List<Edge> currentPattern = new ArrayList<>();

        boolean expectingIncoming = true; // il primo deve essere vendita
        long startTime = -1;
        long endTime = -1;

        while (mergedIt.hasNext()) {

            Edge edge = mergedIt.next();

            // 1) FINE PER DELTA
            if ( !currentPattern.isEmpty() && edge.getTimestamp() > endTime ) {

                if (!expectingIncoming) {
                    currentPattern.remove(currentPattern.size() - 1);
                }
                if (currentPattern.size() >= minSize) {
                    results.add(new ReceiveAndForward(new ArrayList<>(currentPattern)));
                }

                currentPattern.clear();
                startTime = -1;
                endTime = -1;
                expectingIncoming = true;
                // NOT continue → l’arco va rivalutato
            }

            // 2) FINE PER ARCO NON INCREMENTALE
            if(  (expectingIncoming && !edge.getTo().equals(center))  || 
                 (!expectingIncoming && !edge.getFrom().equals(center)) ) {

                if(currentPattern.isEmpty()) {
                    continue;
                }

                if (!expectingIncoming) {
                    currentPattern.remove(currentPattern.size() - 1);
                }

                if (currentPattern.size() >= minSize) {
                    results.add(new ReceiveAndForward(new ArrayList<>(currentPattern)));
                }
                currentPattern.clear();
                startTime = -1;
                endTime = -1;
                expectingIncoming = true;

                continue;
            }

            // 3) ARCO INCREMENTALE
            if( (expectingIncoming && edge.getTo().equals(center))  || 
                (!expectingIncoming && edge.getFrom().equals(center)) ) {

                if (currentPattern.isEmpty()) {
                    startTime = edge.getTimestamp();
                    endTime = startTime + delta;
                }

                currentPattern.add(edge);
                expectingIncoming = !expectingIncoming;

                continue;
            }

            System.err.println("Unexpected case in findValidMergedReceiveAndForwardPatterns");

        }
        // EVENTUALE PATTERN FINALE
        if (!currentPattern.isEmpty() && !expectingIncoming) {
            currentPattern.remove(currentPattern.size() - 1);
        }
        if (currentPattern.size() >= minSize) {
            results.add(new ReceiveAndForward(new ArrayList<>(currentPattern)));
        }
    }


    // ============ MERGED RECEIVE AND FORWARD NFT PATTERN ============
    /**
     * Pattern: ReceiveAndForward (A→B, B→C, D→B, B→E, ...) con algoritmo merged
     * Motif base: due archi B→C, A→B (B vende poi compra)
     * Regola incrementale: aggiungere altri archi alternando acquisti (Y→B) e vendite (B→X) dello stesso NFT
     */
    public List<ReceiveAndForwardNFT> findMergedReceiveAndForwardNFT(int minSize, List<ReceiveAndForward> receiveAndForwardPatterns) {
        List<ReceiveAndForwardNFT> results = new ArrayList<>();

        List<Edge> currentPattern;

        for (ReceiveAndForward pattern : receiveAndForwardPatterns) {

            if ( !( (pattern.getEdges().get(0)) instanceof EdgeNFT ) ) continue;
            
            currentPattern = new ArrayList<>();
            List<Edge> edges = pattern.getEdges();            

            for (int i = 0; i < edges.size(); i+=2) {
                String nftReceived = ( (EdgeNFT) edges.get(i) ).getNftId();
                String nftForwarded = ( (EdgeNFT) edges.get(i+1) ).getNftId();

                if( nftReceived.equals(nftForwarded) ){
                    currentPattern.add(edges.get(i));   // received
                    currentPattern.add(edges.get(i+1)); // forwarded
                }
            }

            if(currentPattern.size()>=minSize) results.add( new ReceiveAndForwardNFT(currentPattern) );

        }

        return results;

    }
    

}