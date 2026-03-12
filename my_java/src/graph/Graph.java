package src.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Graph {

    private final Map<String, UserNode> nodes = new HashMap<>();

    public UserNode getOrCreateNode(String address) {
        return nodes.computeIfAbsent(
            address.toLowerCase(),
            UserNode::new
        );
    }

    public void addEdge(String seller,
                        String buyer,
                        long timestamp) {

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
    


}