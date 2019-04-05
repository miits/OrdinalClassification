package org.ordinalclassification.types;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.rulelearn.data.InformationTableBuilder;
import org.rulelearn.data.InformationTableWithDecisionDistributions;
import org.rulelearn.measures.HVDM;

import java.io.IOException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class DistanceArrayTest {
    private static InformationTableWithDecisionDistributions data;
    private static DistanceArray distances;
    private static double normalization;

    @BeforeAll
    static void setUp() {
        String jsonPath = "data/test/json/distance-test.json";
        String csvPath = "data/test/csv/distance-test.csv";
        try {
            data = new InformationTableWithDecisionDistributions(InformationTableBuilder.safelyBuildFromCSVFile(jsonPath, csvPath, false));
        } catch (IOException e) {
            e.printStackTrace();
        }
        HVDM measure = new HVDM(data);
        distances = new DistanceArray(measure);
        normalization = 4 * 3.0277;
    }

    @Test
    void getDistance() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                double actualDistance = Math.abs(i - j) / normalization;
                assertEquals(distances.getDistance(i, j), actualDistance, 0.009);
            }
        }
    }

    @Test
    void getExampleDistances() {
        for (int i = 0; i < 10; i++) {
            HashMap<Integer, Double> exampleDistances = distances.getExampleDistances(i);
            assertEquals(exampleDistances.size(), 9);
            for (int j = 0; j < 10; j++) {
                if (i == j) {
                    continue;
                }
                double distance = exampleDistances.get(j);
                double actualDistance = Math.abs(i - j) / normalization;
                assertEquals(distance, actualDistance, 0.009);
            }
        }
    }
}