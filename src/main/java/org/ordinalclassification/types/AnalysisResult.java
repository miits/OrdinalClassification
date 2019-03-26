package org.ordinalclassification.types;

import org.rulelearn.data.Decision;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalysisResult {


    private List<ResultRow> results;
    private static String csvSeparator = ";";

    public AnalysisResult() {
        this.results = new ArrayList<>();
    }

    public void addResults(HashMap<Integer, LearningExampleType> assignment, Decision minorityUnionLimitingDecision, Decision majorityUnionLimitingDecision) {
        for (Map.Entry<Integer, LearningExampleType> entry: assignment.entrySet()) {
            ResultRow row = new ResultRow(entry.getKey(), entry.getValue(), minorityUnionLimitingDecision.toString(), majorityUnionLimitingDecision.toString());
            results.add(row);
        }
    }

    public void saveCsv(String filename) throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF-8"));
        writeHeaders(bw);
        for (ResultRow row: results)
        {
            StringBuffer oneLine = new StringBuffer();
            oneLine.append(row.getIndex());
            oneLine.append(csvSeparator);
            oneLine.append(row.getType());
            oneLine.append(csvSeparator);
            oneLine.append(row.getMinorityDecision());
            oneLine.append(csvSeparator);
            oneLine.append(row.getMajorityDecision());
            bw.write(oneLine.toString());
            bw.newLine();
        }
        bw.flush();
        bw.close();
    }

    private void writeHeaders(BufferedWriter bw) throws IOException {
        StringBuffer oneLine = new StringBuffer();
        oneLine.append("index");
        oneLine.append(csvSeparator);
        oneLine.append("type");
        oneLine.append(csvSeparator);
        oneLine.append("minority_decision");
        oneLine.append(csvSeparator);
        oneLine.append("majority_decision");
        bw.write(oneLine.toString());
        bw.newLine();
    }

    public List<ResultRow> getPerExampleResults() {
        return results;
    }
}
