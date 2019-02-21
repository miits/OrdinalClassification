package put.main;

import put.utils.JsonCsvDatasetIterator;
import put.utils.NeighbourhoodAnalyzer;

public class Main {

    public static void main(String[] args) {
        String jsonPath = ".\\data\\json";
        String csvPath = ".\\data\\csv";
        String resultsPath = ".\\results";
        JsonCsvDatasetIterator iterator = new JsonCsvDatasetIterator(jsonPath, csvPath, resultsPath);
        NeighbourhoodAnalyzer analyzer = new NeighbourhoodAnalyzer();
        iterator.iterate(analyzer);
    }
}
