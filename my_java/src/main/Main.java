package src.main;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import src.graph.Graph;
import src.io.GraphLoader;
import src.io.ResultWriter;
import src.motifMiner.TemporalMotifMiner;
import src.motifMiner.patterns.InStar;
import src.motifMiner.patterns.OneWayCouple;

public class Main {

    public static void main(String[] args) {

        // ===== PARAMETRI GLOBALI =====
        long delta = 604800*4;          // finestra temporale
        int minOneWay = 2;
        int minInStar = 1;

        // ===== LISTA DELLE COLLEZIONI DA ANALIZZARE =====
        List<Path> collectionFiles = new ArrayList<>();
        collectionFiles.add(Paths.get("collections/boredapeyachtclub.json"));
        collectionFiles.add(Paths.get("collections/TestcaseInStar.json"));
        collectionFiles.add(Paths.get("collections/TestcaseOneWayCouple.json"));

        for (Path collectionPath : collectionFiles) {

            String fileName = collectionPath.getFileName().toString();
            String collectionName = fileName.replaceFirst("\\.json$", "");

            System.out.println("\n=== Processing collection: " + collectionName + " ===");

            try {
                // ===== LOAD GRAPH =====
                Graph g = GraphLoader.loadFromJson(collectionPath);

                // ===== MINING =====
                TemporalMotifMiner miner = new TemporalMotifMiner(g, delta);

                List<OneWayCouple> oneWayCouplePatterns =
                        miner.findOneWayCouples(minOneWay);

                List<InStar> inStarPatterns =
                        miner.findInStars(minInStar);

                // ===== OUTPUT PATH =====
                Path resultDir = Paths.get("results", collectionName);
                Files.createDirectories(resultDir);

                Path outputFile = resultDir.resolve("res_" + (delta) + ".txt");

                // ===== SCRITTURA RISULTATI =====
                ResultWriter.writeGraphInfoToFile(g, delta, outputFile);

                ResultWriter.appendPatternCountsBySizeToFile(
                        "OneWayCouple",
                        oneWayCouplePatterns,
                        outputFile
                );

                ResultWriter.appendPatternCountsBySizeToFile(
                        "InStar",
                        inStarPatterns,
                        outputFile
                );

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
