package org.ordinalclassification.types;

import org.rulelearn.measures.HVDM;

import java.util.HashMap;

public class DistanceArray {
    private int examplesCount;
    private HVDM measure;
    private HashMap<Integer, HashMap<Integer, Double>> distances;

    public DistanceArray(HVDM measure) {
        this.examplesCount = measure.getData().getNumberOfObjects();
        this.measure = measure;
        distances = new HashMap<>();
        calculateDistances();
    }

    private void calculateDistances() {
        for (int i = 1; i < examplesCount; i++) {
            HashMap<Integer, Double> exampleDistances = new HashMap<>();
            for (int j = 0; j < i; j++) {
                exampleDistances.put(j, measure.measureDistance(i, j));
            }
            distances.put(i, exampleDistances);
        }
    }

    public double getDistance(int xIndex, int yIndex) {
        if (xIndex == yIndex) {
            return 0;
        }
        return xIndex > yIndex ? distances.get(xIndex).get(yIndex) : distances.get(yIndex).get(xIndex);
    }

    public HashMap<Integer, Double> getExampleDistances(int exampleIndex) {
        HashMap<Integer, Double> exampleDistances = new HashMap<>();
        for (int i = 0; i < exampleIndex; i++) {
            exampleDistances.put(i, distances.get(exampleIndex).get(i));
        }
        for (int i = exampleIndex + 1; i < examplesCount; i++) {
            exampleDistances.put(i, distances.get(i).get(exampleIndex));
        }
        return exampleDistances;
    }
}
