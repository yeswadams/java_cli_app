package com.csv.analyzer.csv;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvParserTest {

    @Test
    void testStandardLineParsing() throws CsvParseException {
        String line = "TXN-001,2026-01-05,Salary Payment,INCOME,Salary,5000.00";
        List<String> tokens = CsvParser.parseLine(line, 1);

        assertEquals(6, tokens.size());
        assertEquals("TXN-001", tokens.get(0));
        assertEquals("2026-01-05", tokens.get(1));
        assertEquals("Salary Payment", tokens.get(2));
        assertEquals("INCOME", tokens.get(3));
        assertEquals("Salary", tokens.get(4));
        assertEquals("5000.00", tokens.get(5));
    }

    @Test
    void testQuotedCommaPreserved() throws CsvParseException {
        String line = "TXN-002,2026-01-06,\"Coffee, Tea, and Snacks\",EXPENSE,Food,15.50";
        List<String> tokens = CsvParser.parseLine(line, 2);

        assertEquals(6, tokens.size());
        assertEquals("Coffee, Tea, and Snacks", tokens.get(2));
    }

    @Test
    void testEscapedDoubleQuotes() throws CsvParseException {
        String line = "TXN-003,2026-01-07,\"He said \"\"Approved\"\"\",EXPENSE,Legal,300.00";
        List<String> tokens = CsvParser.parseLine(line, 3);

        assertEquals(6, tokens.size());
        assertEquals("He said \"Approved\"", tokens.get(2));
    }

    @Test
    void testUnclosedQuoteThrowsException() {
        String line = "TXN-004,2026-01-08,\"Unclosed field,EXPENSE,Food,20.00";

        CsvParseException exception = assertThrows(CsvParseException.class,
                () -> CsvParser.parseLine(line, 4),
                "Unclosed quotation mark must throw CsvParseException.");

        assertEquals(4, exception.getLineNumber());
        assertTrue(exception.getMessage().contains("Unclosed quotation mark"));
    }

    @Test
    void testNullLineReturnsEmptyList() throws CsvParseException {
        List<String> tokens = CsvParser.parseLine(null, 1);
        assertNotNull(tokens);
        assertTrue(tokens.isEmpty());
    }

    @Test
    void testWhitespaceTrimming() throws CsvParseException {
        String line = "  TXN-005 , 2026-01-09 , Grocery , EXPENSE , Food , 45.00 ";
        List<String> tokens = CsvParser.parseLine(line, 5);

        assertEquals("TXN-005", tokens.get(0));
        assertEquals("2026-01-09", tokens.get(1));
        assertEquals("Grocery", tokens.get(2));
        assertEquals("EXPENSE", tokens.get(3));
        assertEquals("Food", tokens.get(4));
        assertEquals("45.00", tokens.get(5));
    }
}
