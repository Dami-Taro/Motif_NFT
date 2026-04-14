package src.motifMiner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
//import java.util.TreeSet;

import src.graph.DatasetNFT;
import src.graph.Edge;
//import src.graph.EdgeTimestampComparator;
import src.graph.UserNode;
import src.main.Constants;
import src.motifMiner.patterns.PatternValidationException;
import src.motifMiner.patterns.SameNFTChain;
import src.motifMiner.patterns.SameNFTCycle;
import src.motifMiner.patterns.NoAnomalySameNFTChain;
import src.motifMiner.patterns.NoAnomalySameNFTCycle;

public class NFTsTemporalMotifMiner {
    
    private final DatasetNFT datasetNFT;
    private final long delta; // Finestra temporale massima (in secondi)
    
    public NFTsTemporalMotifMiner(DatasetNFT datasetNFT, long delta) {
        this.datasetNFT = datasetNFT;
        this.delta = delta;
    }

    private boolean outOfTime(Edge firstEdge, long delta, Edge currentEdge){
        return ( currentEdge.getTimestamp() > ( firstEdge.getTimestamp() + delta ) );
    }


    // ============ SAME NFT CHAIN ============
    /**
     * Pattern: Chain (A→B, B→C, ...)
     * Motif base: due archi A→B, B→C
     * Regola incrementale: aggiungere archi C→D
     * 
     * Vincolo: tutti archi scambiano lo stesso NFT
     */
    public List<SameNFTChain> findSameNFTChains(int minSize) {
        List<SameNFTChain> results = new ArrayList<>();

        for (String currentNft : this.datasetNFT.getNFTs()) {

            NavigableSet<Edge> nftTransactions = this.datasetNFT.getNFTTransactions(currentNft);
            findSameNFTChainsPatterns(
                currentNft,
                nftTransactions,
                minSize,
                results
            );
        }

        return results;
    }

    public List<SameNFTChain> findContiguousSameNFTChains(int minSize) {
        List<SameNFTChain> results = new ArrayList<>();

        for (String currentNft : this.datasetNFT.getNFTs()) {

            List<NavigableSet<Edge>> nftTransactions = this.datasetNFT.getNftContiguousTransaction(currentNft);
            for (NavigableSet<Edge> nftContiguousTransaction : nftTransactions) {
                findSameNFTChainsPatterns(
                    currentNft,
                    nftContiguousTransaction,
                    minSize,
                    results
                );
            }
        }

        return results;
    }
  
    private void findSameNFTChainsPatterns(
            String currentNft,
            NavigableSet<Edge> nftTransactions,
            int minSize,
            List<SameNFTChain> results) {

        Iterator<Edge> edgeIterator = nftTransactions.iterator();
        if ( !edgeIterator.hasNext() ) return;

        List<Edge> currentPattern = new ArrayList<>();
        currentPattern.add(edgeIterator.next());
        /* 
        Edge firstEdge = null;
        while ( edgeIterator.hasNext() && firstEdge == null ) {
            
            firstEdge = edgeIterator.next();
            if ( Constants.isBURN(firstEdge.getFrom().getEntity()) || Constants.isBURN(firstEdge.getTo().getEntity()) ) 
                firstEdge = null; // skip se diretto da o verso burn entity (Mint o Burn)
        
        }
        if(firstEdge==null){System.out.println(currentNft + ": "+nftTransactions.first().getFrom().getSimpleAddress() + "-> null first edge\n"); return; }
        currentPattern.add( firstEdge );
        */

        while(edgeIterator.hasNext()){
            
            Edge currentEdge = edgeIterator.next();
            
            //aggiungo currentEdge a nuovo pattern
            if( outOfTime(currentPattern.get(0), delta, currentEdge) ){

                removeEntitySameNFTChainPattern(currentPattern);
                if( currentPattern.size() >= minSize )
                    results.add(new SameNFTChain( new ArrayList<>(currentPattern), currentNft));

                currentPattern.clear();
                currentPattern.add(currentEdge);
                continue;
            }

            //else aggiungo currentEdge incrementando currentPattern
            currentPattern.add(currentEdge);

        }
        removeEntitySameNFTChainPattern(currentPattern);
        if( currentPattern.size() >= minSize )
            results.add(new SameNFTChain( new ArrayList<>(currentPattern), currentNft));

    }
    private void removeEntitySameNFTChainPattern( List<Edge> patternEdges){
        if( patternEdges == null ) return;
        
        if( (!patternEdges.isEmpty()) && patternEdges.get(0).getFrom().getEntity() != null ){
            System.err.println("TROVATO UN CAZZO DI BURN ADDRESS: \""+patternEdges.get(0).getFrom().getEntity()+"\" != \"Burn Addresses\"");
        }
        if( (!patternEdges.isEmpty()) && Constants.isBURN(patternEdges.get(0).getFrom().getEntity()) ){ 
            System.err.println("TROVATO UN CAZZO DI BURN ADDRESS:" + patternEdges.get(0).getFrom().getSimpleAddress() + "->" + patternEdges.get(0).getTo().getSimpleAddress() + " in pattern: " + patternEdges);
            patternEdges.remove(0);
        }
        if( (!patternEdges.isEmpty()) && Constants.isBURN(patternEdges.get(patternEdges.size()-1).getTo().getEntity()) ){ 
            patternEdges.remove(patternEdges.size()-1);
        }
    }
    

    // ============ SAME NFT CYCLE ============
    /**
     * Pattern: Chain (A→B, B→C, ... , Z→A)
     * 
     * Vincolo: tutti archi scambiano lo stesso NFT e primo e ultimo nodo uguali
     */
    public List<SameNFTCycle> findSameNFTCycle(int minSize) {
        List<SameNFTCycle> results = new ArrayList<>();

        for (String currentNft : this.datasetNFT.getNFTs()) {

            NavigableSet<Edge> nftTransactions = this.datasetNFT.getNFTTransactions(currentNft);
            findSameNFTCyclePatterns(
                currentNft,
                nftTransactions,
                minSize,
                results
            );
        }

        return results;
    }

    public List<SameNFTCycle> findContiguousSameNFTCycle(int minSize) {

        List<SameNFTCycle> results = new ArrayList<>();

        for (String currentNft : this.datasetNFT.getNFTs()) {

            List<NavigableSet<Edge>> nftContiguousTransactions = this.datasetNFT.getNftContiguousTransaction(currentNft);

            for (NavigableSet<Edge> nftContiguousTransaction : nftContiguousTransactions) {
                findSameNFTCyclePatterns(
                    currentNft,
                    nftContiguousTransaction,
                    minSize,
                    results
                );
            }
        }

        return results;
    }
  
    private void findSameNFTCyclePatterns(
            String currentNft,
            NavigableSet<Edge> nftTransactions,
            int minSize,
            List<SameNFTCycle> results) {

        Iterator<Edge> edgeIterator = nftTransactions.iterator();
        List<Edge> windowEdges = new ArrayList<>();
        HashMap<UserNode, Edge> previousUsers = new HashMap<>();

        while(edgeIterator.hasNext()){
            
            Edge currentEdge = edgeIterator.next();
            UserNode currentLastNode = currentEdge.getTo();
            
            //aggiungo currentEdge a timeWindow e aggiorno previousUsers
            windowEdges.add(currentEdge);

            if( currentEdge.getFrom().getEntity() != null) continue; // non memorizzo i nodi con entity (es. marketplace)

            previousUsers.put( currentEdge.getFrom(), currentEdge);
            this.moveTimeWindow(windowEdges, previousUsers);

            //muovo timeWindow se necessario 
            if( previousUsers.containsKey(currentLastNode) && outOfTime( previousUsers.get( currentLastNode ) , delta, currentEdge) ){ 
            }

            //rilevato il ciclo lo aggiungo a results e aggiorno priviousUsers 
            if( previousUsers.containsKey( currentLastNode ) ){

                this.dealNewCycle(currentNft, windowEdges, previousUsers, minSize, results);
            }
        }
        
    }

    private void dealNewCycle(String currentNft, List<Edge> windowEdges, HashMap<UserNode, Edge> previousUsers, int minSize, List<SameNFTCycle> results){
        
        int lastEdgeIndex = windowEdges.size()-1;
        Edge lastEdge = windowEdges.get(windowEdges.size()-1);
        
        UserNode cycledNode = lastEdge.getTo();
        if( cycledNode.getEntity() != null) throw new RuntimeException("errore nella rilevazione del SameNFTCycle del nft " + currentNft + ": nodo con entity non dovrebbe essere in previousUsers. Nodo: " + cycledNode.getSimpleAddress() + ", entity: " + cycledNode.getEntity());

        //trovo l'indice dove inia il ciclo
        int firstEdgeIndex = windowEdges.indexOf( previousUsers.get(cycledNode) );
        if (firstEdgeIndex == -1) throw new RuntimeException("errore nella rilevazione del SameNFTCycle del nft " + currentNft + ": non presente arco con lastNode: " + cycledNode.getSimpleAddress() + ". previousUserValue: " + previousUsers.get(cycledNode).getFrom().getSimpleAddress() + " -> " + previousUsers.get(cycledNode).getTo().getSimpleAddress());

        //aggiungo ciclo a results
        SameNFTCycle cycle = new SameNFTCycle( new ArrayList<>( windowEdges.subList(firstEdgeIndex, lastEdgeIndex+1)), currentNft);
        if( cycle.getEdges().size() >= minSize )
            results.add(cycle);
        
        //i prossimi cicli di cycledNode devono iniziare dal nuovo edge
        previousUsers.remove(cycledNode); // evita cicli (A->B, ..., A->B, ..., B->A)

        /*
        //inizio a cercare nuovi cicli dal nuovo Edge in poi
        windowEdges.clear();
        previousUsers.clear();
         */
    }

    private void moveTimeWindow( List<Edge> windowEdges, HashMap<UserNode, Edge> previousUsers) {
        
        Edge lastEdge = windowEdges.get(windowEdges.size() - 1);

        Iterator<Edge> it = windowEdges.iterator();
        while (it.hasNext()) {
            Edge currentEdge = it.next();
            UserNode currentFrom = currentEdge.getFrom();

            if (outOfTime(currentEdge, delta, lastEdge)) {
                it.remove();
                if ( previousUsers.containsKey(currentFrom) && (previousUsers.get(currentFrom).equals(currentEdge)) ) // rimuovo SOLO se esiste e punta a quell'edge
                    previousUsers.remove(currentFrom);
            } else {
                break;
            }
        }
    }

/*
    private void moveTimeWindow(List<Edge> windowEdges, HashMap<UserNode, Edge> previousUsers){

        Edge lastEdge = windowEdges.get(windowEdges.size()-1);

        // rimuovo archi e corrispettivi nodi che non rientrano più in delta 
        for (int i = 0; i < windowEdges.size(); i++) {

            Edge currentEdge = windowEdges.get(i);
            
            if ( outOfTime(currentEdge, delta, lastEdge) ){
                windowEdges.remove(currentEdge);
                previousUsers.remove(currentEdge.getFrom());
            }
            else{
                break;
            }
        }
    }
 */
/*
    public List<SameNFTCycle> findSameNFTCycleFromChains( List<SameNFTChain> chains, int minSize) {
        List<SameNFTCycle> results = new ArrayList<>();

        for (SameNFTChain chain : chains) {

            String nftId = chain.getNft();
            List<Edge> edges = chain.getEdges();

            if (edges.size() < minSize) continue;

            //trasformo da List<SameNFTChain> a NavigableSet ordinata per timestamp
            NavigableSet<Edge> chainEdges = new TreeSet<>(new EdgeTimestampComparator());
            chainEdges.addAll(edges);

            findSameNFTCyclePatterns(
                nftId,
                chainEdges,
                minSize,
                results
            );
        }

        return results;
    }
 */

    public List<NoAnomalySameNFTChain> findNoAnomalySameNFTChain(List<SameNFTChain> input) {

        List<NoAnomalySameNFTChain> results = new ArrayList<>();

        for (SameNFTChain chain : input) {
            try {
                NoAnomalySameNFTChain clean = new NoAnomalySameNFTChain(chain);
                clean.validate(); // 🔹 controlla anomalie

                results.add(clean);

            } catch (PatternValidationException e) {
            }
        }

        return results;
    }

    public List<NoAnomalySameNFTCycle> findNoAnomalySameNFTCycle(List<SameNFTCycle> input) {

        List<NoAnomalySameNFTCycle> results = new ArrayList<>();

        for (SameNFTCycle cycle : input) {
            try {
                NoAnomalySameNFTCycle clean = new NoAnomalySameNFTCycle(cycle);
                clean.validate();

                results.add(clean);

            } catch (PatternValidationException e) {
            }
        }

        return results;
    }

}
