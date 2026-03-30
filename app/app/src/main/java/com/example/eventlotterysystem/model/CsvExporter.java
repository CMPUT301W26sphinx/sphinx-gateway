package com.example.eventlotterysystem.model;

import java.util.List;

/**
 * This class is used to export the registered entrants to a csv file.
 * @author Jaylin
 */
public class CsvExporter {
    // I looked at this source for an idea on how to build the csv file formatted strings:
    // https://stackoverflow.com/questions/42844117/stringbuilder-export-csv-with-quotation-marks-at-the-beginning
    // Author: Tim Kathete Stadler Mar 16 2017
    public static String buildRegisteredEntrantsCsv(List<EntrantDisplay> entrants) {
        StringBuilder sb = new StringBuilder();
        sb.append("name,email\n");
        for (EntrantDisplay entrant : entrants) {
            if (entrant == null || entrant.getStatus() != EntrantListEntry.STATUS_REGISTERED) {
                continue;
            }
            sb.append(escape(entrant.getFullName())).append(",");
            sb.append(escape(entrant.getEmail())).append("\n");
        }
        return sb.toString();
    }

    /**
     * This method is used to fix any improper formatting in the csv file.
     * @param value
     *  The string to fix/check.
     * @return the string for the csv file.
     */
    private static String escape(String value) {
        // checks for proper formatting
        if (value == null) {
            value = "";
        }
        // make sure don't break formating in the csv if it is in the string
        boolean needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n");
        value = value.replace("\"", "\"\"");

        return needsQuotes ? "\"" + value + "\"" : value;
    }
}