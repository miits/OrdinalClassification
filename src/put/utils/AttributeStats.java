package put.utils;

import org.rulelearn.data.Attribute;
import org.rulelearn.data.InformationTableWithDecisionDistributions;
import org.rulelearn.types.EnumerationField;
import org.rulelearn.types.Field;

import java.util.Arrays;

public abstract class AttributeStats {
    protected Attribute attribute;
    protected int attributeIndex;

    public AttributeStats(Attribute attribute, InformationTableWithDecisionDistributions data) {
        this.attribute = attribute;
        this.attributeIndex = Arrays.asList(data.getAttributes()).indexOf(attribute);
    }

    protected boolean isEnumerationValueType() {
        Class<? extends Field> valueClass = attribute.getValueType().getClass();
        return EnumerationField.class.isAssignableFrom(valueClass);
    }
}
