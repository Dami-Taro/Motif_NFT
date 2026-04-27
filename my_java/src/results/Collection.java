package src.results;

import java.util.*;

import src.graph.DatasetNFT;
import src.graph.Graph;

public class Collection {

    private String name;

    private long totEdges = -1;
    private long totNodes = -1;
    private long totNfts = -1;
    private long totAnomalies = -1;
    private long totAnomalNfts = -1;

    // label -> Delta
    private Map<String, Delta> deltas = new LinkedHashMap<>();

    // ===== GETTER & SETTER =====

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTotEdges() {
        return totEdges;
    }

    public void setTotEdges(long totEdges) {
        this.totEdges = totEdges;
    }

    public long getTotNodes() {
        return totNodes;
    }

    public void setTotNodes(long totNodes) {
        this.totNodes = totNodes;
    }

    public long getTotNfts() {
        return totNfts;
    }

    public void setTotNfts(long totNfts) {
        this.totNfts = totNfts;
    }

    public long getTotAnomalies() {
        return totAnomalies;
    }

    public void setTotAnomalies(long totAnomalies) {
        this.totAnomalies = totAnomalies;
    }

    public long getTotAnomalNfts() {
        return totAnomalNfts;
    }

    public void setTotAnomalNfts(long totAnomalNfts) {
        this.totAnomalNfts = totAnomalNfts;
    }

    public Map<String, Delta> getDeltas() {
        return deltas;
    }

    public void setDeltas(Map<String, Delta> deltas) {
        this.deltas = deltas;
    }

    public void setInfos(Graph g, DatasetNFT dsNFT) {
        if (g == null || dsNFT == null) return;

        // ===== INFO GRAFO =====
        this.totNodes = g.getNodes().size();
        this.totEdges = g.getTotalEdges();

        // ===== INFO DATASET NFT =====
        this.totNfts = dsNFT.getTotalNFTs();

        // ===== ANOMALIE =====
        int anomalNfts = 0;
        int totalAnomalies = 0;

        for (String nft : dsNFT.getNFTs()) {
            List<NavigableSet<src.graph.Edge>> chains = dsNFT.getNftContiguousTransaction(nft);

            int interruptions = chains.size() - 1;

            if (interruptions > 0) {
                anomalNfts++;
                totalAnomalies += interruptions;
            }
        }

        this.totAnomalNfts = anomalNfts;
        this.totAnomalies = totalAnomalies;
    }

    // ===== LOGICA =====

    public void addAllDeltas(Map<String, Long> deltaMap) {
        if (deltaMap == null) return;

        for (Map.Entry<String, Long> entry : deltaMap.entrySet()) {
            String label = entry.getKey();
            long value = entry.getValue();

            if (value <= 0) continue;

            Delta d = new Delta();
            d.setLabel(label);
            d.setValue(value);

            deltas.put(label, d);
        }
    }

    public void addDelta(Delta delta) {
        if (delta == null || delta.getLabel() == null) return;
        deltas.put(delta.getLabel(), delta);
    }

    public Delta getDelta(String label) {
        return deltas.get(label);
    }

    private long duration = -1;

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }


}