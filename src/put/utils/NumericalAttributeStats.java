package put.utils;

import org.rulelearn.data.Attribute;
import org.rulelearn.data.InformationTableWithDecisionDistributions;
import org.rulelearn.data.Table;
import org.rulelearn.types.EvaluationField;
import org.rulelearn.types.RealField;

public class NumericalAttributeStats extends AttributeStats {
    private double[] dataSeries;
    private double mean;
    private double variance;
    private double stdDev;

    public NumericalAttributeStats(Attribute attribute, InformationTableWithDecisionDistributions data) {
        super(attribute, data);
        extractDataSeries(data);
        calculateMean();
        calculateVariance();
        calculateStdDev();
    }

    private void extractDataSeries(InformationTableWithDecisionDistributions data) {
        Table<EvaluationField> fields = data.getActiveConditionAttributeFields();
        int datasetSize = fields.getNumberOfObjects();
        this.dataSeries = new double[datasetSize];
        for (int i = 0; i < datasetSize; i++) {
            dataSeries [i] = ((RealField) fields.getField(i, attributeIndex)).getValue();
        }
    }

    private void calculateMean() {
        double sum = 0;
        for (double x: dataSeries)
            sum += x;
        this.mean = sum / dataSeries.length;
    }

    private void calculateVariance() {
        double temp = 0;
        for (double x: dataSeries)
            temp += Math.pow(x - mean, 2);
        this.variance = temp / (dataSeries.length - 1);
    }

    private void calculateStdDev() {
        this.stdDev = Math.sqrt(variance);
    }

    public double getMean() {
        return mean;
    }

    public double getVariance() {
        return variance;
    }

    public double getStdDev() {
        return stdDev;
    }
}
