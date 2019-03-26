package org.ordinalclassification.utils;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.rulelearn.data.*;
import org.rulelearn.types.Field;
import org.rulelearn.types.RealFieldFactory;
import org.rulelearn.types.UnknownSimpleFieldMV2;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.rulelearn.data.AttributePreferenceType.GAIN;

class NumericalAttributeStatsTest {
    private static Attribute[] attributes = new Attribute[3];
    private static double[][] valuesArray;
    private static List<Field[]> fields = new ArrayList<>();
    private static InformationTableWithDecisionDistributions data;
    private static int testDataLength;

    @BeforeAll
    static void init() {
        testDataLength = 5;
        valuesArray = new double[testDataLength][3];
        RealFieldFactory factory = RealFieldFactory.getInstance();
        attributes[0] = new EvaluationAttribute("attr0", true, AttributeType.CONDITION, factory.create(0, GAIN), new UnknownSimpleFieldMV2(), GAIN);
        attributes[1] = new EvaluationAttribute("attr1", true, AttributeType.CONDITION, factory.create(0, GAIN), new UnknownSimpleFieldMV2(), GAIN);
        attributes[2] = new EvaluationAttribute("attr2", true, AttributeType.DECISION, factory.create(0, GAIN), new UnknownSimpleFieldMV2(), GAIN);
        Field[] fieldsArray = new Field[3];
        for (int i = 0; i < testDataLength; i++) {
            fieldsArray[0] = factory.create(i, GAIN);
            fieldsArray[1] = factory.create(i * 2, GAIN);
            fieldsArray[2] = factory.create(0, GAIN);
            fields.add(fieldsArray.clone());
            valuesArray[i][0] = i;
            valuesArray[i][1] = i * 2;
            valuesArray[i][2] = 0;
        }
        data = new InformationTableWithDecisionDistributions(attributes, fields);
    }

    @Test
    void shouldCalculateCorrectValues() {
        double correctMean = 2;
        double correctVariance = 2.5;
        double correctStdDev = 1.5811;
        NumericalAttributeStats stats = new NumericalAttributeStats(0, DataSubsetExtractor.get2dArrayColumn(valuesArray, 0));
        assertEquals(correctMean, stats.getMean());
        assertEquals(correctVariance, stats.getVariance());
        assertEquals(correctStdDev, stats.getStdDev(), 0.00009);
    }
}