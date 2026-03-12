package src.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import src.graph.Graph;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class GraphLoader {

    private static final Gson gson = new GsonBuilder().create();

    // Evento singolo
    private static class AssetEventDTO {
        String seller;
        String buyer;
        long event_timestamp;
        NFTDTO nft;
    }

    private static class NFTDTO {
        String identifier;
    }

    // Contenitore per ogni riga
    private static class AssetEventsWrapper {
        List<AssetEventDTO> asset_events;
    }

    public static Graph loadFromJson(Path jsonPath) throws IOException {

        Graph graph = new Graph();
        int lineNumber = 0;

        try (BufferedReader br = Files.newBufferedReader(jsonPath)) {
            String line;

            while ((line = br.readLine()) != null) {
                lineNumber++;

                try {
                    AssetEventsWrapper wrapper =
                            gson.fromJson(line, AssetEventsWrapper.class);

                    if (wrapper == null || wrapper.asset_events == null)
                        continue;

                    for (AssetEventDTO event : wrapper.asset_events) {
                        if (event.seller == null ||
                            event.buyer == null ||
                            event.event_timestamp <= 0)
                            continue;

                        graph.addEdge(
                            event.seller,
                            event.buyer,
                            event.event_timestamp
                        );
                    }

                } catch (Exception e) {
                    System.err.println("Malformed JSON at line " + lineNumber);
                }
            }
        }

        return graph;
    }

    public static Graph loadFromJsonNFT(Path jsonPath) throws IOException {

        Graph graph = new Graph();
        int lineNumber = 0;

        try (BufferedReader br = Files.newBufferedReader(jsonPath)) {
            String line;

            while ((line = br.readLine()) != null) {
                lineNumber++;

                try {
                    AssetEventsWrapper wrapper =
                            gson.fromJson(line, AssetEventsWrapper.class);

                    if (wrapper == null || wrapper.asset_events == null)
                        continue;

                    for (AssetEventDTO event : wrapper.asset_events) {
                        if (event.seller == null ||
                            event.buyer == null ||
                            event.event_timestamp <= 0 ||
                            event.nft == null ||
                            event.nft.identifier == null)
                            continue;

                        graph.addEdgeNFT(   // archi con NFT
                            event.seller,
                            event.buyer,
                            event.event_timestamp,
                            event.nft.identifier
                        );
                    }

                } catch (Exception e) {
                    System.err.println("Malformed JSON at line " + lineNumber);
                }
            }
        }

        return graph;
    }

}
