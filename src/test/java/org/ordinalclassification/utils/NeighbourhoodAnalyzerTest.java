package org.ordinalclassification.utils;

import org.junit.jupiter.api.Test;
import org.ordinalclassification.types.AnalysisResult;
import org.ordinalclassification.types.LearningExampleType;
import org.ordinalclassification.types.ResultRow;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NeighbourhoodAnalyzerTest {
    @Test
    void shouldReturnAllSafe() {
        String setName = "car-S100";
        String jsonPath = String.format("data\\test\\json\\%s.json", setName);
        String csvPath = String.format("data\\test\\csv\\%s.csv", setName);
        NeighbourhoodAnalyzer analyzer = new NeighbourhoodAnalyzer(jsonPath, csvPath, "");
        analyzer.runAnalysisSilent();
        HashMap<String, AnalysisResult> resultsByName = analyzer.getResultsByName();
        AnalysisResult results = resultsByName.get("union_vs_union_knn");
        List<ResultRow> perExampleResults = results.getPerExampleResults();
        int safeCounter = 0;
        int len = perExampleResults.size();
        for (ResultRow res: perExampleResults) {
            if (res.getType() == String.valueOf(LearningExampleType.SAFE)) {
                safeCounter++;
            }
        }
        double resultsPercent = (double) safeCounter / len * 100;
        double expectedPercent = 100;
        assertEquals(expectedPercent, resultsPercent);

        setName = "haberman-S100";
        jsonPath = String.format("data\\test\\json\\%s.json", setName);
        csvPath = String.format("data\\test\\csv\\%s.csv", setName);
        analyzer = new NeighbourhoodAnalyzer(jsonPath, csvPath, "");
        analyzer.runAnalysisSilent();
        resultsByName = analyzer.getResultsByName();
        results = resultsByName.get("union_vs_union_knn");
        perExampleResults = results.getPerExampleResults();
        safeCounter = 0;
        len = perExampleResults.size();
        for (ResultRow res: perExampleResults) {
            if (res.getType() == String.valueOf(LearningExampleType.SAFE)) {
                safeCounter++;
            }
        }
        resultsPercent = (double) safeCounter / len * 100;
        expectedPercent = 100;
        assertEquals(expectedPercent, resultsPercent);
    }

    @Test
    void shouldReturn75Safe25Borderline() {
        String setName = "car-B75-S25";
        String jsonPath = String.format("data\\test\\json\\%s.json", setName);
        String csvPath = String.format("data\\test\\csv\\%s.csv", setName);
        NeighbourhoodAnalyzer analyzer = new NeighbourhoodAnalyzer(jsonPath, csvPath, "");
        analyzer.runAnalysisSilent();
        HashMap<String, AnalysisResult> resultsByName = analyzer.getResultsByName();
        AnalysisResult results = resultsByName.get("union_vs_union_knn");
        List<ResultRow> perExampleResults = results.getPerExampleResults();
        int safeCounter = 0;
        int borderCounter = 0;
        double len = perExampleResults.size();
        for (ResultRow res: perExampleResults) {
            if (res.getType().equals(String.valueOf(LearningExampleType.SAFE))) {
                safeCounter++;
            } else if (res.getType().equals(String.valueOf(LearningExampleType.BORDERLINE))) {
                borderCounter++;
            }
        }
        double safePercent = (double) safeCounter / len * 100.0;
        double borderPercent = (double) borderCounter / len * 100.0;
        double expectedSafePercent = 75;
        double expectedBorderPercent = 25;
        assertEquals(expectedSafePercent, safePercent);
        assertEquals(expectedBorderPercent, borderPercent);
    }

    @Test
    void shouldReturnAllOutlier() {
        String setName = "car-O100";
        String jsonPath = String.format("data\\test\\json\\%s.json", setName);
        String csvPath = String.format("data\\test\\csv\\%s.csv", setName);
        NeighbourhoodAnalyzer analyzer = new NeighbourhoodAnalyzer(jsonPath, csvPath, "");
        analyzer.runAnalysisSilent();
        HashMap<String, AnalysisResult> resultsByName = analyzer.getResultsByName();
        AnalysisResult results = resultsByName.get("union_vs_union_knn");
        List<ResultRow> perExampleResults = results.getPerExampleResults();
        int outlierCounter = 0;
        int len = perExampleResults.size();
        for (ResultRow res: perExampleResults) {
            if (res.getType() == String.valueOf(LearningExampleType.OUTLIER)) {
                outlierCounter++;
            }
        }
        double resultsPercent = (double) outlierCounter / len * 100;
        double expectedPercent = 100;
        assertEquals(expectedPercent, resultsPercent);
    }

    @Test
    void shouldReturn50Borderline50Outlier() {
        String setName = "car-B50-O50";
        String jsonPath = String.format("data\\test\\json\\%s.json", setName);
        String csvPath = String.format("data\\test\\csv\\%s.csv", setName);
        NeighbourhoodAnalyzer analyzer = new NeighbourhoodAnalyzer(jsonPath, csvPath, "");
        analyzer.runAnalysisSilent();
        HashMap<String, AnalysisResult> resultsByName = analyzer.getResultsByName();
        AnalysisResult results = resultsByName.get("union_vs_union_knn");
        List<ResultRow> perExampleResults = results.getPerExampleResults();
        int outlierCounter = 0;
        int borderCounter = 0;
        double len = perExampleResults.size();
        for (ResultRow res: perExampleResults) {
            if (res.getType().equals(String.valueOf(LearningExampleType.OUTLIER))) {
                outlierCounter++;
            } else if (res.getType().equals(String.valueOf(LearningExampleType.BORDERLINE))) {
                borderCounter++;
            }
        }
        double outlierPercent = (double) outlierCounter / len * 100.0;
        double borderPercent = (double) borderCounter / len * 100.0;
        double expectedPercent = 50;
        assertEquals(expectedPercent, outlierPercent);
        assertEquals(expectedPercent, borderPercent);
    }

    @Test
    void shouldReturnEquallyDistributed() {
        String setName = "car-B25-O25-R25-S25";
        String jsonPath = String.format("data\\test\\json\\%s.json", setName);
        String csvPath = String.format("data\\test\\csv\\%s.csv", setName);
        NeighbourhoodAnalyzer analyzer = new NeighbourhoodAnalyzer(jsonPath, csvPath, "");
        analyzer.runAnalysisSilent();
        HashMap<String, AnalysisResult> resultsByName = analyzer.getResultsByName();
        AnalysisResult results = resultsByName.get("union_vs_union_knn");
        List<ResultRow> perExampleResults = results.getPerExampleResults();
        int safeCounter = 0;
        int borderCounter = 0;
        int rareCounter = 0;
        int outlierCounter = 0;
        double len = perExampleResults.size();
        for (ResultRow res: perExampleResults) {
            if (res.getType().equals(String.valueOf(LearningExampleType.SAFE))) {
                safeCounter++;
            } else if (res.getType().equals(String.valueOf(LearningExampleType.BORDERLINE))) {
                borderCounter++;
            } else if (res.getType().equals(String.valueOf(LearningExampleType.RARE))) {
                rareCounter++;
            } else if (res.getType().equals(String.valueOf(LearningExampleType.OUTLIER))) {
                outlierCounter++;
            }
        }
        double safePercent = (double) safeCounter / len * 100.0;
        double borderPercent = (double) borderCounter / len * 100.0;
        double rarePercent = (double) rareCounter / len * 100.0;
        double outlierPercent = (double) outlierCounter / len * 100.0;
        double expectedPercent = 25;
        assertEquals(expectedPercent, safePercent);
        assertEquals(expectedPercent, borderPercent);
        assertEquals(expectedPercent, rarePercent);
        assertEquals(expectedPercent, outlierPercent);

        setName = "haberman-B25-O25-R25-S25";
        jsonPath = String.format("data\\test\\json\\%s.json", setName);
        csvPath = String.format("data\\test\\csv\\%s.csv", setName);
        analyzer = new NeighbourhoodAnalyzer(jsonPath, csvPath, "");
        analyzer.runAnalysisSilent();
        resultsByName = analyzer.getResultsByName();
        results = resultsByName.get("union_vs_union_knn");
        perExampleResults = results.getPerExampleResults();
        safeCounter = 0;
        borderCounter = 0;
        rareCounter = 0;
        outlierCounter = 0;
        len = perExampleResults.size();
        for (ResultRow res: perExampleResults) {
            if (res.getType().equals(String.valueOf(LearningExampleType.SAFE))) {
                safeCounter++;
            } else if (res.getType().equals(String.valueOf(LearningExampleType.BORDERLINE))) {
                borderCounter++;
            } else if (res.getType().equals(String.valueOf(LearningExampleType.RARE))) {
                rareCounter++;
            } else if (res.getType().equals(String.valueOf(LearningExampleType.OUTLIER))) {
                outlierCounter++;
            }
        }
        safePercent = (double) safeCounter / len * 100.0;
        borderPercent = (double) borderCounter / len * 100.0;
        rarePercent = (double) rareCounter / len * 100.0;
        outlierPercent = (double) outlierCounter / len * 100.0;
        assertEquals(expectedPercent, safePercent);
        assertEquals(expectedPercent, borderPercent);
        assertEquals(expectedPercent, rarePercent);
        assertEquals(expectedPercent, outlierPercent);
    }
}