package org.ordinalclassification.types;

public class ResultRow {
    private int index;
    private LearningExampleType type;
    private String minorityDecision;
    private String majorityDecision;

    public ResultRow(int index, LearningExampleType type, String minorityDecision, String majorityDecision) {
        this.index = index;
        this.type = type;
        this.minorityDecision = minorityDecision;
        this.majorityDecision = majorityDecision;
    }

    public String getIndex() {
        return String.valueOf(index);
    }

    public String getType() {
        return String.valueOf(type);
    }

    public String getMinorityDecision() {
        return minorityDecision;
    }

    public String getMajorityDecision() {
        return majorityDecision;
    }
}
