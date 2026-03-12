package src.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import src.graph.Graph;
import src.motifMiner.patterns.Pattern;

public class ResultWriter {

    // Scrive le informazioni base del grafo su file di testo
    public static void writeGraphInfoToFile(
            Graph graph,
            long delta,
            Path output) {

        try (BufferedWriter writer = Files.newBufferedWriter(output)) {

            writer.write("=== GRAPH INFO ===\n");
            writer.write("Nodi: " + graph.getNodes().size() + "\n");
            writer.write("Archi: " + graph.getTotalEdges() + "\n");
            writer.write("Delta (sec): " + delta + "\n\n");

        } catch (IOException e) {
            System.err.println("Errore durante la scrittura delle informazioni del grafo su file: "
                    + output);
            e.printStackTrace();
        }
    }

    // Appende una lista di pattern a un file già esistente
    public static void appendPatternsToFile(
            String title,
            List<? extends Pattern> patterns,
            Path output) {

        try (BufferedWriter writer = Files.newBufferedWriter(
                output,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {

            writer.write("\n=== " + title + " ===\n");
            writer.write("Totale pattern: " + patterns.size() + "\n\n");

            int index = 1;
            for (Pattern pattern : patterns) {
                writer.write("[" + index++ + "] ");
                writer.write(pattern.toString());
                writer.write("\n");
            }

        } catch (IOException e) {
            System.err.println("Errore durante l'append dei pattern su file: "
                    + output + " (section: " + title + ")");
            e.printStackTrace();
        }
    }

    // Raggruppa i pattern per dimensione in una mappa ordinata
    public static void appendPatternCountsBySizeToFile(
            String title,
            List<? extends Pattern> patterns,
            Path output) {

        Map<Integer, List<Pattern>> groupedBySize = groupPatternsBySize(patterns);

        try (BufferedWriter writer = Files.newBufferedWriter(
                output,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {

            writer.write("\n=== " + title + " (grouped by size) ===\n");
            writer.write("Totale pattern: " + patterns.size() + "\n\n");

            for (Map.Entry<Integer, List<Pattern>> entry : groupedBySize.entrySet()) {

                int size = entry.getKey();
                int count = entry.getValue().size();

                writer.write(
                    String.format("size %d → %d pattern\n", size, count)
                );
            }

        } catch (IOException e) {
            System.out.println(
                "Errore durante la scrittura dei conteggi per size su file: "
                + output + " (section: " + title + ")"
            );
            e.printStackTrace();
        }
    }


    private static Map<Integer, List<Pattern>> groupPatternsBySize(List<? extends Pattern> patterns) {

        Map<Integer, List<Pattern>> grouped = new TreeMap<>();

        for (Pattern pattern : patterns) {
            int size = pattern.getSize();
            grouped.computeIfAbsent(size, k -> new ArrayList<>()).add(pattern);
        }

        return grouped;
    }




}


/* 
    //Scrive una lista di pattern su file di testo
    public static void writePatternsToFile(
            String title,
            List<? extends Pattern> patterns,
            Path output) throws IOException {

        try (BufferedWriter writer = Files.newBufferedWriter(output)) {

            writer.write("=== " + title + " ===\n");
            writer.write("Totale pattern: " + patterns.size() + "\n\n");

            int index = 1;
            for (Pattern pattern : patterns) {
                writer.write("[" + index++ + "] ");
                writer.write(pattern.toString());
                writer.write("\n");
            }
        }
    }
    */