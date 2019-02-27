package put.utils;

import org.rulelearn.data.*;
import org.rulelearn.types.*;

import java.util.*;

public class EnumerationAttributeStats extends AttributeStats{
    private List<String> values;
    private Set<Decision> decisions;
    private HashMap<Integer, DecisionDistribution> distributionByValueIndex;
    private int[] countByValueIndex;

    public EnumerationAttributeStats(Attribute attribute, InformationTableWithDecisionDistributions data) {
        super(attribute, data);
        this.decisions = data.getDecisionDistribution().getDecisions();
        try {
            initValues();
        } catch (Exception e) {
            e.printStackTrace();
        }
        initCounts(data);
    }

    private void initValues() {
        if (isEnumerationValueType()) {
            this.values = getAttributeValues((EnumerationField) attribute.getValueType());
        } else {
            throw new IllegalArgumentException("Not an enumeration value type");
        }
    }

    private List<String> getAttributeValues(EnumerationField field) {
        ElementList elementList = field.getElementList();
        return Arrays.asList(elementList.getElements());
    }

    private void initCounts(InformationTableWithDecisionDistributions data) {
        countByValueIndex = new int[values.size()];
        distributionByValueIndex = new HashMap<>();
        for (int i = 0; i < values.size(); i++) {
            distributionByValueIndex.put(i, new DecisionDistribution());
        }
        count(data);
    }

    private void count(InformationTableWithDecisionDistributions data) {
        Table<EvaluationField> fields = data.getActiveConditionAttributeFields();
        Decision[] objectDecisions = data.getDecisions();
        int valueIndex;
        int datasetSize = fields.getNumberOfObjects();
        for (int i = 0; i < datasetSize; i++) {
            EvaluationField field = fields.getField(i, attributeIndex);
            valueIndex = values.indexOf(field.toString());
            countByValueIndex[valueIndex]++;
            distributionByValueIndex.get(valueIndex).increaseCount(objectDecisions[i]);
        }
    }

    public int getCount(int valueId) {
        return countByValueIndex[valueId];
    }

    public int getCountForDecision(int valueId, Decision decision) {
        return distributionByValueIndex.get(valueId).getCount(decision);
    }
}
