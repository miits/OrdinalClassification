package org.ordinalclassification.utils;

public class NumericalAttributeStats extends AttributeStats {
    private double mean;
    private double variance;
    private double stdDev;

    public NumericalAttributeStats(int attributeIndex, double[] dataSeries) {
        super(attributeIndex, dataSeries);
        calculateMean();
        calculateVariance();
        calculateStdDev();
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
        this.variance = temp / (dataSeries.length);
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
