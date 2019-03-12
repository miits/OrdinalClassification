package put.utils;

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
//        Test
        jsonFiles = getTestJson();
        csvFiles = getTestCsv();
//        Test
        Iterator<File> jsonIterator = Arrays.stream(jsonFiles).iterator();
        Iterator<File> csvIterator = Arrays.stream(csvFiles).iterator();
        while (jsonIterator.hasNext() && csvIterator.hasNext()) {
            File json = jsonIterator.next();
            File csv = csvIterator.next();
            String resultsPath = String.format("%s\\%s", this.resultsPath, FilenameUtils.removeExtension(json.getName()));
            String[] paths = {json.getPath(), csv.getPath(), resultsPath};
            operation.carryOut(paths);
        }
    }

    private File[] getFiles(String directoryPath) {
        File[] files = new File(directoryPath).listFiles();
        return files;
    }

    private File[] getTestCsv() {
        File[] files = new File[] {new File(this.csvPath + "\\car.csv")};
        return files;
    }

    private File[] getTestJson() {
        File[] files = new File[] {new File(this.jsonPath + "\\car.json")};
        return files;
    }
}
