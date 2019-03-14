package org.ordinalclassification.classifiers;

import org.rulelearn.measures.HVDM;
import org.ordinalclassification.utils.KernelLabeler;

public class KernelAnalyzer extends NearestNeighborsAnalyzer {
    private double kernel;
    private KernelLabeler labeler;

    public KernelAnalyzer(HVDM measure, int[] majorityIndices, int[] minorityIndices, double kernel, KernelLabeler labeler) {
        super(measure, majorityIndices, minorityIndices);
        this.kernel = kernel;
        this.labeler = labeler;
    }
}
