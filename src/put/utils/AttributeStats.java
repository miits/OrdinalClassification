package put.utils;

public abstract class AttributeStats {
    protected int attributeIndex;
    protected double[] dataSeries;

    public AttributeStats(int attributeIndex, double[] dataSeries) {
        this.attributeIndex = attributeIndex;
        this.dataSeries = dataSeries;
    }
}
