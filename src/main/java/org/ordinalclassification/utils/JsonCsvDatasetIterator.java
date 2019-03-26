package org.ordinalclassification.utils;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;

public class JsonCsvDatasetIterator {
    private String jsonPath;
    private String csvPath;
    private String resultsPath;

    public JsonCsvDatasetIterator(String jsonPath, String csvPath, String resultsPath) {
        this.jsonPath = jsonPath;
        this.csvPath = csvPath;
        this.resultsPath = resultsPath;
    }

    public void iterate(DatasetOperation operation) {
        File[] jsonFiles = getFiles(this.jsonPath);
        File[] csvFiles = getFiles(this.csvPath);
        Iterator<File> jsonIterator = Arrays.stream(jsonFiles).iterator();
        Iterator<File> csvIterator = Arrays.stream(csvFiles).iterator();
        int datasetsCount = jsonFiles.length;
        int counter = 0;
        while (jsonIterator.hasNext() && csvIterator.hasNext()) {
            File json = jsonIterator.next();
            File csv = csvIterator.next();
            String datasetName = FilenameUtils.removeExtension(json.getName());
            String resultsPath = String.format("%s\\%s", this.resultsPath, datasetName);
            String[] paths = {json.getPath(), csv.getPath(), resultsPath};
            System.out.println(String.format("[JsonCsvDatasetIterator] Processing dataset: %s (%d/%d)", datasetName, ++counter, datasetsCount));
            operation.carryOut(paths);
        }
    }

    private File[] getFiles(String directoryPath) {
        File[] files = new File(directoryPath).listFiles();
        return files;
    }

    private File[] getTestCsv() {
        File[] files = new File[] {new File("data\\test\\csv\\car.csv")};
        return files;
    }

    private File[] getTestJson() {
        File[] files = new File[] {new File("data\\test\\json\\car.json")};
        return files;
    }
}
