package org.ordinalclassification.utils;

import org.rulelearn.data.InformationTable;

public class InformationTableUtils {
    public static void checkTablesLength(int length, InformationTable[] tables) {
        for (InformationTable table: tables) {
            try {
                checkObjectsNumberIs(length, table);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void checkObjectsNumberIs(int number, InformationTable table) {
        if (!(table.getNumberOfObjects() == number)) {
            throw new IllegalArgumentException("Wrong objects number");
        }
    }
}
