package src.motifMiner;

import java.nio.file.Path;
import java.util.*;

import src.graph.DatasetNFT;
import src.graph.Edge;
import src.graph.UserNode;
import src.io.ResultWriter;
import src.motifMiner.patterns.Pattern;
import src.motifMiner.patterns.PatternValidationException;

public class Validator {

    /* ===============================
     * DATASET NFT VALIDATION
     * =============================== */

    public static void validateContiguousTransactions(
            DatasetNFT dataset,
            Path outputFile) {

        Map<String, List<Integer>> everyNftContiguousTransactions = new TreeMap<>();

        for (String nft : dataset.getNFTs()) {

            everyNftContiguousTransactions.put(nft, new ArrayList<>());

            NavigableSet<Edge> nftTransactions = dataset.getNFTTransactions(nft);
            if (nftTransactions == null || nftTransactions.isEmpty())
                continue;

            UserNode owner = nftTransactions.first().getFrom();
            int currentContiguous = 0;

            for (Edge edge : nftTransactions) {

                if (!edge.getFrom().equals(owner)) {
                    everyNftContiguousTransactions
                        .get(nft)
                        .add(currentContiguous);
                    currentContiguous = 0;
                }

                owner = edge.getTo();
                currentContiguous++;
            }

            everyNftContiguousTransactions
                .get(nft)
                .add(currentContiguous);
        }

        //ResultWriter.printNftData( everyNftContiguousTransactions, "CONTIGUOUS TRANSACTIONS PER NFT", outputFile );
    }

        /* ===============================
     * PATTERN VALIDATION
     * =============================== */

    public static <T extends Pattern> void validatePatterns(
            String patternName,
            List<T> patterns,
            Path outputFile) {

        int valid = 0;
        int invalid = 0;

        Map<String, Integer> errorCounts = new HashMap<>();

        for (Pattern p : patterns) {
            try {
                p.validate();
                valid++;
            } catch (PatternValidationException e) {
                invalid++;
                errorCounts.merge(e.getMessage(), 1, Integer::sum);
            }
        }

        appendValidationReport(
            patternName,
            patterns.size(),
            valid,
            invalid,
            errorCounts,
            outputFile
        );
    }

    private static void appendValidationReport(
        String patternName,
        int total,
        int valid,
        int invalid,
        Map<String, Integer> errors,
        Path outputFile) {

    StringBuilder sb = new StringBuilder();

    sb.append("\n=== VALIDATION REPORT: ")
        .append(patternName)
        .append(" ===\n");

    sb.append("Total patterns: ").append(total).append("\n");
    sb.append("Valid patterns: ").append(valid).append("\n");
    sb.append("Invalid patterns: ").append(invalid).append("\n");

    if (!errors.isEmpty()) {
        sb.append("\nErrors breakdown:\n");
        for (Map.Entry<String,Integer> entry : errors.entrySet()) {
            sb.append(" - ")
                .append(entry.getKey())
                .append(": ")
                .append(entry.getValue())
                .append("\n");
        }
    }

    sb.append("========================================\n");

        //ResultWriter.appendRawText(sb.toString(), outputFile);
    }


}