package put.classifiers;

import put.measures.HVDM;

import java.util.*;

import static java.util.stream.Collectors.toMap;

public abstract class NearestNeighborsAnalyzer {
    protected HVDM measure;
    protected int[] majorityIndices;
    protected int[] minorityIndices;
    protected HashMap<Integer, HashMap<Integer, Double>> distances;

    public NearestNeighborsAnalyzer(HVDM measure, int[] majorityIndices, int[] minorityIndices) {
        this.measure = measure;
        this.majorityIndices = majorityIndices;
        this.minorityIndices = minorityIndices;
        this.distances = new HashMap<>();
        calculateDistances();
    }

    private void calculateDistances() {
        for (int exampleIndex: minorityIndices) {
            HashMap<Integer, Double> exampleDistances = new HashMap<>();
            for (int objectIndex: majorityIndices) {
                exampleDistances.put(objectIndex, measure.measureDistance(exampleIndex, objectIndex));
            }
            distances.put(exampleIndex, exampleDistances);
        }
    }

    protected int[] getObjectsIndicesSortedByDistance(int exampleIndex) {
        LinkedHashMap<Integer, Double> sorted =  distances.get(exampleIndex)
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(
                        toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                LinkedHashMap::new));
        int[] indices = new int[sorted.size()];
        int i = 0;
        for (int key: sorted.keySet()) {
            indices[i] = key;
            i++;
        }
        return indices;
    }
}
