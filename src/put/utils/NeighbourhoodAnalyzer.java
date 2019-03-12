package put.utils;

import it.unimi.dsi.fastutil.ints.IntSortedSet;
import org.rulelearn.approximations.Union;
import org.rulelearn.data.*;
import put.classifiers.KNNAnalyzer;
import put.measures.HVDM;
import put.types.LearningExampleType;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class NeighbourhoodAnalyzer implements DatasetOperation {
    private String jsonPath;
    private String csvPath;
    private String resultsPath;
    private DataSubsetExtractor dataExtractor;
    private HVDM measure;

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
        while (atLeastUnionIterator.hasNext() && atMostUnionIterator.hasNext()) {
            IntSortedSet atLeastObjects = atLeastUnionIterator.next().getObjects();
            IntSortedSet atMostObjects = atMostUnionIterator.next().getObjects();
            int[] atLeast = new int[atLeastObjects.size()];
            int[] atMost = new int[atMostObjects.size()];
            atLeastObjects.toArray(atLeast);
            atMostObjects.toArray(atMost);
            if (atLeast.length > atMost.length) {
                kNNAndKernelAnalysis(atLeast, atMost);
            } else {
                kNNAndKernelAnalysis(atMost, atLeast);
            }
        }
    }

    private void classVsUnionAnalysis(HashMap<Decision, InformationTable> classesByDecision, Union[] atLeastUnions, Union[] atMostUnions) {
//        kNearestAnalysis();
//        kernelAnalysis();
    }

    private void kNNAndKernelAnalysis(int[] majorityInidices, int[] minorityIndices) {
        kNearestAnalysis(majorityInidices, minorityIndices);
        kernelAnalysis(majorityInidices, minorityIndices);
    }

    private void kNearestAnalysis(int[] majorityIndices, int[] minorityIndices) {
        KNearestLabeler labeler = new KNearestLabeler(4, 2, 1);
        KNNAnalyzer analyzer = new KNNAnalyzer(measure, majorityIndices, minorityIndices, 5, labeler);
        analyzer.labelExamples();
        HashMap<Integer, LearningExampleType> labelsAssignment = analyzer.getLabelsAssignment();
    }

    private void kernelAnalysis(int[] majorityindices, int[] minorityIndices) {

    }

    private void saveResults() {

    }
}
