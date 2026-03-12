package src.main;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import src.graph.Graph;
import src.io.GraphLoader;
import src.io.ResultWriter;
import src.io.TimeFormatter;
import src.motifMiner.TemporalMotifMiner;
import src.motifMiner.patterns.*;

public class Main {

    public static void main(String[] args) {

        // ===== PARAMETRI GLOBALI =====
        long delta = 60*60*24*365*5;          // finestra temporale
        double percentage = 5.0;   // percentile per stima delta
        //int minOneWay = 2;
        int minMergedInStar = 2;
        int minMergedGiveAndTake = 2;
        int minMergedReceiveAndForward = 2;
        

        // ===== LISTA DELLE COLLEZIONI DA ANALIZZARE =====
        List<Path> collectionFiles = new ArrayList<>();
        collectionFiles.add(Paths.get("collections/boredapeyachtclub.json"));
        //collectionFiles.add(Paths.get("collections/TestcaseDistinctInStar.json"));
        //collectionFiles.add(Paths.get("collections/TestcaseOneWayCouple.json"));
        //collectionFiles.add(Paths.get("collections/TestcaseMergedInStar.json")); 
        //collectionFiles.add(Paths.get("collections/TestcaseMergedGiveAndTake.json")); 
        //collectionFiles.add(Paths.get("collections/TestcaseMergedReceiveAndForward.json"));

        for (Path collectionPath : collectionFiles) {

            String fileName = collectionPath.getFileName().toString();
            String collectionName = fileName.replaceFirst("\\.json$", "");

            System.out.println("\n=== Processing collection: " + collectionName + " ===");

            try {
                // ===== LOAD GRAPH =====
                Graph g = GraphLoader.loadFromJsonNFT(collectionPath);
                DeltaEstimator deltaEstimator = new DeltaEstimator(g);
                //delta = deltaEstimator.getPercentile(percentage); // 90th percentile
                //System.out.println("⏱️ Estimated delta " + percentage + "th percentile): " + delta + " sec = " + TimeFormatter.secondsToSimpleString(delta));
                if(delta<=0) {
                    System.out.println("⚠️ Estimated delta is not valid, skipping collection " + collectionName);
                    continue;
                }

                // ===== OUTPUT PATH =====
                Path resultDir = Paths.get("results", collectionName);
                Files.createDirectories(resultDir);
                Path outputFile = resultDir.resolve("res_" + TimeFormatter.secondsToSimpleString(delta) + ".txt");

                // ===== MINING =====
                TemporalMotifMiner miner = new TemporalMotifMiner(g, delta);

                List<InStar> mergedInStar = miner.findMergedInStars(minMergedInStar);
                List<GiveAndTake> mergedGiveAndTake = miner.findMergedGiveAndTakes(minMergedGiveAndTake);
                List<ReceiveAndForward> mergedReceiveAndForward = miner.findMergedReceiveAndForward(minMergedReceiveAndForward);
                List<ReceiveAndForwardNFT> mergedReceiveAndForwardNFT = miner.findMergedReceiveAndForwardNFT(minMergedReceiveAndForward, mergedReceiveAndForward);

                // ===== SCRITTURA RISULTATI =====
                ResultWriter.writeGraphInfoToFile(g, delta, outputFile);
                ResultWriter.appendPatternCountsBySizeToFile("MergedInStar", mergedInStar, outputFile);
                ResultWriter.appendPatternCountsBySizeToFile("MergedGiveAndTake", mergedGiveAndTake, outputFile);
                ResultWriter.appendPatternCountsBySizeToFile("MergedReceiveAndForward", mergedReceiveAndForward, outputFile);
                ResultWriter.appendPatternCountsBySizeToFile("MergedReceiveAndForwardNFT", mergedReceiveAndForwardNFT, outputFile);
                
                //ResultWriter.appendPatternsToFile("MergedInStar", mergedInStar, outputFile);
                //ResultWriter.appendPatternsToFile("MergedGiveAndTake", mergedGiveAndTake, outputFile);
                //ResultWriter.appendPatternsToFile("MergedReceiveAndForward", mergedReceiveAndForward, outputFile);
                //ResultWriter.appendPatternsToFile("MergedReceiveAndForwardNFT", mergedReceiveAndForwardNFT, outputFile);


                System.out.println("✅ Risultati scritti in " + outputFile);

            } catch (OutOfMemoryError e) {
                System.err.println("⚠️ OutOfMemoryError durante l'analisi di "
                        + collectionName + " — risultati parziali possibili");
            } catch (Exception e) {
                System.err.println("❌ Errore durante l'analisi di "
                        + collectionName);
                e.printStackTrace();
            }
        }

        System.out.println("\n=== Analisi completata ===");
    }
}
