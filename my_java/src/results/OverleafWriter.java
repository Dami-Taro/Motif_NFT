package src.results;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import src.io.ResultWriter;
import src.motifMiner.patterns.GiveAndTake;
import src.motifMiner.patterns.InStar;
import src.motifMiner.patterns.NoAnomalySameNFTChain;
import src.motifMiner.patterns.NoAnomalySameNFTCycle;
import src.motifMiner.patterns.ReceiveAndForwardNFT;
import src.motifMiner.patterns.SameNFTChain;
import src.motifMiner.patterns.SameNFTCycle;

public class OverleafWriter {

    private static final String SEP = ",";
    private static String formatCollection(String name) {
        if (name == null) return "";

        Map<String, String> map = new HashMap<>();
        map.put("axie_infinity_assets", "aiA");
        map.put("decentraland_assets", "dA");
        map.put("decentraland_estate", "dE");
        map.put("decentraland_land", "dL");
        map.put("decentraland_names", "dN");
        map.put("the_sandbox_assets", "sbA");
        map.put("the_sandbox_land", "sbL");

        return map.getOrDefault(name, name);
    }
    private static String formatDelta(String delta) {
        if (delta == null) return "";

        Map<String, String> map = new HashMap<>();
        map.put("25_percentile", "p25");
        map.put("50_percentile", "p50");
        map.put("75_percentile", "p75");
        map.put("100_percentile", "p100");
        map.put("duration", "dur");

        return map.getOrDefault(delta, delta);
    }
    
    public static void writeDiffAnomalyCount(Results results){
        if (results == null || results.getCollections().isEmpty()) return;

        // ===== FILES =====
        List<String> patterns = Arrays.asList(
            SameNFTChain.class.getSimpleName(), NoAnomalySameNFTChain.class.getSimpleName(), 
            SameNFTCycle.class.getSimpleName(), NoAnomalySameNFTCycle.class.getSimpleName()
        );

        Iterator<String> patternIterator = patterns.iterator();

        while (patternIterator.hasNext()) {
            String pattern = patternIterator.next();
            String noAnomalyPattern = patternIterator.hasNext() ? patternIterator.next() : null;

            if( noAnomalyPattern == null ){
                System.out.println("NoAnomaly pattern not found for pattern: " + pattern);
                continue;
            }

            do_writeDiffAnomalyCount(results, pattern, noAnomalyPattern);
        }

    }
    private static void do_writeDiffAnomalyCount(Results results, String patternName, String noAnomalyPatternName) {

        try {
            Path overleafDir = results.getResultDir().resolve("overleaf");
            Files.createDirectories(overleafDir);

            Path file = overleafDir.resolve("diffAnomalyCount_" + patternName + ".dat");
            ResultWriter.createEmptyFile(file);

            List<String> lines = new ArrayList<>();

            // ===== COLUMNS =====
            List<String> deltas = Arrays.asList(
                "25_percentile",
                "50_percentile",
                "75_percentile",
                "100_percentile",
                "duration"
            );

            // ===== ROWS =====
            List<String> collections = Arrays.asList(
                "axie_infinity_assets",
                "decentraland_assets",
                "decentraland_estate",
                "decentraland_land",
                "decentraland_names",
                "the_sandbox_assets",
                "the_sandbox_land"
            );

            // ===== HEADER =====
            StringBuilder header = new StringBuilder("Collection");
            for (String delta : deltas) {
                header.append(SEP).append(formatDelta(delta));
            }
            lines.add(header.toString());

            // ===== ROWS =====
            for (String collectionName : collections) {

                Collection collection = results.getCollection(collectionName);
                if (collection == null){ System.out.println("DiffAnomalyCount collection not found: " + collectionName); continue;}

                StringBuilder row = new StringBuilder(formatCollection(collectionName));

                for (String delta : deltas) {

                    Delta d = collection.getDelta(delta);
                    if (d == null){ System.out.println("DiffAnomalyCount in collection: " + collectionName + " delta not found: " + delta);}

                    int value = getAnomalyDifference( d.getPatternResults(), patternName, noAnomalyPatternName );

                    row.append(SEP).append( value );
                }

                lines.add(row.toString());
            }

            // ===== WRITE FILE =====
            System.out.println("Writing data to: " + file);
            ResultWriter.appendLinesToFile(lines, file);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static int getAnomalyDifference(Map<String, List<Integer>> patternResults, String patternName, String noAnomalyPatternName) {
        if (patternResults == null) return 0;

        List<Integer> patternValues = patternResults.get( patternName ); 
        if(patternValues == null){ System.out.println("Pattern values for: " + patternName + " not found");}
        List<Integer> noAnomalyPatternValues = patternResults.get( noAnomalyPatternName );
        if(noAnomalyPatternValues == null){ System.out.println("NoAnomalyPattern values for: " + noAnomalyPatternName + " not found");}

        if (patternValues == null || noAnomalyPatternValues == null) return 0;

        int diff =  patternValues.size() - noAnomalyPatternValues.size();

        //System.out.println("patternSize: " + patternValues.size() + " noAnomalyPatternSize: " + noAnomalyPatternValues.size() + " diff: " + diff );

        return Math.max(diff, 0);
    }

    public static void writeRatioAnomalyCount(Results results){
        if (results == null || results.getCollections().isEmpty()) return;

        // ===== FILES =====
        List<String> patterns = Arrays.asList(
            SameNFTChain.class.getSimpleName(), NoAnomalySameNFTChain.class.getSimpleName(), 
            SameNFTCycle.class.getSimpleName(), NoAnomalySameNFTCycle.class.getSimpleName()
        );

        Iterator<String> patternIterator = patterns.iterator();

        while (patternIterator.hasNext()) {
            String pattern = patternIterator.next();
            String noAnomalyPattern = patternIterator.hasNext() ? patternIterator.next() : null;

            if( noAnomalyPattern == null ){
                System.out.println("NoAnomaly pattern not found for pattern: " + pattern);
                continue;
            }

            do_writeRatioAnomalyCount(results, pattern, noAnomalyPattern);
        }
    }
    private static void do_writeRatioAnomalyCount(Results results, String patternName, String noAnomalyPatternName) {

        try {
            Path overleafDir = results.getResultDir().resolve("overleaf");
            Files.createDirectories(overleafDir);

            Path file = overleafDir.resolve("ratioAnomalyCount_" + patternName + ".dat");
            ResultWriter.createEmptyFile(file);

            List<String> lines = new ArrayList<>();

            // ===== COLUMNS =====
            List<String> deltas = Arrays.asList(
                "25_percentile",
                "50_percentile",
                "75_percentile",
                "100_percentile",
                "duration"
            );

            // ===== ROWS =====
            List<String> collections = Arrays.asList(
                "axie_infinity_assets",
                "decentraland_assets",
                "decentraland_estate",
                "decentraland_land",
                "decentraland_names",
                "the_sandbox_assets",
                "the_sandbox_land"
            );

            // ===== HEADER =====
            StringBuilder header = new StringBuilder("Collection");
            for (String delta : deltas) {
                header.append(SEP).append(formatDelta(delta));
            }
            lines.add(header.toString());

            // ===== ROWS =====
            for (String collectionName : collections) {

                Collection collection = results.getCollection(collectionName);
                if (collection == null){ 
                    System.out.println("RatioAnomalyCount collection not found: " + collectionName); 
                    continue;
                }

                StringBuilder row = new StringBuilder(formatCollection(collectionName));

                for (String delta : deltas) {

                    Delta d = collection.getDelta(delta);
                    if (d == null){ 
                        System.out.println("RatioAnomalyCount in collection: " + collectionName + " delta not found: " + delta);
                    }

                    double value = getAnomalyRatio(d.getPatternResults(), patternName, noAnomalyPatternName);

                    row.append(SEP).append(String.format("%.4f", value));
                }

                lines.add(row.toString());
            }

            // ===== WRITE FILE =====
            System.out.println("Writing data to: " + file);
            ResultWriter.appendLinesToFile(lines, file);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static double getAnomalyRatio(Map<String, List<Integer>> patternResults, String patternName, String noAnomalyPatternName) {
        if (patternResults == null) return 0.0;

        List<Integer> patternValues = patternResults.get(patternName); 
        if(patternValues == null){ 
            System.out.println("Pattern values for: " + patternName + " not found");
        }

        List<Integer> noAnomalyPatternValues = patternResults.get(noAnomalyPatternName);
        if(noAnomalyPatternValues == null){ 
            System.out.println("NoAnomalyPattern values for: " + noAnomalyPatternName + " not found");
        }

        if (patternValues == null || noAnomalyPatternValues == null) return 0.0;

        int fullSize = patternValues.size(); // TUTTI
        int noAnomalySize = noAnomalyPatternValues.size(); // SENZA PATTERN CON ANOMALIE
        //SCOPO: CON ANOMALIE / TUTTI -> 
        int anomalySize = fullSize - noAnomalySize; // SOLO CON ANOMALIE

        if (fullSize == 0) {
            System.out.println("Division by zero for pattern: " + patternName);
            return 0.0;
        }

        double ratio = (double) anomalySize / fullSize;

        //System.out.println("fullSize: " + fullSize + " noAnomalySize: " + noAnomalySize + " ratio: " + ratio);

        return ratio;
    }

    public static void writePatternCount(Results results) {
        if (results == null || results.getCollections().isEmpty()) return;

        try {
            Path overleafDir = results.getResultDir().resolve("overleaf");
            Files.createDirectories(overleafDir);

            Path file = overleafDir.resolve("patternCount_50_percentile.dat");
            ResultWriter.createEmptyFile(file);

            List<String> lines = new ArrayList<>();

            // ===== FILES =====
            String delta = "50_percentile";

            // ===== COLUMNS =====
            List<String> patterns = Arrays.asList(
                InStar.class.getSimpleName(),
                GiveAndTake.class.getSimpleName(),
                ReceiveAndForwardNFT.class.getSimpleName(),
                SameNFTChain.class.getSimpleName(),
                SameNFTCycle.class.getSimpleName()
            );

            // ===== ROWS =====
            List<String> collections = Arrays.asList(
                "axie_infinity_assets",
                //"decentraland_assets",
                "decentraland_estate",
                "decentraland_land",
                "decentraland_names",
                //"the_sandbox_assets",
                "the_sandbox_land"
            );

            // ===== HEADER =====
            StringBuilder header = new StringBuilder("Collection");
            for (String pattern : patterns) {
                header.append(SEP).append(pattern);
            }
            lines.add(header.toString());

            // ===== ROWS =====
            for (String collectionName : collections) {

                Collection collection = results.getCollection(collectionName);
                if (collection == null) {
                    System.out.println("PatternCount collection not found: " + collectionName);
                    continue;
                }

                StringBuilder row = new StringBuilder(formatCollection(collectionName));

                Delta d = collection.getDelta(delta);
                if (d == null) {
                    System.out.println("PatternCount delta not found for collection: " + collectionName);
                    continue;
                }

                Map<String, List<Integer>> patternResults = d.getPatternResults();

                for (String pattern : patterns) {

                    int value = 0;

                    if (patternResults != null) {
                        List<Integer> values = patternResults.get(pattern);

                        if (values == null) {
                            System.out.println("Pattern " + pattern + " not found in " + collectionName);
                        } else {
                            value = values.size();
                        }
                    }

                    row.append(SEP).append(value);
                }

                lines.add(row.toString());
            }

            // ===== WRITE FILE =====
            System.out.println("Writing data to: " + file);
            ResultWriter.appendLinesToFile(lines, file);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeMaxPatternLength(Results results){
        if (results == null || results.getCollections().isEmpty()) return;

        // ===== FILES =====
        List<String> patterns = Arrays.asList(
            InStar.class.getSimpleName(),
            GiveAndTake.class.getSimpleName(),
            ReceiveAndForwardNFT.class.getSimpleName(),
            SameNFTChain.class.getSimpleName(),
            SameNFTCycle.class.getSimpleName()
        );

        for (String pattern : patterns) {
            do_writeMaxPatternLength(results, pattern);
        }
    }
    private static void do_writeMaxPatternLength(Results results, String patternName) {

        try {
            Path overleafDir = results.getResultDir().resolve("overleaf");
            Files.createDirectories(overleafDir);

            Path file = overleafDir.resolve("maxPatternLength_" + patternName + ".dat");
            ResultWriter.createEmptyFile(file);

            List<String> lines = new ArrayList<>();

            // ===== COLUMNS =====
            List<String> deltas = Arrays.asList(
                "25_percentile",
                "50_percentile",
                "75_percentile",
                "100_percentile",
                "duration"
            );

            // ===== ROWS =====
            List<String> collections = Arrays.asList(
                "axie_infinity_assets",
                //"decentraland_assets",
                "decentraland_estate",
                "decentraland_land",
                "decentraland_names",
                //"the_sandbox_assets",
                "the_sandbox_land"
            );

            // ===== HEADER =====
            StringBuilder header = new StringBuilder("Collection");
            for (String delta : deltas) {
                header.append(SEP).append(formatDelta(delta));
            }
            lines.add(header.toString());

            // ===== ROWS =====
            for (String collectionName : collections) {

                Collection collection = results.getCollection(collectionName);
                if (collection == null) {
                    System.out.println("MaxPatternLength collection not found: " + collectionName);
                    continue;
                }

                StringBuilder row = new StringBuilder(formatCollection(collectionName));

                for (String delta : deltas) {

                    Delta d = collection.getDelta(delta);
                    if (d == null) {
                        System.out.println("MaxPatternLength in collection: " + collectionName + " delta not found: " + delta);
                        row.append(SEP).append("0");
                        continue;
                    }

                    int value = getMaxLength(d.getPatternResults(), patternName);

                    row.append(SEP).append(value);
                }

                lines.add(row.toString());
            }

            // ===== WRITE FILE =====
            System.out.println("Writing data to: " + file);
            ResultWriter.appendLinesToFile(lines, file);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static int getMaxLength(Map<String, List<Integer>> patternResults, String patternName) {
        if (patternResults == null) return 0;

        List<Integer> values = patternResults.get(patternName);

        if (values == null || values.isEmpty()) {
            System.out.println("MaxLength values not found for pattern: " + patternName);
            return 0;
        }

        int max = 0;
        for (int v : values) {
            if (v > max) max = v;
        }

        return max;
    }
    
    public static void writeTotalPatternCount(Results results){
        if (results == null || results.getCollections().isEmpty()) return;

        // ===== FILES =====
        List<String> patterns = Arrays.asList(
            InStar.class.getSimpleName(),
            GiveAndTake.class.getSimpleName(),
            ReceiveAndForwardNFT.class.getSimpleName(),
            SameNFTChain.class.getSimpleName(),
            SameNFTCycle.class.getSimpleName()
        );

        for (String pattern : patterns) {
            do_writeTotalPatternCount(results, pattern);
        }
    }
    private static void do_writeTotalPatternCount(Results results, String patternName) {

        try {
            Path overleafDir = results.getResultDir().resolve("overleaf");
            Files.createDirectories(overleafDir);

            Path file = overleafDir.resolve("totalCountDelta_" + patternName + ".dat");
            ResultWriter.createEmptyFile(file);

            List<String> lines = new ArrayList<>();

            // ===== COLUMNS =====
            List<String> deltas = Arrays.asList(
                "25_percentile",
                "50_percentile",
                "75_percentile",
                "100_percentile",
                "duration"
            );

            // ===== ROWS =====
            List<String> collections = Arrays.asList(
                "axie_infinity_assets",
                //"decentraland_assets",
                "decentraland_estate",
                "decentraland_land",
                "decentraland_names",
                //"the_sandbox_assets",
                "the_sandbox_land"
            );

            // ===== HEADER =====
            StringBuilder header = new StringBuilder("Collection");
            for (String delta : deltas) {
                header.append(SEP).append(formatDelta(delta));
            }
            lines.add(header.toString());

            // ===== ROWS =====
            for (String collectionName : collections) {

                Collection collection = results.getCollection(collectionName);
                if (collection == null) {
                    System.out.println("TotalPatternCount collection not found: " + collectionName);
                    continue;
                }

                StringBuilder row = new StringBuilder(formatCollection(collectionName));

                for (String delta : deltas) {

                    Delta d = collection.getDelta(delta);
                    if (d == null) {
                        System.out.println("TotalPatternCount in collection: " + collectionName + " delta not found: " + delta);
                        row.append(SEP).append("0");
                        continue;
                    }

                    int value = getCount(d.getPatternResults(), patternName);

                    row.append(SEP).append(value);
                }

                lines.add(row.toString());
            }

            // ===== WRITE FILE =====
            System.out.println("Writing data to: " + file);
            ResultWriter.appendLinesToFile(lines, file);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static int getCount(Map<String, List<Integer>> patternResults, String patternName) {
        if (patternResults == null) return 0;

        List<Integer> values = patternResults.get(patternName);

        if (values == null || values.isEmpty()) {
            System.out.println("TotalPatternCount values not found for pattern: " + patternName);
            return 0;
        }

        return values.size();
    }
    
    public static void writeTableCollectionInfo(Results results) {
        if (results == null || results.getCollections().isEmpty()) return;

        try {
            Path outDir = results.getResultDir().resolve("overleaf");
            Files.createDirectories(outDir);

            Path file = outDir.resolve("tableCollectionInfo.tex");
            ResultWriter.createEmptyFile(file);

            List<String> lines = new ArrayList<>();

            // HEADER LATEX
            lines.add("\\begin{tabular}{lcccccc}");
            lines.add("\\toprule");
            lines.add("Collection & Id & nNodes & nEdges & nNfts & nAnomalies & APercent \\\\");
            lines.add("\\midrule");

            // MAP
            Map<String, String> idMap = new LinkedHashMap<>();
            idMap.put("axie_infinity_assets", "aiA");
            idMap.put("decentraland_assets", "dA");
            idMap.put("decentraland_estate", "dE");
            idMap.put("decentraland_land", "dL");
            idMap.put("decentraland_names", "dN");
            idMap.put("the_sandbox_assets", "sbA");
            idMap.put("the_sandbox_land", "sbL");

            for (Map.Entry<String, String> entry : idMap.entrySet()) {

                String collectionName = entry.getKey();
                String id = entry.getValue();

                Collection c = results.getCollection(collectionName);
                if (c == null) continue;

                long nodes = c.getTotNodes();
                long edges = c.getTotEdges();
                long nfts = c.getTotNfts();
                long anomalies = c.getTotAnomalies();

                double perc = 0.0;
                long denom = edges - nfts;
                if (denom > 0) {
                    perc = (double) anomalies * 100 / denom;
                }

                lines.add(
                    collectionName.replaceAll("_", " ") + " & " +
                    id + " & " +
                    nodes + " & " +
                    edges + " & " +
                    nfts + " & " +
                    anomalies + " & " +
                    String.format("%.2f", perc) +
                    " \\\\"
                );
            }

            lines.add("\\bottomrule");
            lines.add("\\end{tabular}");

            ResultWriter.appendLinesToFile(lines, file);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void writeTableCollectionPercentiles(Results results) {
        if (results == null || results.getCollections().isEmpty()) return;

        try {
            Path outDir = results.getResultDir().resolve("overleaf");
            Files.createDirectories(outDir);

            Path file = outDir.resolve("tableCollectionPercentiles.tex");
            ResultWriter.createEmptyFile(file);

            List<String> lines = new ArrayList<>();

            // HEADER LATEX

            lines.add("\\begin{tabular}{lccccc}");
            lines.add("\\toprule");
            lines.add("Id & 25p & 50p & 75p & 100p & duration \\\\");
            lines.add("\\midrule");

            // MAP
            Map<String, String> idMap = new LinkedHashMap<>();
            idMap.put("axie_infinity_assets", "aiA");
            idMap.put("decentraland_assets", "dA");
            idMap.put("decentraland_estate", "dE");
            idMap.put("decentraland_land", "dL");
            idMap.put("decentraland_names", "dN");
            idMap.put("the_sandbox_assets", "sbA");
            idMap.put("the_sandbox_land", "sbL");

            List<String> deltas = Arrays.asList(
                "25_percentile",
                "50_percentile",
                "75_percentile",
                "100_percentile",
                "duration"
            );

            for (Map.Entry<String, String> entry : idMap.entrySet()) {

                String collectionName = entry.getKey();
                String id = entry.getValue();

                Collection c = results.getCollection(collectionName);
                if (c == null) continue;

                StringBuilder row = new StringBuilder();
                row.append(id);

                for (String deltaLabel : deltas) {
                    Delta d = c.getDelta(deltaLabel);
                    row.append(" & ").append(d != null ? d.getValue() : 0);
                }

                row.append(" \\\\");
                lines.add(row.toString());
            }

            lines.add("\\bottomrule");
            lines.add("\\end{tabular}");

            ResultWriter.appendLinesToFile(lines, file);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writePatternSizeDistribution(Results results){
        if (results == null || results.getCollections().isEmpty()) return;

        // ===== FILES =====
        List<String> patterns = Arrays.asList(
            InStar.class.getSimpleName(),
            GiveAndTake.class.getSimpleName(),
            ReceiveAndForwardNFT.class.getSimpleName(),
            SameNFTChain.class.getSimpleName(),
            SameNFTCycle.class.getSimpleName()
        );

        for (String pattern : patterns) {
            do_writePatternSizeDistribution(results, pattern);
        }
    }
    private static void do_writePatternSizeDistribution(Results results, String patternName) {

        try {
            Path overleafDir = results.getResultDir().resolve("overleaf");
            Files.createDirectories(overleafDir);

            Path file = overleafDir.resolve("patternSizeDistribution_" + patternName + ".dat");
            ResultWriter.createEmptyFile(file);

            List<String> lines = new ArrayList<>();

            // ===== SETTINGS =====
            String delta = "50_percentile";
            int maxK = 20;

            // ===== COLUMNS =====
            List<String> collections = Arrays.asList(
                "axie_infinity_assets",
                //"decentraland_assets",
                "decentraland_estate",
                "decentraland_land",
                "decentraland_names",
                //"the_sandbox_assets",
                "the_sandbox_land"
            );

            // ===== HEADER =====
            StringBuilder header = new StringBuilder("k");
            for (String collectionName : collections) {
                header.append(SEP).append(formatCollection(collectionName));
            }
            lines.add(header.toString());

            // ===== ROWS =====
            for (int k = 1; k <= maxK; k++) {

                StringBuilder row = new StringBuilder(String.valueOf(k));

                for (String collectionName : collections) {

                    Collection collection = results.getCollection(collectionName);
                    if (collection == null) {
                        System.out.println("PatternSizeDist collection not found: " + collectionName);
                        row.append(SEP).append("0");
                        continue;
                    }

                    Delta d = collection.getDelta(delta);
                    if (d == null) {
                        System.out.println("PatternSizeDist delta not found: " + delta + " in " + collectionName);
                        row.append(SEP).append("0");
                        continue;
                    }

                    int value = getCountBySize(d.getPatternResults(), patternName, k);

                    row.append(SEP).append(value);
                }

                lines.add(row.toString());
            }

            // ===== WRITE FILE =====
            System.out.println("Writing data to: " + file);
            ResultWriter.appendLinesToFile(lines, file);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static int getCountBySize(Map<String, List<Integer>> patternResults, String patternName, int k) {
    if (patternResults == null) return 0;

    List<Integer> values = patternResults.get(patternName);

    if (values == null || values.isEmpty()) {
        System.out.println("PatternSizeDist values not found for pattern: " + patternName);
        return 0;
    }

    int count = 0;
    for (int v : values) {
        if (v == k) count++;
    }

    return count;
}

}