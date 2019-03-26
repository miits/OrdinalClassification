package org.ordinalclassification.utils;

import it.unimi.dsi.fastutil.ints.IntSortedSet;
import org.ordinalclassification.classifiers.KernelAnalyzer;
import org.ordinalclassification.classifiers.NearestNeighborsAnalyzer;
import org.rulelearn.approximations.Union;
import org.rulelearn.approximations.UnionWithSingleLimitingDecision;
import org.rulelearn.data.*;
import org.ordinalclassification.classifiers.KNNAnalyzer;
import org.rulelearn.measures.HVDM;
import org.ordinalclassification.types.AnalysisResult;
import org.ordinalclassification.types.LearningExampleType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class NeighbourhoodAnalyzer implements DatasetOperation {
    private String jsonPath;
    private String csvPath;
    private String resultsPath;
    private DataSubsetExtractor dataExtractor;
    private HVDM measure;
    private HashMap<String, AnalysisResult> resultsByName;
    private static String unionVsUnionKernelFilename = "union_vs_union_kernel";
    private static String unionVsUnionKNNFilename = "union_vs_union_knn";
    private static String classVsUnionKernelFilename = "class_vs_union_kernel";
    private static String classVsUnionKNNFilename = "class_vs_union_knn";

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

    public void runAnalysisSilent() {
        try {
            loadData();
            analyze();
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
        HashMap<Decision, int[]> classesByDecision = dataExtractor.getClassesByDecision();
        classVsUnionAnalysis(classesByDecision, atLeastUnions, atMostUnions);
    }

    private void unionVsUnionAnalysis(Union[] atLeastUnions, Union[] atMostUnions) {
        Collections.reverse(Arrays.asList(atLeastUnions));
        Iterator<Union> atLeastUnionIterator = Arrays.stream(atLeastUnions).iterator();
        Iterator<Union> atMostUnionIterator = Arrays.stream(atMostUnions).iterator();
        resultsByName.put(unionVsUnionKNNFilename, new AnalysisResult());
        resultsByName.put(unionVsUnionKernelFilename, new AnalysisResult());
        while (atLeastUnionIterator.hasNext() && atMostUnionIterator.hasNext()) {
            UnionWithSingleLimitingDecision atLeastUnion = (UnionWithSingleLimitingDecision) atLeastUnionIterator.next();
            UnionWithSingleLimitingDecision atMostUnion = (UnionWithSingleLimitingDecision) atMostUnionIterator.next();
            int[] atLeast = unionToArray(atLeastUnion);
            int[] atMost = unionToArray(atMostUnion);
            if (atLeast.length > atMost.length) {
                performKNearestAnalysis(atLeast, atMost, atLeastUnion.getLimitingDecision(), atMostUnion.getLimitingDecision(), unionVsUnionKNNFilename);
                performKernelAnalysis(atLeast, atMost, atLeastUnion.getLimitingDecision(), atMostUnion.getLimitingDecision(), unionVsUnionKernelFilename);
            } else {
                performKNearestAnalysis(atMost, atLeast, atMostUnion.getLimitingDecision(), atLeastUnion.getLimitingDecision(), unionVsUnionKNNFilename);
                performKernelAnalysis(atMost, atLeast, atMostUnion.getLimitingDecision(), atLeastUnion.getLimitingDecision(), unionVsUnionKernelFilename);
            }
        }
    }

    private int[] unionToArray(UnionWithSingleLimitingDecision union) {
        IntSortedSet objects = union.getObjects();
        int arr[] = new int[objects.size()];
        objects.toArray(arr);
        return arr;
    }

    private void performKNearestAnalysis(int[] majority, int[] minority, Decision majorityLimitingDecision, Decision minorityLimitingDecision, String resultsKey) {
        HashMap<Integer, LearningExampleType> kNearestResults = kNearestAnalysis(majority, minority);
        resultsByName.get(resultsKey).addResults(kNearestResults, minorityLimitingDecision, majorityLimitingDecision);
    }

    private void performKernelAnalysis(int[] majority, int[] minority, Decision majorityLimitingDecision, Decision minorityLimitingDecision, String resultsKey) {
        HashMap<Integer, LearningExampleType> kernelResults = kernelAnalysis(majority, minority, majorityLimitingDecision, minorityLimitingDecision);
        resultsByName.get(resultsKey).addResults(kernelResults, minorityLimitingDecision, majorityLimitingDecision);
    }

    private HashMap<Integer, LearningExampleType> kNearestAnalysis(int[] majorityIndices, int[] minorityIndices) {
        KNearestLabeler labeler = new KNearestLabeler(4, 2, 1);
        KNNAnalyzer analyzer = new KNNAnalyzer(measure, majorityIndices, minorityIndices, 5, labeler);
        analyzer.labelExamples();
        return analyzer.getLabelsAssignment();
    }

    private HashMap<Integer, LearningExampleType> kernelAnalysis(int[] majorityIndices, int[] minorityIndices, Decision majorityLimitingDecision, Decision minorityLimitingDecision) {
        KernelLabeler labeler = new KernelLabeler(0.7, 0.3, 0.1);
        KernelAnalyzer analyzer = new KernelAnalyzer(measure, majorityIndices, minorityIndices, majorityLimitingDecision, minorityLimitingDecision, labeler);
        analyzer.labelExamples();
        return analyzer.getLabelsAssignment();
    }

    private void classVsUnionAnalysis(HashMap<Decision, int[]> classesByDecision, Union[] atLeastUnions, Union[] atMostUnions) {
        Collections.reverse(Arrays.asList(atLeastUnions));
        Iterator<Union> atLeastUnionIterator = Arrays.stream(atLeastUnions).iterator();
        Iterator<Union> atMostUnionIterator = Arrays.stream(atMostUnions).iterator();
        resultsByName.put(classVsUnionKNNFilename, new AnalysisResult());
        resultsByName.put(classVsUnionKernelFilename, new AnalysisResult());
        UnionWithSingleLimitingDecision atMostUnion = (UnionWithSingleLimitingDecision) atMostUnionIterator.next();
        Decision classDecision = atMostUnion.getLimitingDecision();
        while (atLeastUnionIterator.hasNext()) {
            UnionWithSingleLimitingDecision atLeastUnion = (UnionWithSingleLimitingDecision) atLeastUnionIterator.next();
            int[] atLeast = unionToArray(atLeastUnion);
            int[] classObjects = classesByDecision.get(classDecision);
            performKNearestAnalysis(atLeast, classObjects, atLeastUnion.getLimitingDecision(), classDecision, classVsUnionKNNFilename);
            performKernelAnalysis(atLeast, classObjects, atLeastUnion.getLimitingDecision(), classDecision, classVsUnionKernelFilename);
            if (classesByDecision.entrySet().size() > 2) {
                classDecision = atLeastUnion.getLimitingDecision();
                classObjects = classesByDecision.get(classDecision);
                int[] atMost = unionToArray(atMostUnion);
                performKNearestAnalysis(atMost, classObjects, atMostUnion.getLimitingDecision(), classDecision, classVsUnionKNNFilename);
                performKernelAnalysis(atMost, classObjects, atMostUnion.getLimitingDecision(), classDecision, classVsUnionKernelFilename);
            }
            if (atMostUnionIterator.hasNext()) {
                atMostUnion = (UnionWithSingleLimitingDecision) atMostUnionIterator.next();
            }
        }
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

    public HashMap<String, AnalysisResult> getResultsByName() {
        return resultsByName;
    }
}
