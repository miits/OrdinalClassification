package org.rulelearn.measures;

import org.rulelearn.data.*;
import org.rulelearn.types.*;
import org.ordinalclassification.types.FieldValueWrapper;
import org.ordinalclassification.utils.*;

import java.util.HashMap;

public class HVDM implements Measure {
    private InformationTableWithDecisionDistributions data;
    private int numberOfAttributes;
    private boolean[] attributeIsNominal;
    private int[] nominalAttributeValuesNumber;
    private double[][] values;
    private boolean[][] missingValues;
    private HashMap<Integer, AttributeStats> valueStatsByAttributeIndex;

    public HVDM(InformationTableWithDecisionDistributions data) {
        this.data = data;
        this.numberOfAttributes = data.getActiveConditionAttributeFields().getNumberOfAttributes();
        initNominalAttributesMarking();
        initValues();
        initStats();
    }

    private void initNominalAttributesMarking() {
        this.attributeIsNominal = new boolean[numberOfAttributes];
        this.nominalAttributeValuesNumber = new int[numberOfAttributes];
        for (int i = 0; i < numberOfAttributes; i++) {
            Attribute attribute = data.getAttribute(i);
            Field field = attribute.getValueType();
            if (field instanceof EnumerationField) {
                attributeIsNominal[i] = true;
                nominalAttributeValuesNumber[i] = ((EnumerationField) field).getElementList().getSize();
            } else {
                attributeIsNominal[i] = false;
            }
        }
    }

    private void initValues() {
        this.values = new double[data.getNumberOfObjects()][numberOfAttributes];
        this.missingValues = new boolean[data.getNumberOfObjects()][numberOfAttributes];
        for (int i = 0; i < data.getNumberOfObjects(); i++) {
            for (int j = 0; j < numberOfAttributes; j++) {
                FieldValueWrapper fieldValue = new FieldValueWrapper(data.getField(i, j));
                values[i][j] = fieldValue.getValue();
                missingValues[i][j] = fieldValue.isMissing();
            }
        }
    }

    private void initStats() {
        this.valueStatsByAttributeIndex = new HashMap<>();
        for (int i = 0; i < numberOfAttributes; i++) {
            double[] dataSeries = DataSubsetExtractor.get2dArrayColumn(values, i);
            if (attributeIsNominal[i]) {
                valueStatsByAttributeIndex.put(i, new EnumerationAttributeStats(i, dataSeries, nominalAttributeValuesNumber[i], data.getDecisions()));
            } else {
                valueStatsByAttributeIndex.put(i, new NumericalAttributeStats(i, dataSeries));
            }
        }
    }

    public double measureDistance(int xIndex, int yIndex) {
        double distance = 0;
        for (int i = 0; i < numberOfAttributes; i++) {
            distance += getDistance(i, xIndex, yIndex);
        }
        return distance;
    }

    private double getDistance(int attributeIndex, int xIndex, int yIndex) {
        if (missingValues[xIndex][attributeIndex] || missingValues[yIndex][attributeIndex]) {
            return 1;
        }
        if (attributeIsNominal[attributeIndex]) {
            return normalizedVDMForAttribute(attributeIndex, xIndex, yIndex);
        } else {
            return normalizedDiffForAttribute(attributeIndex, xIndex, yIndex);
        }
    }

    private double normalizedVDMForAttribute(int attributeIndex, int xIndex, int yIndex) {
        EnumerationAttributeStats xValueStats = (EnumerationAttributeStats) valueStatsByAttributeIndex.get(attributeIndex);
        EnumerationAttributeStats yValueStats = (EnumerationAttributeStats) valueStatsByAttributeIndex.get(attributeIndex);
        int xValueCount = xValueStats.getCount((int) values[xIndex][attributeIndex]);
        int yValueCount = yValueStats.getCount((int) values[yIndex][attributeIndex]);
        double normalizedVDM = 0;
        for (Decision decision: data.getDecisionDistribution().getDecisions()) {
            int xValueClassCount = xValueStats.getCountForDecision((int) values[xIndex][attributeIndex], decision);
            int yValueClassCount = yValueStats.getCountForDecision((int) values[yIndex][attributeIndex], decision);
            double pX = xValueCount == 0 ? 0 : (double) xValueClassCount / (double) xValueCount;
            double pY = yValueCount == 0 ? 0 : (double) yValueClassCount / (double) yValueCount;
            double diff = Math.abs(pX - pY);
            normalizedVDM += Math.pow(diff, 2);
        }
        normalizedVDM = Math.sqrt(normalizedVDM);
        return normalizedVDM;
    }

    private double normalizedDiffForAttribute(int attributeIndex, int xIndex, int yIndex) {
        NumericalAttributeStats attributeStats = (NumericalAttributeStats) valueStatsByAttributeIndex.get(attributeIndex);
        double diff = Math.abs(values[xIndex][attributeIndex] - values[yIndex][attributeIndex]);
        return diff / 4 * attributeStats.getStdDev();
    }

    public InformationTableWithDecisionDistributions getData() {
        return data;
    }

    @Override
    public MeasureType getType() {
        return MeasureType.COST;
    }
}
