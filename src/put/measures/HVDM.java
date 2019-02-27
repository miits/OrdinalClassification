package put.measures;

import org.rulelearn.data.*;
import org.rulelearn.measures.Measure;
import org.rulelearn.measures.MeasureType;
import org.rulelearn.types.EnumerationField;
import org.rulelearn.types.EvaluationField;
import org.rulelearn.types.Field;
import org.rulelearn.types.RealField;
import put.utils.AttributeStats;
import put.utils.EnumerationAttributeStats;
import put.utils.InformationTableUtils;
import put.utils.NumericalAttributeStats;

import java.util.HashMap;

public class HVDM implements Measure {
    private InformationTableWithDecisionDistributions data;
    private Table<EvaluationField> fields;
    private int numberOfAttributes;
    private boolean[] attributeIsNominal;
    private HashMap<Integer, AttributeStats> valueStatsByAttributeIndex;

    public HVDM(InformationTableWithDecisionDistributions data) {
        this.data = data;
        this.fields = data.getActiveConditionAttributeFields();
        this.numberOfAttributes = fields.getNumberOfAttributes();
        this.attributeIsNominal = new boolean[numberOfAttributes];
        this.valueStatsByAttributeIndex = new HashMap<>();
        initStats();
    }

    private void initStats() {
        for (int i = 0; i < numberOfAttributes; i++) {
            Attribute attribute = data.getAttribute(i);
            if (EnumerationField.class.isAssignableFrom(attribute.getValueType().getClass())) {
                valueStatsByAttributeIndex.put(i, new EnumerationAttributeStats(attribute, data));
                attributeIsNominal[i] = true;
            } else {
                valueStatsByAttributeIndex.put(i, new NumericalAttributeStats(attribute, data));
                attributeIsNominal[i] = false;
            }
        }
    }

    public double measureDistance(InformationTable x, InformationTable y) {
        InformationTableUtils.checkTablesLength(1, new InformationTable[]{x, y});
        double distance = 0;
        for (int i = 0; i < numberOfAttributes; i++) {
            distance += getDistance(i, x.getField(0, i), y.getField(0, i));
        }
        return distance;
    }

    private double getDistance(int attributeIndex, Field x, Field y) {
        if (attributeIsNominal[attributeIndex]) {
            return normalizedVDMForAttribute(attributeIndex, (EnumerationField) x, (EnumerationField) y);
        } else {
            return normalizedDiffForAttribute(attributeIndex, (RealField) x, (RealField) y);
        }
    }

    private double normalizedVDMForAttribute(int attributeIndex, EnumerationField x, EnumerationField y) {
        EnumerationAttributeStats xValueStats = (EnumerationAttributeStats) valueStatsByAttributeIndex.get(attributeIndex);
        int xValueCount = xValueStats.getCount(x.getValue());
        EnumerationAttributeStats yValueStats = (EnumerationAttributeStats) valueStatsByAttributeIndex.get(attributeIndex);
        int yValueCount = yValueStats.getCount(x.getValue());
        double normalizedVDM = 0;
        for (Decision decision: data.getDecisionDistribution().getDecisions()) {
            int yValueClassCount = yValueStats.getCountForDecision(x.getValue(), decision);
            int xValueClassCount = xValueStats.getCountForDecision(x.getValue(), decision);
            double diff = Math.abs(xValueClassCount / xValueCount - yValueClassCount / yValueCount);
            normalizedVDM += Math.pow(diff, 2);
        }
        normalizedVDM = Math.sqrt(normalizedVDM);
        return normalizedVDM;
    }

    private double normalizedDiffForAttribute(int attributeIndex, RealField x, RealField y) {
        NumericalAttributeStats attributeStats = (NumericalAttributeStats) valueStatsByAttributeIndex.get(attributeIndex);
        double diff = Math.abs(x.getValue() - y.getValue());
        return diff / 4 * attributeStats.getStdDev();
    }

    @Override
    public MeasureType getType() {
        return MeasureType.COST;
    }
}
