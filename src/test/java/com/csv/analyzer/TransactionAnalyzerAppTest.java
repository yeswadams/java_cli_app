package com.csv.analyzer;

import com.csv.analyzer.cli.CommandLineParser;
import com.csv.analyzer.config.AppContainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TransactionAnalyzerAppTest {

    private final AppContainer container = new AppContainer();

    @Test
    void testHelpOnEmptyArguments() {
        int code = TransactionAnalyzerApp.run(new String[]{}, container, new CommandLineParser());
        assertEquals(0, code);
    }

    @Test
    void testMissingFileReturnsErrorCode() {
        int code = TransactionAnalyzerApp.run(new String[]{"summary"}, container, new CommandLineParser());
        assertEquals(1, code);
    }

    @Test
    void testNonExistentFileReturnsErrorCode() {
        int code = TransactionAnalyzerApp.run(new String[]{"summary", "does_not_exist.csv"}, container, new CommandLineParser());
        assertEquals(1, code);
    }

    @Test
    void testSuccessfulExecutionOfAllCommands(@TempDir Path tempDir) throws Exception {
        Path csvFile = tempDir.resolve("sample.csv");
        String content = """
                id,date,description,type,category,amount
                TXN-01,2026-01-10,Salary,INCOME,Salary,5000.00
                TXN-02,2026-01-15,Groceries,EXPENSE,Food,150.00
                TXN-03,2026-02-01,Freelance,INCOME,Bonus,800.00
                """;
        Files.writeString(csvFile, content);

        String pathStr = csvFile.toString();

        assertEquals(0, TransactionAnalyzerApp.run(new String[]{"summary", pathStr}, container, new CommandLineParser()));
        assertEquals(0, TransactionAnalyzerApp.run(new String[]{"categories", pathStr}, container, new CommandLineParser()));
        assertEquals(0, TransactionAnalyzerApp.run(new String[]{"monthly", pathStr}, container, new CommandLineParser()));
        assertEquals(0, TransactionAnalyzerApp.run(new String[]{"search", pathStr, "--type", "EXPENSE", "--category", "Food"}, container, new CommandLineParser()));
    }
}
