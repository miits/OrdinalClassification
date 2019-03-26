package org.ordinalclassification.main;

import org.ordinalclassification.utils.JsonCsvDatasetIterator;
import org.ordinalclassification.utils.NeighbourhoodAnalyzer;

public class Main {

    public static void main(String[] args) {
        String jsonPath = ".\\data\\nonOrdinal\\gen\\json";
        String csvPath = ".\\data\\nonOrdinal\\gen\\csv";
        String resultsPath = ".\\results\\nonOrdinal\\gen";
        JsonCsvDatasetIterator iterator = new JsonCsvDatasetIterator(jsonPath, csvPath, resultsPath);
        NeighbourhoodAnalyzer analyzer = new NeighbourhoodAnalyzer();
        iterator.iterate(analyzer);
    }
}
