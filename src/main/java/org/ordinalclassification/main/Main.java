package org.ordinalclassification.main;

import org.ordinalclassification.utils.JsonCsvDatasetIterator;
import org.ordinalclassification.utils.NeighbourhoodAnalyzer;

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
