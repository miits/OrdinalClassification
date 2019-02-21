package put.utils;

import org.rulelearn.approximations.ClassicalDominanceBasedRoughSetCalculator;
import org.rulelearn.approximations.Union;
import org.rulelearn.data.Decision;
import org.rulelearn.data.DecisionDistribution;
import org.rulelearn.data.InformationTable;
import org.rulelearn.data.InformationTableWithDecisionDistributions;

import java.util.HashMap;
import java.util.Set;

public class DataSubsetExtractor {
    private InformationTableWithDecisionDistributions data;
    private DecisionDistribution decisionDistribution;
    private Set<Decision> decisions;
    private int datasetSize;
    private Union[] atLeastUnions;
    private Union[] atMostUnions;
    private HashMap<Decision, InformationTable> classesByDecision;

    public DataSubsetExtractor(InformationTableWithDecisionDistributions data) {
        this.data = data;
        this.decisionDistribution = this.data.getDecisionDistribution();
        this.decisions = this.decisionDistribution.getDecisions();
        this.datasetSize = this.data.getDecisions().length;
        this.atLeastUnions = buildUnionsOfType(Union.UnionType.AT_LEAST);
        this.atMostUnions = buildUnionsOfType(Union.UnionType.AT_MOST);
        this.classesByDecision = buildClassesByDecision();
    }

    public Union[] getAtLeastUnions() {
        return atLeastUnions;
    }

    public Union[] getAtMostUnions() {
        return atMostUnions;
    }

    public HashMap<Decision, InformationTable> getClassesByDecision() {
        return classesByDecision;
    }

    private Union[] buildUnionsOfType(Union.UnionType type) {
        Union[] unions = new Union[this.decisions.size()];
        int it = 0;
        for (Decision decision: this.decisions) {
            unions[it] = new Union(type, decision, this.data, new ClassicalDominanceBasedRoughSetCalculator());
            it++;
        }
        return unions;
    }

    private HashMap<Decision, InformationTable> buildClassesByDecision() {
        HashMap<Decision, InformationTable> classesByDecision = new HashMap<>();
        for (Decision decision: this.decisions) {
            int[] classIndices = selectIndicesWithDecision(decision);
            InformationTable classSamples = this.data.select(classIndices,true);
            classesByDecision.put(decision, classSamples);
        }
        return classesByDecision;
    }

    private int[] selectIndicesWithDecision(Decision decision) {
        int classSize = this.decisionDistribution.getCount(decision);
        int[] classIndices = new int[classSize];
        int arrayIndex = 0;
        for (int i = 0; i < this.datasetSize; i++) {
            if (this.data.getDecision(i).equals(decision)) {
                classIndices[arrayIndex] = i;
                arrayIndex++;
            }
        }
        return classIndices;
    }
}
