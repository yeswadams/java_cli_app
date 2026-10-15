package com.csv.analyzer.csv;

import com.csv.analyzer.domain.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertioms.*;

class CsvReaderTest {

    /*
     * =========================================================================
     * CSV LAYER TESTING PLAN
     * =========================================================================
     * 1. Validate that our state machine correctly parses inline commas inside quotes.
     * 2. Ensure empty or structurally invalid files break loudly via standard custom exceptions.
     * 3. Utilize isolated temporary directories to remain completely machine-independent.
     * =========================================================================
     */

    private final CsvReader reader = new CsvReader();

    // WHAT: JUnit 5 @TempDir automatically provisions a temporary folder on the local machine's hard drive.
    // WHY: Hardcoding file paths like "C:/Users/test.csv" will crash instantly if run on a Linux build server.
    // Using @TempDir guarantees an isolated, cross-platform sandbox file lifecycle that deletes itself after execution.
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
        // WHY: We assert explicit values to verify our state machine successfully bypassed the nested comma
        // inside the double quotes instead of splitting it into broken, misaligned columns.
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
        // WHY: An empty file cannot map a schema domain. We explicitly verify that a CsvParseException
        // is thrown to keep data streaming rules intact.
        assertThrows(CsvParseException.class, () -> reader.loadTransactions(emptyCsv),
                "Loading an empty dataset file must throw a structural CsvParseException.");
    }

    @Test
    void testMissingFileGracefullyThrowsIOException() {
        // Arrange
        Path deadPath = Path.of("this_file_does_not_exist_anywhere.csv");

        // Act & Assert
        // WHY: Enterprise architectures must never return null or empty lists when an essential resource is missing.
        // Throwing a standard IOException tells the controller to flag an environmental failure immediately.
        assertThrows(IOException.class, () -> reader.loadTransactions(deadPath),
                "Accessing a non-existent filesystem path must trigger a clear IOException.");
    }
}