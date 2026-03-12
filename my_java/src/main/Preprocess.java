package src.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

import src.graph.DatasetNFT;
import src.graph.Graph;
import src.io.Loader;

public final class Preprocess {

    private Preprocess() {
        // utility class
    }

    /**
     * Scansiona ricorsivamente la directory root e restituisce
     * tutti i file .json trovati (qualsiasi sottodirectory).
     */
    public static List<Path> scanJsonFiles(Path rootDir) {
        List<Path> jsonFiles = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(rootDir)) {
            paths
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".json"))
                .forEach(jsonFiles::add);
        } catch (IOException e) {
            throw new UncheckedIOException("Errore durante la scansione di " + rootDir, e);
        }

        return jsonFiles;
    }

    /**
     * Ordina i file per dimensione su disco (crescente).
     * Da chiamare UNA SOLA VOLTA prima dell'analisi.
     */
    public static void sortBySize(List<Path> files) {
        files.sort(Comparator.comparingLong(p -> {
            try {
                return Files.size(p);
            } catch (IOException e) {
                throw new UncheckedIOException("Impossibile leggere dimensione file: " + p, e);
            }
        }));
    }

    /**
     * Ordina i file per dimensione su disco (crescente).
     * Da chiamare UNA SOLA VOLTA prima dell'analisi.
     */
    public static void sortByDescendingSize(List<Path> files) {
        files.sort(
            Comparator.comparingLong((Path p) -> {
                try {
                    return Files.size(p);
                } catch (IOException e) {
                    throw new UncheckedIOException("Impossibile leggere dimensione file: " + p, e);
                }
            }).reversed()
        );
    }


    /**
     * Crea i FileInfos per ogni file JSON, verificando
     * la validità del DatasetNFT associato.
     *
     * @param files lista di Path già ordinata per dimensione
     * @return lista di FileInfos
     */
    public static List<FileInfos> buildFileInfos(List<Path> files) {
        List<FileInfos> infos = new ArrayList<>();

        for (Path collectionPath : files) {

            long size;
            try {
                size = Files.size(collectionPath);
            } catch (IOException e) {
                throw new UncheckedIOException("Errore lettura size: " + collectionPath, e);
            }

            FileInfos fi = new FileInfos(collectionPath, size);

            DatasetNFT dsNFT = Loader.LoadDatasetNFTFromJson(collectionPath);
            fi.setValid(dsNFT.isValidDatasetNFT());

            infos.add(fi);
        }

        return infos;
    }

    /**
     * Scrive su file lo stato dei FileInfos.
     *
     * Formato:
     * path;size;isValid;isProcessed
     */
    public static void writeFileInfosToFile(
            List<FileInfos> infos,
            Path outputFile
    ) {

        try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {

            // header (opzionale ma utile)
            writer.write("path;size;isValid;isProcessed");
            writer.newLine();

            for (FileInfos fi : infos) {
                writer.write(
                        fi.getPath() + ";" +
                        fi.getSize() + ";" +
                        fi.isValid() + ";" +
                        fi.isProcessed()
                );
                writer.newLine();
            }

        } catch (IOException e) {
            throw new RuntimeException(
                    "Errore scrittura FileInfos su file: " + outputFile, e
            );
        }
    }

    // ===============================
    // READ CHECKPOINT
    // ===============================
    public static List<FileInfos> readFileInfosFromFile(Path inputFile) {

        List<FileInfos> result = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(inputFile)) {

            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {

                // skip header
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] tokens = line.split(";");

                if (tokens.length != 4) {
                    throw new IllegalStateException(
                            "Formato invalido FileInfos: " + line
                    );
                }

                Path path = Paths.get(tokens[0]);
                long size = Long.parseLong(tokens[1]);
                boolean isValid = Boolean.parseBoolean(tokens[2]);
                boolean isProcessed = Boolean.parseBoolean(tokens[3]);

                FileInfos fi = new FileInfos(path, size);
                fi.setValid(isValid);
                fi.setProcessed(isProcessed);

                result.add(fi);
            }

        } catch (IOException e) {
            throw new RuntimeException(
                    "Errore lettura FileInfos da file: " + inputFile, e
            );
        }

        return result;
    }


    /**
     * Costruisce il Path di output replicando la struttura sotto "collections"
     * dentro la directory "results".
     *
     * Esempio:
     * collections/pfp/foo.json
     * -> results/pfp/foo/res_XX.txt
     *
     * @param collectionPath path del file JSON originale
     * @param delta valore in secondi per costruire il nome file
     * @return Path completo del file di output
     */
/*
    public static Path getOutputPath(Path collectionPath, String label, long delta) {

        Path collectionsRoot = Paths.get("collections");
        Path resultsRoot = Paths.get("results");

        if (!collectionPath.startsWith(collectionsRoot)) {
            throw new IllegalArgumentException(
                    "Il path non è contenuto dentro 'collections': " + collectionPath
            );
        }

        // Path relativo rispetto a collections
        Path relativePath = collectionsRoot.relativize(collectionPath);
        // es: pfp/foo.json  oppure pfp/rare/foo.json

        // Cartelle genitore (es: pfp oppure pfp/rare)
        Path parentDirs = relativePath.getParent();

        // Nome file senza estensione
        String fileName = relativePath.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        String baseName = (dotIndex == -1)
                ? fileName
                : fileName.substring(0, dotIndex);

        // results/.../foo
        Path resultDir = (parentDirs == null)
                ? resultsRoot.resolve(baseName)
                : resultsRoot.resolve(parentDirs).resolve(baseName);

        try {
            Files.createDirectories(resultDir);
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Errore creazione directory output: " + resultDir, e
            );
        }

        String resultFileName =
                "res_" + label + "_" + TimeFormatter.secondsToSimpleString(delta) + ".txt";

        return resultDir.resolve(resultFileName);
    }
 */

    public static Path createResultDirectory(Path collectionPath) {

        Path collectionsRoot = Paths.get("collections");
        Path resultsRoot = Paths.get("results");

        if (!collectionPath.startsWith(collectionsRoot)) {
            throw new IllegalArgumentException(
                "Il path non è contenuto dentro 'collections': " + collectionPath
            );
        }

        // Path relativo rispetto a collections
        Path relativePath = collectionsRoot.relativize(collectionPath);
        // es: pfp/foo.json oppure pfp/rare/foo.json

        Path parentDirs = relativePath.getParent();

        // Nome file senza estensione
        String fileName = relativePath.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        String baseName = (dotIndex == -1)
                ? fileName
                : fileName.substring(0, dotIndex);

        // results/.../foo
        Path resultDir = (parentDirs == null)
                ? resultsRoot.resolve(baseName)
                : resultsRoot.resolve(parentDirs).resolve(baseName);

        try {
            Files.createDirectories(resultDir);
        } catch (IOException e) {
            throw new UncheckedIOException(
                "Errore creazione directory output: " + resultDir, e
            );
        }

        return resultDir;
    }


    public static Map<String, Long> computeDeltaMap(Graph graph) {

        Map<String, Long> deltaMap = new LinkedHashMap<>();

        DeltaEstimator estimator = new DeltaEstimator(graph);

        long p25 = estimator.getPercentile(25);
        long p50 = estimator.getPercentile(50);
        long p75 = estimator.getPercentile(75);
        long p100 = estimator.getPercentile(100);

        if (p25 > 0)  deltaMap.put("25_percentile", p25);
        if (p50 > 0)  deltaMap.put("50_percentile", p50);
        if (p75 > 0)  deltaMap.put("75_percentile", p75);
        if (p100 > 0) deltaMap.put("100_percentile", p100);

        long duration = graph.getDuration();
        if (duration > 0) deltaMap.put("duration", duration);

        return deltaMap;
    }
}