package src.results;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Results {

    private transient Path resultDir;
    private String resultDirString;
    // collectionName -> Collection
    private Map<String, Collection> collections = new LinkedHashMap<>();

    // ===== GETTER & SETTER =====

    public Map<String, Collection> getCollections() {
        return collections;
    }

    public void setCollections(Map<String, Collection> collections) {
        this.collections = collections;
    }

    public Path getResultDir() {
        return resultDir;
    }

    public void setResultDir(Path resultDir) {
        this.resultDir = resultDir;
    }

    // ===== LOGICA =====

    public void addCollection(Collection collection) {
        if (collection == null || collection.getName() == null) return;
        collections.put(collection.getName(), collection);
    }

    public Collection getCollection(String name) {
        return collections.get(name);
    }

    public void setResultDir(List<src.main.FileInfos> fileList) {
        if (fileList == null || fileList.isEmpty()) return;

        Path firstPath = fileList.get(0).getPath();

        if (firstPath != null) {
            this.resultDir = firstPath.getParent();
        }
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Results:\n");
        for (Collection c : collections.values()) {
            sb.append("- Collection: ").append(c.getName()).append("\n");
            sb.append("  TotEdges:\t").append(c.getTotEdges()).append("\n");
            sb.append("  TotNodes:\t").append(c.getTotNodes()).append("\n");
            sb.append("  TotNFTs:\t").append(c.getTotNfts()).append("\n");
            sb.append("  TotAnomalies:\t").append(c.getTotAnomalies()).append("\n");
            sb.append("  TotAnomalNFTs:\t").append(c.getTotAnomalNfts()).append("\n");
            sb.append("  Deltas:\n");
            for (Map.Entry<String, Delta> entry : c.getDeltas().entrySet()) {
                String label = entry.getKey();
                Delta delta = entry.getValue();
                sb.append("    - ").append(label).append(": value=").append(delta.getValue()).append(", patternResults:\n");
                for (Map.Entry<String, List<Integer>> patternEntry : delta.getPatternResults().entrySet()) {
                    String patternName = patternEntry.getKey();
                    int size = patternEntry.getValue().size();
                    sb.append("      - ").append(patternName).append(": size=").append(size).append("\n");
                }
            }
        }

        return sb.toString();
    }

    // ===== JSON SERIALIZATION =====
    public void saveToJson(Path outputFile) {
        try {
            if (outputFile.getParent() != null) {
                Files.createDirectories(outputFile.getParent());
            }

            // sincronizza stringa prima del salvataggio
            if (resultDir != null) {
                resultDirString = resultDir.toString();
            }

            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

            try (Writer writer = Files.newBufferedWriter(outputFile)) {
                gson.toJson(this, writer);
            }

            System.out.println("✅ Results salvato in: " + outputFile);

        } catch (IOException e) {
            System.err.println("❌ Errore salvataggio Results");
            e.printStackTrace();
        }
    }

    public static Results loadFromJson(Path inputFile) {
        try {
            Gson gson = new GsonBuilder().create();

            try (Reader reader = Files.newBufferedReader(inputFile)) {

                Results results = gson.fromJson(reader, Results.class);

                // ricostruzione Path
                if (results != null && results.resultDirString != null) {
                    results.resultDir = Paths.get(results.resultDirString);
                }

                return results;
            }

        } catch (IOException e) {
            System.err.println("❌ Errore caricamento Results");
            e.printStackTrace();
        }

        return null;
    }
}