package org.ordinalclassification.types;

import org.rulelearn.types.EnumerationField;
import org.rulelearn.types.Field;
import org.rulelearn.types.IntegerField;
import org.rulelearn.types.RealField;

public class FieldValueWrapper {
    private double value;
    private boolean isMissing;

    public FieldValueWrapper(Field field) {
        value = 0;
        isMissing = false;
        if (field instanceof EnumerationField) {
            EnumerationField enumField = (EnumerationField) field;
            value = enumField.getValue();
        } else if (field instanceof RealField) {
            RealField realField = (RealField) field;
            value = realField.getValue();
        } else if (field instanceof IntegerField) {
            IntegerField intField = (IntegerField) field;
            value = intField.getValue();
        } else {
            isMissing = true;
        }
    }

    public double getValue() {
        return value;
    }

    public boolean isMissing() {
        return isMissing;
    }
}
