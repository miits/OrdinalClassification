package put.classifiers;

import org.rulelearn.data.InformationTable;
import put.measures.HVDM;

import java.util.Arrays;
import java.util.Comparator;

public abstract class NearestNeighborsAnalyzer {
    protected HVDM measure;
    protected InformationTable data;
    protected InformationTable examples;
    protected int numberOfObjects;
    protected int numberOfExamples;
    protected double[][] distances;

    protected class DistanceComparator implements Comparator<Integer> {
        double[] distances;

        DistanceComparator(double[] distances) {
            this.distances = distances;
        }

        public Integer[] createIndexArray()
        {
            Integer[] indexes = new Integer[distances.length];
            for (int i = 0; i < distances.length; i++)
            {
                indexes[i] = i;
            }
            return indexes;
        }

        @Override
        public int compare(Integer o1, Integer o2) {
            if (distances[o1] < distances[o2]) return -1;
            if (distances[o1] > distances[o2]) return 1;
            return 0;
        }
    }

    public NearestNeighborsAnalyzer(HVDM measure, InformationTable data, InformationTable examples) {
        this.measure = measure;
        this.data = data;
        this.examples = examples;
        numberOfExamples = examples.getNumberOfObjects();
        numberOfObjects = data.getNumberOfObjects();
        calculateDistances();
    }

    private void calculateDistances() {
        for (int exampleIndex = 0; exampleIndex < numberOfExamples; exampleIndex++) {
            for (int objectIndex = 0; objectIndex < numberOfObjects; objectIndex++) {
                InformationTable x = examples.select(new int[]{exampleIndex}, true);
                InformationTable y = examples.select(new int[]{objectIndex}, true);
                distances[exampleIndex][objectIndex] = measure.measureDistance(x, y);
            }
        }
    }

    protected Integer[] getObjectsIndicesSortedByDistance(int exampleIndex) {
        DistanceComparator comparator = new DistanceComparator(distances[exampleIndex]);
        Integer[] indices = comparator.createIndexArray();
        Arrays.sort(indices, comparator);
        return indices;
    }
}
