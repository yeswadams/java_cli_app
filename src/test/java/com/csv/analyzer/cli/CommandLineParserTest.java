package com.csv.analyzer.cli;

import com.csv.analyzer.domain.TransactionType;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CommandLineParserTest {

    private final CommandLineParser parser = new CommandLineParser();

    @Test
    void testEmptyArgsDefaultsToHelp() {
        parser.parse(new String[]{});
        assertEquals(Command.HELP, parser.getCommand());
    }

    @Test
    void testNullArgsDefaultsToHelp() {
        parser.parse(null);
        assertEquals(Command.HELP, parser.getCommand());
    }

    @Test
    void testUnknownCommandDefaultsToHelp() {
        parser.parse(new String[]{"not-a-real-command", "data.csv"});
        assertEquals(Command.HELP, parser.getCommand());
    }

    @Test
    void testExplicitHelpCommand() {
        parser.parse(new String[]{"help"});
        assertEquals(Command.HELP, parser.getCommand());
    }

    @Test
    void testMissingFilePathThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> parser.parse(new String[]{"summary"}));

        assertTrue(ex.getMessage().contains("Missing target CSV file parameter path"));
    }

    @Test
    void testValidSummaryCommand() {
        parser.parse(new String[]{"summary", "transactions.csv"});

        assertEquals(Command.SUMMARY, parser.getCommand());
        assertEquals(Path.of("transactions.csv"), parser.getFilePath());
    }

    @Test
    void testValidCategoriesCommandCaseInsensitive() {
        parser.parse(new String[]{"CATEGORIES", "data/test.csv"});

        assertEquals(Command.CATEGORIES, parser.getCommand());
        assertEquals(Path.of("data/test.csv"), parser.getFilePath());
    }

    @Test
    void testValidMonthlyCommand() {
        parser.parse(new String[]{"monthly", "data/test.csv"});

        assertEquals(Command.MONTHLY, parser.getCommand());
        assertEquals(Path.of("data/test.csv"), parser.getFilePath());
    }

    @Test
    void testSearchCommandWithOptions() {
        parser.parse(new String[]{"search", "data.csv", "--type", "EXPENSE", "--category", "Food"});

        assertEquals(Command.SEARCH, parser.getCommand());
        assertEquals(Path.of("data.csv"), parser.getFilePath());
        assertEquals(TransactionType.EXPENSE, parser.getFilterType());
        assertEquals("Food", parser.getFilterCategory());
    }
}
