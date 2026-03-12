package src.main;

import java.util.ArrayList;
import java.util.List;

import src.graph.Edge;
import src.graph.Graph;
import src.graph.MergedIterator;
import src.graph.UserNode;
//import src.io.TimeFormatter;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;

public class DeltaEstimator {
    
    private final Graph graph;
    private final List<Double> interEventTimes;

    public DeltaEstimator(Graph graph) {
        this.graph = graph;
        this.interEventTimes = setMergedInterEventTime();
    }

    private List<Double> setMergedInterEventTime() {
        List<Double> interEventTimes = new ArrayList<>();

        for (UserNode node : graph.getNodes()) {

            if (node.getIncomingEdges().size() + node.getOutgoingEdges().size() < 2) {
                continue;
            }

            MergedIterator mergedIt = new MergedIterator(
                node.getOutgoingEdges(),
                node.getIncomingEdges()
            );

            Edge prev = null;

            while (mergedIt.hasNext()) {
                Edge current = mergedIt.next();

                if (prev != null) {
                    long interEventTime = current.getTimestamp() - prev.getTimestamp();

                    // difesa minimale: evita valori negativi o nulli
                    if (interEventTime > 0) {
                        interEventTimes.add((double) interEventTime);
                    }
                }

                prev = current;
            }
        }

        return interEventTimes;
    }

    long getPercentile(double percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100");
        }

        double[] values = interEventTimes.stream().mapToDouble(Double::doubleValue).toArray();
        //System.out.println("interEventTime.size() = " + interEventTimes.size() + ", values.length = " + values.length);

        Percentile percentile = new Percentile();
        double value = percentile.evaluate(values, percentage);
        
        //System.out.println(percentage + "th-percentile: " + value + " = " + (long) value + " sec = " + TimeFormatter.secondsToSimpleString((long) value));
        if (Double.isNaN(value)) {
            return 0;
        }

        return (long) value;
    }


}