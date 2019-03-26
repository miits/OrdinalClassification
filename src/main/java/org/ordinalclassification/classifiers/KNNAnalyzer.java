package org.ordinalclassification.classifiers;

import org.rulelearn.data.Decision;
import org.rulelearn.measures.HVDM;
import org.ordinalclassification.types.LearningExampleType;
import org.ordinalclassification.utils.KNearestLabeler;

import java.util.HashMap;

public class KNNAnalyzer extends NearestNeighborsAnalyzer {
    private int k;
    private KNearestLabeler labeler;

    public KNNAnalyzer(HVDM measure, int[] majorityIndices, int[] minorityIndices, int k, KNearestLabeler labeler) {
        super(measure, majorityIndices, minorityIndices);
        this.k = k;
        this.labeler = labeler;
    }

    @Override
    protected LearningExampleType labelExample(int exampleIndex) {
        int[] kNearest = getKNearestIndices(exampleIndex);
        Decision[] decisions = getDecisions(kNearest);
        int sameClassAmount = countSameDecisions(decisions, measure.getData().getDecision(exampleIndex));
        return labeler.labelExample(sameClassAmount);
    }

    private int[] getKNearestIndices(int exampleIndex) {
        HashMap<Integer, Double> exampleDistances = distances.getExampleDistances(exampleIndex);
        int[] indices = getObjectsIndicesSortedByDistance(exampleIndex, exampleDistances);
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
