package put.utils;

import org.rulelearn.approximations.Union;
import org.rulelearn.data.*;

import java.io.IOException;
import java.util.HashMap;

public class NeighbourhoodAnalyzer implements DatasetOperation {
    private String jsonPath;
    private String csvPath;
    private String resultsPath;
    private DataSubsetExtractor dataExtractor;

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
        this.jsonPath = args[0];
        this.csvPath = args[1];
        this.resultsPath = args[2];
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
                InformationTableBuilder.safelyBuildFromCSVFile(this.jsonPath, this.csvPath, false));
        this.dataExtractor = new DataSubsetExtractor(informationTable);
    }

    private void analyze() {
        Union[] atLeastUnions = this.dataExtractor.getAtLeastUnions();
        Union[] atMostUnions = this.dataExtractor.getAtMostUnions();
        unionVsUnionAnalysis(atLeastUnions, atMostUnions);
        HashMap<Decision, InformationTable> classesByDecision = this.dataExtractor.getClassesByDecision();
        classVsUnionAnalysis(classesByDecision, atLeastUnions, atMostUnions);
    }

    private void unionVsUnionAnalysis(Union[] atLeastUnions, Union[] atMostUnions) {
//        kNearestAnalysis();
//        kernelAnalysis();
    }

    private void classVsUnionAnalysis(HashMap<Decision, InformationTable> classesByDecision, Union[] atLeastUnions, Union[] atMostUnions) {
//        kNearestAnalysis();
//        kernelAnalysis();
    }

    private void kNearestAnalysis(InformationTable minorotyExamples, InformationTable majorityExamples) {

    }

    private void kernelAnalysis(InformationTable minorotyExamples, InformationTable majorityExamples) {

    }

    private void saveResults() {

    }
}
