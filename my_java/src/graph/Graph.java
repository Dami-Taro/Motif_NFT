package src.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Graph {

    private final boolean nft;
    private final Map<String, UserNode> nodes = new HashMap<>();
    private long minDate = -1;
    private long maxDate = -1;
    private long duration = -1;

    public Graph(boolean hasNFT){
        this.nft=hasNFT;
    }

    public boolean hasNFT(){
        return nft;
    }

    public UserNode getOrCreateNode(String address) {
        return nodes.computeIfAbsent(
            address.toLowerCase(),
            UserNode::new
        );
    }

    public void addEdge(String seller,
                        String buyer,
                        long timestamp) {
        if(this.hasNFT()) throw new GraphException("impossibile aggiungere Edge a GraphNFT\n");

        UserNode from = getOrCreateNode(seller);
        UserNode to = getOrCreateNode(buyer);

        Edge edge = new Edge(from, to, timestamp);

        from.addOutgoingEdge(edge);
        to.addIncomingEdge(edge);
    }

    public void addEdgeNFT(String seller,  // archi con NFT
                           String buyer,
                           long timestamp,
                           String nftId) {
        if( !this.hasNFT() ) throw new GraphException("impossibile aggiungere EdgeNFT a Graph\n");

        UserNode from = getOrCreateNode(seller);
        UserNode to = getOrCreateNode(buyer);

        EdgeNFT edge = new EdgeNFT(from, to, timestamp, nftId);

        from.addOutgoingEdge(edge);
        to.addIncomingEdge(edge);
    }

    public Set<UserNode> getNodes() {
        return new HashSet<>(nodes.values());
    }

    public long getTotalEdges() {
        return nodes.values().stream()
            .mapToInt(n -> n.getOutgoingEdges().size())
            .sum();
    }

    public long getMinDate() {
        return minDate;
    }
    public long getMaxDate() {
        return maxDate;
    }
    public long getDuration() {
        return duration;
    }
    
    public void setMinDate(long minDate) {
        this.minDate = minDate;
        updateDuration();
    }
    public void setMaxDate(long maxDate) {
        this.maxDate = maxDate;
        updateDuration();
    }
    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setMinDate() {
        long min = Long.MAX_VALUE;
        boolean found = false;

        for (UserNode node : nodes.values()) {
            if (!node.getOutgoingEdges().isEmpty()) {
                long ts = node.getOutgoingEdges().first().getTimestamp();
                if (ts < min) {
                    min = ts;
                    found = true;
                }
            }
        }

        this.minDate = found ? min : -1;
        updateDuration();
    }
    public void setMaxDate() {
        long max = Long.MIN_VALUE;
        boolean found = false;

        for (UserNode node : nodes.values()) {
            if (!node.getOutgoingEdges().isEmpty()) {
                long ts = node.getOutgoingEdges().last().getTimestamp();
                if (ts > max) {
                    max = ts;
                    found = true;
                }
            }
        }

        this.maxDate = found ? max : -1;
        updateDuration();
    }

    private void updateDuration() {
        if (minDate >= 0 && maxDate >= 0 && maxDate >= minDate) {
            this.duration = maxDate - minDate;
        }
    }

    public void initializeTemporalInfo() {
        setMinDate();
        setMaxDate();
    }

}