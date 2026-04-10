package src.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import src.graph.Graph;
import src.graph.UserNode;
import src.graph.DatasetNFT;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

public class Loader {

    private static final Gson gson = new GsonBuilder().create();

    // Evento singolo
    private static class EventDTO {
        String seller;
        String seller_entity;
        String buyer;
        String buyer_entity;
        long event_timestamp;
        NFTDTO nft;
    }

    private static class NFTDTO {
        String identifier;
    }

    // Contenitore per ogni riga
    private static class AssetEventsWrapper {
        List<EventDTO> asset_events;
    }

    private static class EventIterator implements Iterator<EventDTO> {

        private final BufferedReader reader;
        private Iterator<EventDTO> currentBatch = null;
        private EventDTO nextEvent = null;
        private boolean finished = false;
        private int lineNumber = 0;

        EventIterator(BufferedReader br) throws IOException {
            this.reader = br;
            advance();
        }

        private void advance() {
            nextEvent = null;

            try {
                while (true) {

                    // se ho ancora eventi nella batch corrente
                    if (currentBatch != null && currentBatch.hasNext()) {
                        nextEvent = currentBatch.next();
                        return;
                    }

                    // altrimenti leggo una nuova riga
                    String line = reader.readLine();
                    if (line == null) {
                        finished = true;
                        reader.close();
                        return;
                    }

                    lineNumber++;

                    try {
                        AssetEventsWrapper wrapper =
                            gson.fromJson(line, AssetEventsWrapper.class);

                        if (wrapper != null &&
                            wrapper.asset_events != null &&
                            !wrapper.asset_events.isEmpty()) {

                            currentBatch = wrapper.asset_events.iterator();
                        }

                    } catch (Exception e) {
                        System.err.println(
                            "Malformed JSON at line " + lineNumber
                        );
                        advance();
                    }
                }
            } catch (IOException e) {
                finished = true;
                try { reader.close(); } catch (IOException ignored) {}
            }
        }

        @Override
        public boolean hasNext() {
            return !finished && nextEvent != null;
        }

        @Override
        public EventDTO next() {
            EventDTO result = nextEvent;
            advance();
            return result;
        }
    }


    public static Graph loadGraphFromJson(Path jsonPath) {
        return doLoadFromJson(jsonPath, false);
    }

    public static Graph loadGraphFromJsonNFT(Path jsonPath) {
        return doLoadFromJson(jsonPath, true);
    }

    private static Graph doLoadFromJson(Path jsonPath, boolean includeNFT) {

        Graph graph = new Graph(includeNFT);
        int nonNFTEvents = 0;

        try (BufferedReader br = Files.newBufferedReader(jsonPath)) {
            
            EventIterator events = new EventIterator(br);

            while ( events.hasNext() ){

                EventDTO event = events.next();

                if (event.seller == null ||
                    event.buyer == null ||
                    event.event_timestamp <= 0)
                    continue;

                if(includeNFT && (
                    event.nft == null ||
                    event.nft.identifier == null)){
                    nonNFTEvents++;
                    continue;
                }

                UserNode sellerNode = graph.getOrCreateNode(event.seller);
                UserNode buyerNode  = graph.getOrCreateNode(event.buyer);

                if (event.seller_entity != null) {
                    //if( !Constants.isBURN(event.seller_entity) ) System.out.println("nft: " +event.nft.identifier+ ": (" +event.seller_entity+ ")" +event.seller + " -> " + event.buyer);
                    sellerNode.setEntity(event.seller_entity);
                }
                if (event.buyer_entity != null) {
                    //if( !Constants.isBURN(event.buyer_entity) ) System.out.println("⚠️nft: " +event.nft.identifier+ ": " +event.seller + " -> " + event.buyer+ " (" +event.buyer_entity+ ")");
                    buyerNode.setEntity(event.buyer_entity);
                }

                if(includeNFT)
                    graph.addEdgeNFT(   // archi con NFT
                        event.seller,
                        event.buyer,
                        event.event_timestamp,
                        event.nft.identifier
                    );
                else
                    graph.addEdge(  // archi senza NFT
                        event.seller,
                        event.buyer,
                        event.event_timestamp
                    );

            }
            
        } catch(IOException e){
            System.err.println("Errore in apertura file");
        }
        catch(Exception e){
            System.err.println("Qualcosa è andato storto durante la creazione di Graph");
        }

        if(includeNFT && nonNFTEvents > 0) {
            System.out.println("⚠️  Skipped " + nonNFTEvents +
                " non-NFT events while loading graph from " +
                jsonPath.getFileName() + "\n");
        }

        graph.initializeTemporalInfo();

        return graph;
    }

    public static DatasetNFT LoadDatasetNFTFromJson(Path jsonPath) {

        DatasetNFT nftChains = new DatasetNFT();

        try (BufferedReader br = Files.newBufferedReader(jsonPath)) {
            
            EventIterator events = new EventIterator(br);

            while ( events.hasNext() ){

                EventDTO event = events.next();

                if (event.seller == null ||
                    event.buyer == null ||
                    event.event_timestamp <= 0 ||
                    (event.nft == null || event.nft.identifier == null))
                    continue;

                nftChains.addTransaction(   // archi con NFT
                    event.seller,
                    event.buyer,
                    event.event_timestamp,
                    event.nft.identifier
                );
            
            }
            
        } catch(IOException e){
            System.err.println("Errore in apertura file");
        }
        catch(Exception e){
            System.err.println("Qualcosa è andato storto durante la creazione di Graph");
        }

        return nftChains;
    }


}
