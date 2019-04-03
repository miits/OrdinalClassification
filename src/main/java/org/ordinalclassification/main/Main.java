package org.ordinalclassification.main;

import org.ordinalclassification.utils.JsonCsvDatasetIterator;
import org.ordinalclassification.utils.NeighbourhoodAnalyzer;

public class Main {

    public static void main(String[] args) {
        if (args.length < 3) {
            throw new IllegalArgumentException("Argument missing");
        }
        JsonCsvDatasetIterator iterator = new JsonCsvDatasetIterator(args[0], args[1], args[2]);
        NeighbourhoodAnalyzer analyzer = new NeighbourhoodAnalyzer();
        iterator.iterate(analyzer);
    }
}
