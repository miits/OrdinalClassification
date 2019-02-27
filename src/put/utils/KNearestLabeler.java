package put.utils;

import put.types.LearningExampleType;

public class KNearestLabeler {
    private int safeLowerLimit;
    private int borderlineLowerLimit;
    private int rareLowerLimit;

    public KNearestLabeler(int safeLowerLimit, int borderlineLowerLimit, int rareLowerLimit) {
        this.safeLowerLimit = safeLowerLimit;
        this.borderlineLowerLimit = borderlineLowerLimit;
        this.rareLowerLimit = rareLowerLimit;
    }

    public LearningExampleType labelExample(int sameClassAmount) {
        if (sameClassAmount >= safeLowerLimit) return LearningExampleType.SAFE;
        if (sameClassAmount >= borderlineLowerLimit) return LearningExampleType.BORDERLINE;
        if (sameClassAmount >= rareLowerLimit) return LearningExampleType.RARE;
        return LearningExampleType.OUTLIER;
    }
}
