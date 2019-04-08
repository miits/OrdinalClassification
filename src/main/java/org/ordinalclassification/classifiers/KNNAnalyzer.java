package org.ordinalclassification.classifiers;

import org.rulelearn.data.Decision;
import org.rulelearn.measures.HVDM;
import org.ordinalclassification.types.LearningExampleType;
import org.ordinalclassification.utils.KNearestLabeler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class KNNAnalyzer extends NearestNeighborsAnalyzer {
    private int k;
    private KNearestLabeler labeler;
    private HashMap<Integer, int[][]> neighbourhoods;

    public KNNAnalyzer(HVDM measure, int[] majorityIndices, int[] minorityIndices, int k, KNearestLabeler labeler) {
        super(measure, majorityIndices, minorityIndices);
        this.k = k;
        this.labeler = labeler;
        buildNeighbourhoods();
    }

    @Override
    protected LearningExampleType labelExample(int exampleIndex) {
        return labeler.customLabel(exampleIndex, neighbourhoods);
    }

    private void buildNeighbourhoods() {
        neighbourhoods = new HashMap<>();
        for (int minorityExampleIndex: minorityIndices) {
            int[] kNearest = getKNearestIndices(minorityExampleIndex);
            Decision[] decisions = getDecisions(kNearest);
            int[][] divided = divideNeighbours(kNearest, decisions, measure.getData().getDecision(minorityExampleIndex));
            neighbourhoods.put(minorityExampleIndex, divided);
        }
    }

    private int[] getKNearestIndices(int exampleIndex) {
        HashMap<Integer, Double> exampleDistances = distances.getExampleDistances(exampleIndex);
        int[] indices = getObjectsIndicesSortedByDistance(exampleDistances);
        int[] kNearesIndices = new int[k];
        for (int i = 0; i < k; i++) {
            kNearesIndices[i] = indices[i];
        }
        return kNearesIndices;
    }

    private Decision[] getDecisions(int[] objectIndices) {
        Decision[] decisions = new Decision[objectIndices.length];
        int i = 0;
        for (int index: objectIndices) {
            decisions[i] = measure.getData().getDecision(index);
            i++;
        }
        return decisions;
    }

    private int[][] divideNeighbours(int[] kNearest, Decision[] decisions, Decision minDecision) {
        ArrayList<Integer> minNeighbours = new ArrayList<>();
        ArrayList<Integer> majNeighbours = new ArrayList<>();
        for (int i = 0; i < kNearest.length; i++) {
            if (decisions[i].equals(minDecision)) {
                minNeighbours.add(kNearest[i]);
            } else {
                majNeighbours.add(kNearest[i]);
            }
        }
        int[][] divided = new int[2][];
        divided[0] = minNeighbours.stream().mapToInt(x -> x).toArray();
        divided[1] = majNeighbours.stream().mapToInt(x -> x).toArray();
        return divided;
    }

    private int countSameDecisions(Decision[] decisions, Decision toCompare) {
        int count = 0;
        for (Decision decision: decisions) {
            if (decision.equals(toCompare)) {
                count++;
            }
        }
        return count;
    }
}
