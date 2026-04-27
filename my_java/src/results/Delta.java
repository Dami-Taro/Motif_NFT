package src.results;

import java.util.*;
import src.motifMiner.patterns.Pattern;

public class Delta {

    private String label;
    private long value = -1;

    // patternName -> lista delle size dei pattern
    private Map<String, List<Integer>> patternResults = new HashMap<>();

    // ===== GETTER & SETTER =====

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public Map<String, List<Integer>> getPatternResults() {
        return patternResults;
    }

    public void setPatternResults(Map<String, List<Integer>> patternResults) {
        this.patternResults = patternResults;
    }

    // ===== LOGICA =====

    public void addPattern(List<? extends Pattern> patterns) {
        if( patterns == null ){ System.out.println(" Delta "+this.label+": failed to add pattern null\n"); return;}
        if( patterns.isEmpty()){ System.out.println(" Delta "+this.label+": failed to add pattern vuoto\n"); return;}

        if (patternResults == null) {
            patternResults = new HashMap<>();
        }

        String patternName = patterns.get(0).getName();

        List<Integer> sizes = new ArrayList<>();
        for (Pattern p : patterns) {
            sizes.add(p.getSize());
        }

        patternResults.put(patternName, sizes);
    }

    public void addPattern(String name, List<? extends Pattern> patterns) {
        if( patterns == null ){ System.out.println(" Delta "+this.label+": failed to add pattern null\n"); return;}
        if( patterns.isEmpty()){ System.out.println(" Delta "+this.label+": failed to add pattern vuoto\n"); return;}
        if( name == null || name.isEmpty() ){ System.out.println(" Delta "+this.label+": failed to add pattern: nome vuoto\n"); return;}

        if (patternResults == null) {
            patternResults = new HashMap<>();
        }

        String patternName = name;

        List<Integer> sizes = new ArrayList<>();
        for (Pattern p : patterns) {
            sizes.add(p.getSize());
        }

        patternResults.put(patternName, sizes);
}
}