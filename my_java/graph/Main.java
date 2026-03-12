package graph;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws Exception {
        Graph g = GraphLoader.loadFromJson(Paths.get("boredapeyachtclub.json"));

        System.out.println("Nodi: " + g.getNodes().size());
        System.out.println("Archi: " + g.getTotalEdges());

        TemporalMotifMiner miner = new TemporalMotifMiner(g, 604800); // delta = 1 week = 604800 secondi
        
        TemporalMotifMiner.PatternResult<TemporalMotifMiner.OneWayCouple>
                oneWayCouplePatterns = miner.findOneWayCouples(2);

        TemporalMotifMiner.PatternResult<TemporalMotifMiner.InStar>
                inStarPatterns = miner.findInStars(3);

        TemporalMotifMiner.PatternResult<TemporalMotifMiner.GiveAndTake>
                giveAndTakePatterns = miner.findGiveAndTakes(2);

        // ===== SCRITTURA SU FILE =====
        Path output = Paths.get("temporal_motifs_results.txt");

        try (BufferedWriter writer = Files.newBufferedWriter(output)) {

            writer.write("=== TEMPORAL MOTIF MINING RESULTS ===\n\n");

            writer.write("Nodi: " + g.getNodes().size() + "\n");
            writer.write("Archi: " + g.getTotalEdges() + "\n");
            writer.write("Delta (sec): 604800\n\n");

            writer.write("=== OneWayCouple ===\n");
            writer.write(oneWayCouplePatterns.toString());
            writer.write("\n\n");

            writer.write("=== InStar ===\n");
            writer.write(inStarPatterns.toString());
            writer.write("\n\n");

            writer.write("=== GiveAndTake ===\n");
            writer.write(giveAndTakePatterns.toString());
            writer.write("\n");

        }

        System.out.println("✅ Risultati scritti in temporal_motifs_results.txt");

        /*
        List<TemporalMotifMiner.GiveAndTake> giveAndTakePatterns = miner.findGiveAndTakes(2);
        System.out.println("Mined GiveAndTake:");
        printPatternList(giveAndTakePatterns);
        List<TemporalMotifMiner.OneWayCouple> oneWayCouplesPatterns = miner.findOneWayCouples(2);
        System.out.println("Mined OneWayCouple:");
        printPatternList(oneWayCouplesPatterns);
        List<TemporalMotifMiner.InStar> inStarPatterns = miner.findInStars(3);
        System.out.println("Mined InStar:");
        printPatternList(inStarPatterns);
        */
        
    }
    /*
    private static void printPatternList(List<?> patterns) {
        Map<Integer, Integer> patternCount = new HashMap<>();
        for (Object pattern : patterns) {
            if(pattern instanceof TemporalMotifMiner.OneWayCouple){
                int length = ((TemporalMotifMiner.OneWayCouple) pattern).getEdges().size();
                patternCount.put(length, patternCount.getOrDefault(length, 0) + 1);
                continue;
            }
            if(pattern instanceof TemporalMotifMiner.InStar){
                int length = ((TemporalMotifMiner.InStar) pattern).getEdges().size();
                patternCount.put(length, patternCount.getOrDefault(length, 0) + 1);
                continue;
            }
            if(pattern instanceof TemporalMotifMiner.GiveAndTake){
                int length = ((TemporalMotifMiner.GiveAndTake) pattern).getEdges().size()*2; //ogni ciclo ha 2 edges
                patternCount.put(length, patternCount.getOrDefault(length, 0) + 1);
                continue;
            }
        }
        for (int k = 3; k <= 21; k++) {
            System.out.println("\tPattern di lunghezza " + k + ": " + patternCount.getOrDefault(k, 0));
        }
    }
    */

}
