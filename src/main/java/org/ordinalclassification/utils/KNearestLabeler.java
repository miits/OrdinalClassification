package org.ordinalclassification.utils;

import org.ordinalclassification.types.LearningExampleType;
import org.rulelearn.data.Decision;

import java.util.HashMap;

public class KNearestLabeler {
    private int safeLowerLimit;
    private int borderlineLowerLimit;
    private int rareLowerLimit;

    public KNearestLabeler(int safeLowerLimit, int borderlineLowerLimit, int rareLowerLimit) {
        this.safeLowerLimit = safeLowerLimit;
        this.borderlineLowerLimit = borderlineLowerLimit;
        this.rareLowerLimit = rareLowerLimit;
    }

    public LearningExampleType labelExample(int sameClassAmount) {
        if (sameClassAmount >= safeLowerLimit) return LearningExampleType.SAFE;
        if (sameClassAmount >= borderlineLowerLimit) return LearningExampleType.BORDERLINE;
        if (sameClassAmount >= rareLowerLimit) return LearningExampleType.RARE;
        return LearningExampleType.OUTLIER;
    }

    public LearningExampleType customLabel(int exampleIndex, HashMap<Integer, int[][]> neighbourhoods) {
        int[] minClassExamples = neighbourhoods.get(exampleIndex)[0];
        if (minClassExamples.length >= safeLowerLimit) return LearningExampleType.SAFE;
        if (minClassExamples.length >= borderlineLowerLimit) return LearningExampleType.BORDERLINE;
        if (minClassExamples.length == rareLowerLimit) {
            int[] minNeighbourhood = neighbourhoods.get(minClassExamples[0])[0];
            return rareExampleCheck(exampleIndex, minNeighbourhood);
        }
        return LearningExampleType.OUTLIER;
    }

    private LearningExampleType rareExampleCheck(int exampleIndex, int[] minNeighbourhood) {
        if (minNeighbourhoodNotEmpty(minNeighbourhood)) {
            if (minNeighbourhoodPointsToExample(exampleIndex, minNeighbourhood)) {
                return LearningExampleType.RARE;
            } else {
                return LearningExampleType.BORDERLINE;
            }
        }
        return LearningExampleType.RARE;
    }

    private boolean minNeighbourhoodPointsToExample(int exampleIndex, int[] minNeighbourhood) {
        return minNeighbourhood[0] == exampleIndex;
    }

    private boolean minNeighbourhoodNotEmpty(int[] minNeighbourhood) {
        return minNeighbourhood.length > 0;
    }
}
