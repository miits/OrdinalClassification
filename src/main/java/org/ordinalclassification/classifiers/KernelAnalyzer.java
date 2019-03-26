package org.ordinalclassification.classifiers;

import org.ordinalclassification.types.LearningExampleType;
import org.rulelearn.data.Decision;
import org.rulelearn.measures.HVDM;
import org.ordinalclassification.utils.KernelLabeler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class KernelAnalyzer extends NearestNeighborsAnalyzer {
    private Decision majorityLimitingDecision;
    private Decision minorityLimitingDecision;
    private KernelLabeler labeler;
    private double kernelWidth;

    public KernelAnalyzer(HVDM measure, int[] majorityIndices, int[] minorityIndices, Decision majorityLimitingDecision, Decision minorityLimitingDecision, KernelLabeler labeler) {
        super(measure, majorityIndices, minorityIndices);
        this.majorityLimitingDecision = majorityLimitingDecision;
        this.minorityLimitingDecision = minorityLimitingDecision;
        this.labeler = labeler;
        setKernelWidth();
    }

    private void setKernelWidth() {
        double sum = 0;
        for (int i: minorityIndices) {
            sum += getDistanceToNthNeighbour(i, 5);
        }
        kernelWidth = sum / (double) minorityIndices.length;
    }

    private double getDistanceToNthNeighbour(int exampleIndex, int n) {
        HashMap<Integer, Double> exampleDistances = distances.getExampleDistances(exampleIndex);
        int[] objectsIndicesSorted = getObjectsIndicesSortedByDistance(exampleIndex, exampleDistances);
        return distances.getDistance(exampleIndex, objectsIndicesSorted[n - 1]);
    }

    @Override
    protected LearningExampleType labelExample(int exampleIndex) {
        int[] inKernel = getIndicesOfObjectsInKernel(exampleIndex);
        double minorityDecisionWeightedSum = weightedCountForDecision(inKernel, exampleIndex, minorityLimitingDecision);
        double majorityDecisionWeightedSum = weightedCountForDecision(inKernel, exampleIndex, majorityLimitingDecision);
        double ratio = minorityDecisionWeightedSum / (minorityDecisionWeightedSum + majorityDecisionWeightedSum);
        return labeler.labelExample(ratio);
    }

    private int[] getIndicesOfObjectsInKernel(int exampleIndex) {
        HashMap<Integer, Double> exampleDistances = distances.getExampleDistances(exampleIndex);
        int[] objectsIndicesSorted = getObjectsIndicesSortedByDistance(exampleIndex, exampleDistances);
        ArrayList<Integer> inKernelWindow = new ArrayList();
        for (int i: objectsIndicesSorted) {
            if (exampleDistances.get(i) > kernelWidth) {
                break;
            }
            inKernelWindow.add(i);
        }
        int[] inKernelWindowArr = new int[inKernelWindow.size()];
        int i = 0;
        for (int index: inKernelWindow) {
            inKernelWindowArr[i] = index;
            i++;
        }
        return inKernelWindowArr;
    }

    private double weightedCountForDecision(int[] inKernel, int exampleIndex, Decision decision) {
        double sum = 0;
        for (int index: inKernel) {
            if (measure.getData().getDecision(index).equals(decision)) {
                sum += getEpanechnikov(distances.getDistance(exampleIndex, index));
            }
        }
        return sum;
    }

    private double getEpanechnikov(double distance) {
        return 3.0 / 4.0 * (1 - Math.pow(distance, 2));
    }
}
