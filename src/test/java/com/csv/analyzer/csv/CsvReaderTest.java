package com.csv.analyzer.csv;

import com.csv.analyzer.domain.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvReaderTest {

    /*
     * =========================================================================
     * CSV READER TESTING PLAN
     * =========================================================================
     * 1. Validate that our state machine correctly parses inline commas inside quotes.
     * 2. Ensure empty or structurally invalid files break loudly via standard custom exceptions.
     * 3. Utilize isolated temporary directories to remain completely machine-independent.
     * 4. Verify duplicate transaction IDs are deduplicated/dropped with fault isolation.
     * 5. Verify malformed rows are skipped while valid rows are successfully processed.
     * =========================================================================
     */

    private final CsvReader reader = new CsvReader();

    @Test
    void testValidFileParsingWithQuotedCommas(@TempDir Path tempDir) throws Exception {
        // Arrange
        Path csvFile = tempDir.resolve("valid_transactions.csv");
        String mockContent = """
                id,date,description,type,category,amount
                TXN-001,2026-01-05,"Salary, January base bonus",INCOME,Salary,85000.00
                TXN-002,2026-01-06,Rent processing,EXPENSE,Housing,25000.00
                """;
        Files.writeString(csvFile, mockContent);

        // Act
        List<Transaction> result = reader.loadTransactions(csvFile);

        // Assert
        assertEquals(2, result.size(), "Parser should have extracted exactly 2 valid record objects.");
        assertEquals("Salary, January base bonus", result.get(0).description());
        assertEquals("TXN-002", result.get(1).id());
    }

    @Test
    void testEmptyFileThrowsException(@TempDir Path tempDir) throws IOException {
        // Arrange
        Path emptyCsv = tempDir.resolve("empty.csv");
        Files.writeString(emptyCsv, ""); // Create a completely empty file without a header row

        // Act & Assert
        assertThrows(CsvParseException.class, () -> reader.loadTransactions(emptyCsv),
                "Loading an empty dataset file must throw a structural CsvParseException.");
    }

    @Test
    void testMissingFileGracefullyThrowsIOException() {
        // Arrange
        Path deadPath = Path.of("this_file_does_not_exist_anywhere.csv");

        // Act & Assert
        assertThrows(IOException.class, () -> reader.loadTransactions(deadPath),
                "Accessing a non-existent filesystem path must trigger a clear IOException.");
    }

    @Test
    void testMissingRequiredHeaderThrowsException(@TempDir Path tempDir) throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("missing_header.csv");
        String mockContent = """
                id,date,description,type,amount
                TXN-001,2026-01-05,Salary,INCOME,85000.00
                """;
        Files.writeString(csvFile, mockContent);

        // Act & Assert
        assertThrows(CsvParseException.class, () -> reader.loadTransactions(csvFile),
                "Missing 'category' header must throw a CsvParseException.");
    }

    @Test
    void testFaultIsolationSkipsMalformedRowsAndDuplicates(@TempDir Path tempDir) throws Exception {
        // Arrange
        Path csvFile = tempDir.resolve("fault_tolerance.csv");
        String mockContent = """
                id,date,description,type,category,amount
                TXN-001,2026-01-05,First Income,INCOME,Salary,5000.00
                TXN-002,invalid-date,Bad Date,EXPENSE,Food,100.00
                TXN-003,2026-01-07,Missing Column,EXPENSE,Food
                TXN-001,2026-01-08,Duplicate ID,INCOME,Bonus,2000.00
                TXN-004,2026-01-09,Negative Amount,EXPENSE,Food,-50.00
                TXN-005,2026-01-10,Valid Expense,EXPENSE,Utilities,150.00
                """;
        Files.writeString(csvFile, mockContent);

        // Act
        List<Transaction> result = reader.loadTransactions(csvFile);

        // Assert - Only TXN-001 and TXN-005 should survive
        assertEquals(2, result.size(), "Fault isolation must filter out corrupt/duplicate records.");
        assertEquals("TXN-001", result.get(0).id());
        assertEquals("TXN-005", result.get(1).id());
    }
}