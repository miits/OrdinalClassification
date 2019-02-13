package put.main;

import org.rulelearn.approximations.ClassicalDominanceBasedRoughSetCalculator;
import org.rulelearn.approximations.Union;
import org.rulelearn.data.*;
import org.rulelearn.types.ElementList;
import org.rulelearn.types.EnumerationField;
import org.rulelearn.types.EnumerationFieldFactory;
import org.rulelearn.types.EvaluationField;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        String jsonPath = ".\\data\\json\\balance_scale.json";
        String csvPath = ".\\data\\csv\\balance_scale.csv";
        try {
            InformationTableWithDecisionDistributions informationTable = new InformationTableWithDecisionDistributions(InformationTableBuilder.safelyBuildFromCSVFile(jsonPath, csvPath, false));

            Attribute[] attributes = informationTable.getAttributes();
            int decisionAttributeIndex = attributes.length - 1;
            EvaluationAttribute decisionAttribute = (EvaluationAttribute) attributes[decisionAttributeIndex];
            EnumerationField valueType = (EnumerationField) decisionAttribute.getValueType();
            ElementList elementList = valueType.getElementList();

            DecisionDistribution decisionDistribution = informationTable.getDecisionDistribution();
            Decision[] decisions = informationTable.getDecisions();

            for (int i = 0; i < elementList.getSize() - 1; i++) {
                //type 1
                EvaluationField lowerBoundry = EnumerationFieldFactory.getInstance().create(elementList, i, valueType.getPreferenceType());
                Decision lowerDecision = new SimpleDecision(lowerBoundry, decisionAttributeIndex);
                Union atMostUnion = new Union(Union.UnionType.AT_MOST, lowerDecision, informationTable, new ClassicalDominanceBasedRoughSetCalculator());

                EvaluationField upperBoundry = EnumerationFieldFactory.getInstance().create(elementList, i + 1, valueType.getPreferenceType());
                Decision upperDecision = new SimpleDecision(upperBoundry, decisionAttributeIndex);
                Union atLeastUnion = new Union(Union.UnionType.AT_LEAST, upperDecision, informationTable, new ClassicalDominanceBasedRoughSetCalculator());

                //type 2
                SimpleDecision classDecision = new SimpleDecision(lowerBoundry, decisionAttributeIndex);
                int classSize = decisionDistribution.getCount(classDecision);
                int[] classIndices = new int[classSize];
                int arrayIndex = 0;
                for (int j = 0; j < decisions.length; j++) {
                    if (decisions[j].equals(classDecision)) {
                        classIndices[arrayIndex] = j;
                        arrayIndex++;
                    }
                }
                InformationTable classObjects = informationTable.select(classIndices,true);
                System.out.println();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
