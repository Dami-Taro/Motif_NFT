package src.results;

import java.nio.file.Path;
import java.util.*;

public class Results {

    private Path resultDir;
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
}