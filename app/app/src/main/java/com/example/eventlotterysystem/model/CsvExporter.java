package com.example.eventlotterysystem.model;

import android.content.Context;
import android.net.Uri;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

/**
 * This class is used to export the registered entrants to a csv file.
 * Creates CSV formatting.
 * @author Jaylin
 */
public class CsvExporter {
    // I looked at this source for an idea on how to build the csv file formatted strings:
    // https://stackoverflow.com/questions/42844117/stringbuilder-export-csv-with-quotation-marks-at-the-beginning
    // Author: Tim Kathete Stadler Mar 16 2017
    // Update: also looked at :
    // To figure out how to create a csv, I looked at these sources:
    // https://medium.com/@sanjayajosep/offline-first-challenge-making-csv-pdf-reports-right-on-android-faf2ee7946dc

    private final Context context;

    public CsvExporter(Context context) {
        this.context = context.getApplicationContext();
    }

    public void writeCsv(Uri uri, List<EntrantDisplay> entrants) throws IOException {
        OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
        if (outputStream == null) {
            throw new IOException("Could not open output stream");
        }

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            writer.write("name,email");
            writer.newLine();

            for (EntrantDisplay entrant : entrants) {
                writer.write(escapeCsv(entrant.getFullName()));
                writer.write(",");
                writer.write(escapeCsv(entrant.getEmail()));
                writer.newLine();
            }

            writer.flush();
        }
    }

    /**
     * This method is used to make sure that all input strings dont break the csv file.
     * @param value
     * @return
     */
    private String escapeCsv(String value) {
        if (value == null) {
            value = "";
        }

        boolean needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n");
        value = value.replace("\"", "\"\"");

        return needsQuotes ? "\"" + value + "\"" : value;
    }
}