package org.ordinalclassification.utils;

import org.rulelearn.approximations.ClassicalDominanceBasedRoughSetCalculator;
import org.rulelearn.approximations.Union;
import org.rulelearn.approximations.UnionsWithSingleLimitingDecision;
import org.rulelearn.data.*;

import java.util.HashMap;
import java.util.Set;

public class DataSubsetExtractor {
    private InformationTableWithDecisionDistributions data;
    private DecisionDistribution decisionDistribution;
    private Set<Decision> decisions;
    private int datasetSize;
    private Union[] atLeastUnions;
    private Union[] atMostUnions;
    private HashMap<Decision, int[]> classesByDecision;

    public InformationTableWithDecisionDistributions getData() {
        return data;
    }

    public DataSubsetExtractor(InformationTableWithDecisionDistributions data) {
        this.data = data;
        this.decisionDistribution = data.getDecisionDistribution();
        this.decisions = decisionDistribution.getDecisions();
        this.datasetSize = data.getDecisions().length;
        this.classesByDecision = buildClassesByDecision();
        buildUnions();
    }

    public Union[] getAtLeastUnions() {
        return atLeastUnions;
    }

    public Union[] getAtMostUnions() {
        return atMostUnions;
    }

    public HashMap<Decision, int[]> getClassesByDecision() {
        return classesByDecision;
    }

    public static double[] get2dArrayColumn(double[][] array, int index){
        double[] column = new double[array.length];
        for(int i = 0; i < column.length; i++){
            column[i] = array[i][index];
        }
        return column;
    }

    private void buildUnions() {
        UnionsWithSingleLimitingDecision unions = new UnionsWithSingleLimitingDecision(data, new ClassicalDominanceBasedRoughSetCalculator());
        this.atLeastUnions = unions.getUpwardUnions();
        this.atMostUnions = unions.getDownwardUnions();
    }

    private HashMap<Decision, int[]> buildClassesByDecision() {
        HashMap<Decision, int[]> classesByDecision = new HashMap<>();
        for (Decision decision: decisions) {
            int[] classIndices = selectIndicesWithDecision(decision);
            classesByDecision.put(decision, classIndices);
        }
        return classesByDecision;
    }

    private int[] selectIndicesWithDecision(Decision decision) {
        int classSize = decisionDistribution.getCount(decision);
        int[] classIndices = new int[classSize];
        int arrayIndex = 0;
        for (int i = 0; i < datasetSize; i++) {
            if (data.getDecision(i).equals(decision)) {
                classIndices[arrayIndex] = i;
                arrayIndex++;
            }
        }
        return classIndices;
    }
}
