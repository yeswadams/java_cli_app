package com.csv.analyzer.csv;

import java.util.ArrayList;
import java.util.List;

public class CsvParser {
    /*
     * Build a State Machine:
     * - Look at a single raw string from a CSV file
     * - Break it down to individual pieces - tokens
     * - Strictly respect double quotes and escaped quotes
     * - No external regex dependencies
     */

    public static List<String> parseLine(String line, int lineNumber) throws CsvParseException {
        List<String> tokens = new ArrayList<>();
        if (line == null) {
            return tokens;
        }

        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        int len = line.length();

        for (int i = 0; i < len; i++) {
            char c = line.charAt(i);

            // Handle double quotes
            if (c == '"') {
                if (inQuotes && i + 1 < len && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            // Handle column delimiter commas
            } else if (c == ',') {
                if (inQuotes) {
                    sb.append(c);
                } else {
                    tokens.add(sb.toString().trim());
                    sb.setLength(0); // Reset buffer for the next field
                }
            } else {
                sb.append(c);
            }
        }

        if (inQuotes) {
            throw new CsvParseException("Unclosed quotation mark detected", lineNumber);
        }

        tokens.add(sb.toString().trim());
        return tokens;
    }
}
