package org.ordinalclassification.utils;

import org.ordinalclassification.types.LearningExampleType;

public class KernelLabeler{
    private double safeLowerLimit;
    private double borderlineLowerLimit;
    private double rareLowerLimit;

    public KernelLabeler(double safeLowerLimit, double borderlineLowerLimit, double rareLowerLimit) {
        this.safeLowerLimit = safeLowerLimit;
        this.borderlineLowerLimit = borderlineLowerLimit;
        this.rareLowerLimit = rareLowerLimit;
    }

    public LearningExampleType labelExample(double ratio) {
        if (ratio > safeLowerLimit) return LearningExampleType.SAFE;
        if (ratio > borderlineLowerLimit) return LearningExampleType.BORDERLINE;
        if (ratio > rareLowerLimit) return LearningExampleType.RARE;
        return LearningExampleType.OUTLIER;
    }
}
