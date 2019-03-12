package put.utils;

import org.rulelearn.data.*;

import java.util.*;

public class EnumerationAttributeStats extends AttributeStats{
    private int valuesNumber;
    private Decision[] decisions;
    private HashMap<Integer, DecisionDistribution> distributionByValueIndex;
    private int[] countByValueIndex;

    public EnumerationAttributeStats(int attributeIndex, double[] dataSeries, int valuesNumber, Decision[] decisions) {
        super(attributeIndex, dataSeries);
        this.valuesNumber = valuesNumber;
        this.decisions = decisions;
        initCounts();
    }

    private void initCounts() {
        this.countByValueIndex = new int[valuesNumber];
        this.distributionByValueIndex = new HashMap<>();
        for (int i = 0; i < valuesNumber; i++) {
            distributionByValueIndex.put(i, new DecisionDistribution());
        }
        count();
    }

    private void count() {
        for (int i = 0; i < dataSeries.length; i++) {
            int valueIndex = (int) dataSeries[i];
            countByValueIndex[valueIndex]++;
            distributionByValueIndex.get(valueIndex).increaseCount(decisions[i]);
        }
    }

    public int getCount(int valueId) {
        return countByValueIndex[valueId];
    }

    public int getCountForDecision(int valueId, Decision decision) {
        return distributionByValueIndex.get(valueId).getCount(decision);
    }
}
