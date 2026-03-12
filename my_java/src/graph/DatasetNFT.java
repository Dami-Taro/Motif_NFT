package src.graph;

import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;

import src.io.ResultWriter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DatasetNFT {

    // nftId -> catena temporale delle transazioni
    private final Map<String, NavigableSet<Edge>> datasetNfts = new TreeMap<>();

    // id -> nodo per trovare facilmente i nodi già creati e evitare di crearne di duplicati
    private final Map<String, UserNode> nodes = new TreeMap<>();

    private UserNode getOrCreateNode(String id) {
        return nodes.computeIfAbsent(
            id.toLowerCase(),
            UserNode::new
        );
    }

    public void addTransaction(String seller,
                               String buyer,
                               long timestamp,
                               String nftId) {

        UserNode from = getOrCreateNode(seller);
        UserNode to   = getOrCreateNode(buyer);

        Edge edge = new EdgeNFT(from, to, timestamp, nftId);

        datasetNfts
            .computeIfAbsent(
                nftId,
                k -> new TreeSet<>(new EdgeTimestampComparator())
            )
            .add(edge);
    }

    public NavigableSet<Edge> getNFTTransactions(String nftId) {
        return datasetNfts.get(nftId);
    }

    public Set<String> getNFTs() {
        return datasetNfts.keySet();
    }

    public int getTotalNFTs() {
        return datasetNfts.size();
    }

    public long getTotalTransactions() {
        return datasetNfts.values()
            .stream()
            .mapToLong(Set::size)
            .sum();
    }

    public List<NavigableSet<Edge>> getNftContiguousTransaction(String nftId) {

        List<NavigableSet<Edge>> nftContiguousTransactions = new ArrayList<>();
        NavigableSet<Edge> nftTransactions = this.getNFTTransactions(nftId);
        
        if (nftTransactions == null) {
            return nftContiguousTransactions;
        }

        UserNode owner = nftTransactions.first().getFrom();
        NavigableSet<Edge> currentContiguousTransaction = new TreeSet<>(new EdgeTimestampComparator());

        for (Edge edge : nftTransactions) {

            if (!edge.getFrom().equals(owner)) {
                nftContiguousTransactions.add(currentContiguousTransaction);
                currentContiguousTransaction = new TreeSet<>(new EdgeTimestampComparator());
            }
            currentContiguousTransaction.add(edge);
            owner = edge.getTo();
        }
        nftContiguousTransactions.add(currentContiguousTransaction);
        return nftContiguousTransactions;
    }

    // controlla che ogni NavigableSet<Edge> nftTransactions sia contigua
    // per ogni nft stampa la dimensione delle catene di transazioni contigue
    public void printNftContiguousTransactionSizes(Path outputFile){
        String title = "=== non-contiguous nft : chains ===";
        int totalChainInterruptions = 0;

        List<String> nftContiguousTransactionSizes = new ArrayList<>();

        for (String nft : this.getNFTs()) {

            List<NavigableSet<Edge>> contiguousTransactions = this.getNftContiguousTransaction(nft);
            StringBuilder line = new StringBuilder();
            totalChainInterruptions += contiguousTransactions.size() - 1;

            for (NavigableSet<Edge> contiguousTransaction : contiguousTransactions) {
                line.append(contiguousTransaction.size() + " " );
            }
            nftContiguousTransactionSizes.add(nft + ":\t" + line.toString());
        }

        //aggiungo linee iniziali
        int nftcount = nftContiguousTransactionSizes.size();
        if( nftcount > 0 ){
            nftContiguousTransactionSizes.add(0, title);
            nftContiguousTransactionSizes.add(1, "Found " + totalChainInterruptions + " chain interruption in " + nftcount + " different nfts");
            nftContiguousTransactionSizes.add(2, "nft:\t contiguousTransactionCount1 contiguousTransactionCount2 ...\n");
        }
        ResultWriter.appendLinesToFile(nftContiguousTransactionSizes,outputFile);
    }

    // controlla che ogni NavigableSet<Edge> nftTransactions sia contigua
    // se non lo è stampa le catene di transazioni contigue
    public void printOnlyNonContiguousTransactions(Path outputFile){
        String title = "=== non-contiguous nft : chains ===";
        int totalChainInterruptions = 0;

        List<String> everyNftNonContiguousTransactions = new ArrayList<>();

        for (String nft : this.getNFTs()) {

            List<NavigableSet<Edge>> contiguousTransactions = this.getNftContiguousTransaction(nft);
            totalChainInterruptions += contiguousTransactions.size() - 1;
            StringBuilder line = new StringBuilder();

            if( contiguousTransactions.size() > 1 ){
                for (NavigableSet<Edge> contiguousTransaction : contiguousTransactions) {

                    if (contiguousTransaction.isEmpty()) continue;
                    Edge curretEdge = contiguousTransaction.first();

                    for (Edge edge : contiguousTransaction) {
                        curretEdge = edge;
                        line.append(curretEdge.getFrom().getSimpleAddress() + " -> " );
                    }
                    line.append(curretEdge.getTo().getSimpleAddress() + "\t|\t");
                }
                everyNftNonContiguousTransactions.add(nft + ":\t" + line.toString());
            }

        }
        //aggiungo linee iniziali
        int nftcount = everyNftNonContiguousTransactions.size();
        if( nftcount > 0 ){
            everyNftNonContiguousTransactions.add(0, title);
            everyNftNonContiguousTransactions.add(1, "Found " + totalChainInterruptions + " chain interruption in " + nftcount + " different nfts");
            everyNftNonContiguousTransactions.add(2, "nft:\t node1 -> node2 ... \t|\t nextChain ...\n");
        }

        ResultWriter.appendLinesToFile(everyNftNonContiguousTransactions,outputFile);
    }

    // SO GIÀ CHE NON È CONTIGUA: controlla se NavigableSet<Edge> nftTransactions contiene transazioni non contigue
    public void validateAbortedTransactions(Path outputFile){
        /* 

        Map<String, List<Integer>> everyNftAbortedTransactions = new TreeMap<>();
        
        for (String nft : this.getNFTs()) {

            everyNftAbortedTransactions.put(nft, new ArrayList<>());
            NavigableSet<Edge> nftTransactions = this.getNFTTransactions(nft);
            UserNode owner = nftTransactions.first().getFrom();
            int currentAbortedTransaction = 0;

            for (Edge edge : nftTransactions) {

                if (!edge.getFrom().equals(owner)) {
                    currentAbortedTransaction++;
                    continue;
                }
                if(currentAbortedTransaction>0){
                    everyNftAbortedTransactions.get(nft).add(currentAbortedTransaction);
                    currentAbortedTransaction=0;
                }
                owner = edge.getTo();

            }
            if(currentAbortedTransaction>0){
                //everyNftAbortedTransactions.get(nft).add(currentAbortedTransaction);
                currentAbortedTransaction=0;
            }
        }

        ResultWriter.printNftData(everyNftAbortedTransactions, "ABORTED TRANSACTIONS PER NFT",outputFile);
        */
    }

    public boolean isValidDatasetNFT() {
        for (String nftId : datasetNfts.keySet()) {

            List<NavigableSet<Edge>> transactions = this.getNftContiguousTransaction(nftId);

            if (transactions.size() > 1) {
                //System.err.println("⚠️ NFT " + nftId + " has non-contiguous transactions");
                return false;
            }
        }
        return true;
    }
}