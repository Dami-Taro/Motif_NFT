package src.graph;

import java.util.Objects;

public class EdgeNFT extends Edge {

    private final String nftId;

    public EdgeNFT(UserNode from,
                   UserNode to,
                   long timestamp,
                   String nftId) {
        super(from, to, timestamp);
        this.nftId = nftId;
    }

    public String getNftId() {
        return nftId;
    }

    @Override
    public String toString() {
        return String.format(
            "EdgeNFT{id=%d, %s → %s, nft=%s, ts=%d}",
            getId(),
            getFrom().getAddress().substring(0, 8),
            getTo().getAddress().substring(0, 8),
            nftId,
            getTimestamp()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EdgeNFT)) return false;
        if (!super.equals(o)) return false;
        EdgeNFT edgeNFT = (EdgeNFT) o;
        return Objects.equals(nftId, edgeNFT.nftId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), nftId);
    }
}
