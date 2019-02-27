package put.classifiers;

import org.rulelearn.data.InformationTable;
import put.measures.HVDM;
import put.utils.KernelLabeler;

public class KernelAnalyzer extends NearestNeighborsAnalyzer {
    private double kernel;
    private KernelLabeler labeler;

    public KernelAnalyzer(HVDM measure, InformationTable data, InformationTable examples, double kernel, KernelLabeler labeler) {
        super(measure, data, examples);
        this.kernel = kernel;
        this.labeler = labeler;
    }
}
