package com.csv.analyzer.csv;

import java.util.ArrayList;
import java.util.List;

public class CsvParser {
/*    Plan -> Build a State Machine:
      - Look at a single raw string from a CSV file
      - Break it down to individual pieces - tokens
      - Strictly respect double quotes
      - no regex needed
*/

    public static List<String> parseLine(String line, int lineNumber) throws CsvParseException {
        List<String> tokens = new ArrayList<>(); // creating a new Array
        if(line == null) return tokens;

        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false; // commas are treated as column structural dividers
        int len = line.length();

        for (int i = 0; i < len; i++) {
            char c = line.charAt(i);

//          handle the quotes
            if (c == '"') {
                if(inQuotes && i + 1 < len && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
//          handle the commas
            } else if (c == ',') {
                if(inQuotes) {
                    sb.append(c);
                } else {
                    tokens.add(sb.toString().trim());
                    sb.setLength(0); // reset buffer for the next field
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