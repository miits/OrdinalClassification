package org.ordinalclassification.classifiers;

import org.ordinalclassification.types.DistanceArray;
import org.ordinalclassification.types.LearningExampleType;
import org.rulelearn.measures.HVDM;

import java.util.*;

import static java.util.stream.Collectors.toMap;

public abstract class NearestNeighborsAnalyzer {
    protected HVDM measure;
    protected int[] majorityIndices;
    protected int[] minorityIndices;
    protected DistanceArray distances;
    protected HashMap<Integer, LearningExampleType> labelsAssignment;

    public NearestNeighborsAnalyzer(HVDM measure, int[] majorityIndices, int[] minorityIndices) {
        this.measure = measure;
        this.majorityIndices = majorityIndices;
        this.minorityIndices = minorityIndices;
        this.labelsAssignment = new HashMap<>();
        this.distances = new DistanceArray(measure);
    }

    public void labelExamples() {
        for (int index: minorityIndices) {
            labelsAssignment.put(index, labelExample(index));
        }
    }

    public HashMap<Integer, LearningExampleType> getLabelsAssignment() {
        return labelsAssignment;
    }

    protected LearningExampleType labelExample(int exampleIndex) { return LearningExampleType.SAFE; }

    protected int[] getObjectsIndicesSortedByDistance(HashMap<Integer, Double> distances) {
        LinkedHashMap<Integer, Double> sorted =  distances
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
