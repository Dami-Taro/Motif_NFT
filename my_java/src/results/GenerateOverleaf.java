package src.results;

import java.nio.file.Path;
import java.nio.file.Paths;

public class GenerateOverleaf {

    public static void main(String[] args) {

        Path resultsFile = Paths.get("results/DatasetJson_raw_entity/results.json");

        Results results = Results.loadFromJson(resultsFile);

        if (results == null) {
            System.err.println("❌ Impossible caricare Results");
            return;
        }

        System.out.println("=== Results caricati ===");

        // ===== OVERLEAF =====
        //OverleafWriter.writeDiffAnomalyCount(results);
        //OverleafWriter.writeRatioAnomalyCount(results);
        //OverleafWriter.writePatternCount(results);
        //OverleafWriter.writeMaxPatternLength(results);
        //OverleafWriter.writeTableCollectionInfo(results);
        //OverleafWriter.writeTotalPatternCount(results);
        //OverleafWriter.writeTableCollectionInfo(results);
        //OverleafWriter.writeTableCollectionPercentiles(results);
        //OverleafWriter.writePatternSizeDistribution(results);
        //OverleafWriter.writePatternSizeBoxPlot(results);
        //OverleafWriter.writePatternSizeCumulative(results);
        OverleafWriter.writeTableSizeOutlier(results);

        System.out.println("=== Overleaf files creati ===");
    }
}