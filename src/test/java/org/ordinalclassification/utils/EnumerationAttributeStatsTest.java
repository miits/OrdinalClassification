package org.ordinalclassification.utils;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.rulelearn.data.*;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class EnumerationAttributeStatsTest {
    private static double[][] valuesArray;
    private static InformationTableWithDecisionDistributions data;
    private static Decision[] decisions;
    private static String jsonPath = "data/test/json/car.json";
    private static String csvPath = "data/test/csv/car.csv";
    private static EnumerationAttributeStats firstStats;
    private static EnumerationAttributeStats secondStats;

    @BeforeAll
    static void init() {
        valuesArray = new double[13][3];
        valuesArray[0][0] = 0;
        valuesArray[0][1] = 0;
        valuesArray[0][2] = 0;
        valuesArray[1][0] = 0;
        valuesArray[1][1] = 1;
        valuesArray[1][2] = 1;
        valuesArray[2][0] = 0;
        valuesArray[2][1] = 2;
        valuesArray[2][2] = 0;
        valuesArray[3][0] = 1;
        valuesArray[3][1] = 0;
        valuesArray[3][2] = 1;
        valuesArray[4][0] = 1;
        valuesArray[4][1] = 1;
        valuesArray[4][2] = 0;
        valuesArray[5][0] = 1;
        valuesArray[5][1] = 2;
        valuesArray[5][2] = 1;
        valuesArray[6][0] = 0;
        valuesArray[6][1] = 0;
        valuesArray[6][2] = 0;
        valuesArray[7][0] = 0;
        valuesArray[7][1] = 1;
        valuesArray[7][2] = 1;
        valuesArray[8][0] = 0;
        valuesArray[8][1] = 2;
        valuesArray[8][2] = 0;
        valuesArray[9][0] = 1;
        valuesArray[9][1] = 0;
        valuesArray[9][2] = 1;
        valuesArray[10][0] = 1;
        valuesArray[10][1] = 1;
        valuesArray[10][2] = 0;
        valuesArray[11][0] = 1;
        valuesArray[11][1] = 2;
        valuesArray[11][2] = 1;
        valuesArray[12][0] = 1;
        valuesArray[12][1] = 0;
        valuesArray[12][2] = 1;
        try {
            data = new InformationTableWithDecisionDistributions(InformationTableBuilder.safelyBuildFromCSVFile(jsonPath, csvPath, false));
        } catch (IOException e) {
            e.printStackTrace();
        }
        decisions = data.getDecisions();
        firstStats = new EnumerationAttributeStats(0, DataSubsetExtractor.get2dArrayColumn(valuesArray, 0), 2, decisions);
        secondStats = new EnumerationAttributeStats(1, DataSubsetExtractor.get2dArrayColumn(valuesArray, 1), 3, decisions);
    }

    @Test
    void shouldReturnCorrectCounts() {
        assertEquals(6, firstStats.getCount(0));
        assertEquals(7, firstStats.getCount(1));
        assertEquals(5, secondStats.getCount(0));
        assertEquals(4, secondStats.getCount(1));
        assertEquals(4, secondStats.getCount(2));
    }

    @Test
    void shouldReturnCorrectCountsForDecisions() {
        Decision unaccDecision;
        Decision accDecision;
        if (decisions[0].toString().contains("unacc")) {
            unaccDecision = decisions[0];
            accDecision = decisions[1];
        } else {
            unaccDecision = decisions[1];
            accDecision = decisions[0];
        }
        assertEquals(4, firstStats.getCountForDecision(0, unaccDecision));
        assertEquals(2, firstStats.getCountForDecision(0, accDecision));
        assertEquals(2, firstStats.getCountForDecision(1, unaccDecision));
        assertEquals(5, firstStats.getCountForDecision(1, accDecision));
        assertEquals(2, secondStats.getCountForDecision(0, unaccDecision));
        assertEquals(3, secondStats.getCountForDecision(0, accDecision));
        assertEquals(2, secondStats.getCountForDecision(1, unaccDecision));
        assertEquals(2, secondStats.getCountForDecision(1, accDecision));
        assertEquals(2, secondStats.getCountForDecision(2, unaccDecision));
        assertEquals(2, secondStats.getCountForDecision(2, accDecision));
    }
}