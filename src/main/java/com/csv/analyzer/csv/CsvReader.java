package com.csv.analyzer.csv;

import com.csv.analyzer.domain.Transaction;
import com.csv.analyzer.domain.TransactionType;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

public class CsvReader {

    // 1. Establish the Contract Headers
    private static final List<String> REQUIRED_HEADERS = List.of("id", "date", "description", "type", "category", "amount");

    public List<Transaction> loadTransactions(Path filePath) throws IOException, CsvParseException {
        // 2. Pre-flight check
        if (filePath == null || !Files.exists(filePath)) {
            throw new IOException("Target file does not exist: " + (filePath == null ? "null" : filePath.toAbsolutePath()));
        }

        List<Transaction> transactions = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();

        // 3. Open the stream efficiently
        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String headerLine = br.readLine();
            if (headerLine == null) {
                throw new CsvParseException("Empty dataset file provided", 1);
            }

            int lineNumber = 1;
            List<String> headers = CsvParser.parseLine(headerLine, lineNumber);
            Map<String, Integer> headerMap = mapHeaders(headers, lineNumber);

            // 4. Read the records with Fault Isolation
            String line;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) continue; // Skip cosmetic blank spacing rows

                try {
                    List<String> values = CsvParser.parseLine(line, lineNumber);

                    // Structural constraint check
                    if (values.size() != REQUIRED_HEADERS.size()) {
                        throw new CsvParseException("Column count mismatch. Expected " + REQUIRED_HEADERS.size() + " fields, but found " + values.size(), lineNumber);
                    }

                    Transaction txn = mapRowToTransaction(values, headerMap, lineNumber);

                    // Financial duplicate identification invariant
                    if (!seenIds.add(txn.id())) {
                        System.err.println("[WARN] Line " + lineNumber + ": Dropping duplicate transaction identifier: " + txn.id());
                        continue;
                    }
                    transactions.add(txn);

                } catch (CsvParseException | IllegalArgumentException | DateTimeParseException e) {
                    // Fault Isolation block: catch the error, print diagnostic, keep looping!
                    System.err.println("[ERROR] Skipping row processing error at line " + lineNumber + " -> " + e.getMessage());
                }
            }
        }
        return transactions;
    }

    // 5. Dynamic Header Mapping
    private Map<String, Integer> mapHeaders(List<String> headers, int lineNumber) throws CsvParseException {
        Map<String, Integer> headerMap = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            headerMap.put(headers.get(i).toLowerCase(), i);
        }
        for (String req : REQUIRED_HEADERS) {
            if (!headerMap.containsKey(req)) {
                throw new CsvParseException("Required column missing from file schema: " + req, lineNumber);
            }
        }
        return headerMap;
    }

    // 6. Explicit Object Extraction
    private Transaction mapRowToTransaction(List<String> values, Map<String, Integer> headerMap, int lineNum) {
        String id = values.get(headerMap.get("id"));
        String dateStr = values.get(headerMap.get("date"));
        String description = values.get(headerMap.get("description"));
        String typeStr = values.get(headerMap.get("type"));
        String category = values.get(headerMap.get("category"));
        String amountStr = values.get(headerMap.get("amount"));

        LocalDate date = LocalDate.parse(dateStr); // Throws DateTimeParseException if invalid format
        TransactionType type = TransactionType.valueOf(typeStr.toUpperCase()); // Throws IllegalArgumentException if unknown
        BigDecimal amount = new BigDecimal(amountStr); // Throws NumberFormatException if not a clean decimal string

        return new Transaction(id, date, description, type, category, amount); // Triggers constructor validation invariants
    }
}
