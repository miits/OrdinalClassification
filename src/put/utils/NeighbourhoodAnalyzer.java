package put.utils;

import it.unimi.dsi.fastutil.ints.IntSortedSet;
import org.rulelearn.approximations.Union;
import org.rulelearn.approximations.UnionWithSingleLimitingDecision;
import org.rulelearn.data.*;
import put.classifiers.KNNAnalyzer;
import put.measures.HVDM;
import put.types.AnalysisResult;
import put.types.LearningExampleType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class NeighbourhoodAnalyzer implements DatasetOperation {
    private String jsonPath;
    private String csvPath;
    private String resultsPath;
    private DataSubsetExtractor dataExtractor;
    private HVDM measure;
    private HashMap<String, AnalysisResult> resultsByName;

    public NeighbourhoodAnalyzer(String jsonPath, String csvPath, String resultsPath) {
        this.jsonPath = jsonPath;
        this.csvPath = csvPath;
        this.resultsPath = resultsPath;
    }

    public NeighbourhoodAnalyzer() {
    }

    @Override
    public void carryOut(String[] args) {
        loadArgs(args);
        runAnalysis();
    }

    private void loadArgs(String[] args) {
        try {
            checkArgs(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        jsonPath = args[0];
        csvPath = args[1];
        resultsPath = args[2];
    }

    private void checkArgs(String[] args) throws IllegalArgumentException{
        if (args.length < 3) {
            throw new IllegalArgumentException("Argument missing");
        }
    }

    public void runAnalysis() {
        try {
            loadData();
            analyze();
            saveResults();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadData() throws IOException {
        InformationTableWithDecisionDistributions informationTable = new InformationTableWithDecisionDistributions(
                InformationTableBuilder.safelyBuildFromCSVFile(jsonPath, csvPath, false));
        dataExtractor = new DataSubsetExtractor(informationTable);
        measure = new HVDM(dataExtractor.getData());
        resultsByName = new HashMap<>();
    }

    private void analyze() {
        Union[] atLeastUnions = dataExtractor.getAtLeastUnions();
        Union[] atMostUnions = dataExtractor.getAtMostUnions();
        unionVsUnionAnalysis(atLeastUnions, atMostUnions);
        HashMap<Decision, InformationTable> classesByDecision = dataExtractor.getClassesByDecision();
        classVsUnionAnalysis(classesByDecision, atLeastUnions, atMostUnions);
    }

    private void unionVsUnionAnalysis(Union[] atLeastUnions, Union[] atMostUnions) {
        Iterator<Union> atLeastUnionIterator = Arrays.stream(atLeastUnions).iterator();
        Iterator<Union> atMostUnionIterator = Arrays.stream(atMostUnions).iterator();
        resultsByName.put("uvu_knn", new AnalysisResult());
        resultsByName.put("uvu_kernel", new AnalysisResult());
        while (atLeastUnionIterator.hasNext() && atMostUnionIterator.hasNext()) {
            UnionWithSingleLimitingDecision atLeastUnion = (UnionWithSingleLimitingDecision) atLeastUnionIterator.next();
            UnionWithSingleLimitingDecision atMostUnion = (UnionWithSingleLimitingDecision) atMostUnionIterator.next();
            IntSortedSet atLeastObjects = atLeastUnion.getObjects();
            IntSortedSet atMostObjects = atMostUnion.getObjects();
            int[] atLeast = new int[atLeastObjects.size()];
            int[] atMost = new int[atMostObjects.size()];
            atLeastObjects.toArray(atLeast);
            atMostObjects.toArray(atMost);
            if (atLeast.length > atMost.length) {
                HashMap<Integer, LearningExampleType> kNearestResults = kNearestAnalysis(atLeast, atMost);
                HashMap<Integer, LearningExampleType> kernelResults = kernelAnalysis(atLeast, atMost);
                resultsByName.get("uvu_knn").addResults(kNearestResults, atMostUnion.getLimitingDecision(), atLeastUnion.getLimitingDecision());
                resultsByName.get("uvu_knn").addResults(kernelResults, atMostUnion.getLimitingDecision(), atLeastUnion.getLimitingDecision());
            } else {
                kNearestAnalysis(atMost, atLeast);
                HashMap<Integer, LearningExampleType> kNearestResults = kNearestAnalysis(atMost, atLeast);
                HashMap<Integer, LearningExampleType> kernelResults = kernelAnalysis(atMost, atLeast);
                resultsByName.get("uvu_knn").addResults(kNearestResults, atLeastUnion.getLimitingDecision(), atMostUnion.getLimitingDecision());
                resultsByName.get("uvu_knn").addResults(kernelResults, atLeastUnion.getLimitingDecision(), atMostUnion.getLimitingDecision());
            }
        }
    }

    private void classVsUnionAnalysis(HashMap<Decision, InformationTable> classesByDecision, Union[] atLeastUnions, Union[] atMostUnions) {
//        kNearestAnalysis();
//        kernelAnalysis();
    }

    private HashMap<Integer, LearningExampleType> kNearestAnalysis(int[] majorityIndices, int[] minorityIndices) {
        KNearestLabeler labeler = new KNearestLabeler(4, 2, 1);
        KNNAnalyzer analyzer = new KNNAnalyzer(measure, majorityIndices, minorityIndices, 5, labeler);
        analyzer.labelExamples();
        return analyzer.getLabelsAssignment();
    }

    private HashMap<Integer, LearningExampleType> kernelAnalysis(int[] majorityindices, int[] minorityIndices) {
        return new HashMap<>();
    }

    private void saveResults() throws IOException {
        for (Map.Entry<String, AnalysisResult> entry: resultsByName.entrySet()) {
            AnalysisResult result = entry.getValue();
            String filename = String.format("%s\\%s.csv", resultsPath, entry.getKey());
            createDirIfNotExists();
            result.saveCsv(filename);
        }
    }

    private void createDirIfNotExists() {
        if (Files.notExists(Paths.get(resultsPath))) {
            new File(resultsPath).mkdirs();
        }
    }
}
