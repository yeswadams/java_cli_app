package com.csv.analyzer.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    private final LocalDate today = LocalDate.of(2026, 1, 15);

    @Test
    void testValidTransactionCreation() {
        Transaction txn = new Transaction("T1", today, "Paycheck", TransactionType.INCOME, "Salary", new BigDecimal("1000.00"));

        assertEquals("T1", txn.id());
        assertEquals(today, txn.date());
        assertEquals("Paycheck", txn.description());
        assertEquals(TransactionType.INCOME, txn.type());
        assertEquals("Salary", txn.category());
        assertEquals(new BigDecimal("1000.00"), txn.amount());
    }

    @Test
    void testNullIdThrowsException() {
        assertThrows(NullPointerException.class,
                () -> new Transaction(null, today, "Desc", TransactionType.INCOME, "Salary", new BigDecimal("100.00")));
    }

    @Test
    void testEmptyIdThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Transaction("  ", today, "Desc", TransactionType.INCOME, "Salary", new BigDecimal("100.00")));
    }

    @Test
    void testNullDateThrowsException() {
        assertThrows(NullPointerException.class,
                () -> new Transaction("T1", null, "Desc", TransactionType.INCOME, "Salary", new BigDecimal("100.00")));
    }

    @Test
    void testNullDescriptionThrowsException() {
        assertThrows(NullPointerException.class,
                () -> new Transaction("T1", today, null, TransactionType.INCOME, "Salary", new BigDecimal("100.00")));
    }

    @Test
    void testNullTypeThrowsException() {
        assertThrows(NullPointerException.class,
                () -> new Transaction("T1", today, "Desc", null, "Salary", new BigDecimal("100.00")));
    }

    @Test
    void testNullCategoryThrowsException() {
        assertThrows(NullPointerException.class,
                () -> new Transaction("T1", today, "Desc", TransactionType.INCOME, null, new BigDecimal("100.00")));
    }

    @Test
    void testBlankCategoryThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Transaction("T1", today, "Desc", TransactionType.INCOME, "   ", new BigDecimal("100.00")));
    }

    @Test
    void testNullAmountThrowsException() {
        assertThrows(NullPointerException.class,
                () -> new Transaction("T1", today, "Desc", TransactionType.INCOME, "Salary", null));
    }

    @Test
    void testZeroAmountThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Transaction("T1", today, "Desc", TransactionType.INCOME, "Salary", BigDecimal.ZERO));
    }

    @Test
    void testNegativeAmountThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Transaction("T1", today, "Desc", TransactionType.INCOME, "Salary", new BigDecimal("-50.00")));
    }
}
