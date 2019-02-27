package put.classifiers;

import org.rulelearn.data.Decision;
import org.rulelearn.data.InformationTable;
import put.measures.HVDM;
import put.types.LearningExampleType;
import put.utils.KNearestLabeler;

import java.util.Arrays;

public class KNNAnalyzer extends NearestNeighborsAnalyzer {
    private int k;
    private KNearestLabeler labeler;
    private LearningExampleType[] labelsAssignment;

    public KNNAnalyzer(HVDM measure, InformationTable data, InformationTable examples, int k, KNearestLabeler labeler) {
        super(measure, data, examples);
        this.k = k;
        this.labeler = labeler;
        this.labelsAssignment = new LearningExampleType[numberOfExamples];
    }

    public void labelExamples() {
        for (int i = 0; i < numberOfExamples; i++) {
            labelsAssignment[i] = labelExample(i);
        }
    }

    private LearningExampleType labelExample(int exampleIndex) {
        int[] kNearest = getKNearestIndices(exampleIndex);
        Decision[] decisions = getDecisions(kNearest);
        int sameClassAmount = countSameDecisions(decisions, examples.getDecision(exampleIndex));
        return labeler.labelExample(sameClassAmount);
    }

    private int[] getKNearestIndices(int exampleIndex) {
        Integer[] indices = getObjectsIndicesSortedByDistance(exampleIndex);
        int[] kNearesIndices = new int[k];
        for (int i = 0; i < k; i++) {
            kNearesIndices[i] = indices[i];
        }
        return kNearesIndices;
    }

    private Decision[] getDecisions(int[] objectIndices) {
        Decision[] decisions = new Decision[objectIndices.length];
        for (int index: objectIndices) {
            decisions[index] = data.getDecision(index);
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

    public LearningExampleType[] getLabelsAssignment() {
        return labelsAssignment;
    }
}
