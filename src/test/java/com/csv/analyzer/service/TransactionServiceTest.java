package com.csv.analyzer.service;

import com.csv.analyzer.domain.Transaction;
import com.csv.analyzer.domain.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TransactionServiceTest {

    /*
     * =========================================================================
     * SERVICE LAYER TESTING PLAN
     * =========================================================================
     * 1. Assert exact decimal math handling across precision inputs.
     * 2. Ensure empty or null data parameters fail gracefully without NullPointerExceptions.
     * 3. Confirm target peak aggregation logic operates flawlessly.
     * 4. Verify categorical net balance calculations (income positive, expenses negative).
     * 5. Verify chronological monthly grouping.
     * 6. Test flexible filtering and sorting combinations.
     * =========================================================================
     */

    private final TransactionService service = new TransactionService();

    @Test
    void testSummaryArithmeticWithoutPrecisionLoss() {
        // Arrange
        List<Transaction> dataset = List.of(
                new Transaction("T1", LocalDate.of(2026, 1, 1), "D1", TransactionType.INCOME, "Salary", new BigDecimal("0.10")),
                new Transaction("T2", LocalDate.of(2026, 1, 2), "D2", TransactionType.INCOME, "Salary", new BigDecimal("0.20")),
                new Transaction("T3", LocalDate.of(2026, 1, 3), "D3", TransactionType.EXPENSE, "Food", new BigDecimal("0.15"))
        );

        // Act
        SummaryReport report = service.calculateSummary(dataset);

        // Assert
        assertEquals(new BigDecimal("0.30"), report.totalIncome(), "Income aggregation drifted from exact scaling values.");
        assertEquals(new BigDecimal("0.15"), report.totalExpenses(), "Expense math calculations failed precision tests.");
        assertEquals(new BigDecimal("0.15"), report.netBalance(), "Net balance calculation matrix failed precision tests.");
        assertEquals(3, report.totalCount());
        assertTrue(report.largestIncome().isPresent());
        assertEquals("T2", report.largestIncome().get().id());
        assertTrue(report.largestExpense().isPresent());
        assertEquals("T3", report.largestExpense().get().id());
    }

    @Test
    void testEmptyCollectionsHandledGracefully() {
        // Act
        SummaryReport report = service.calculateSummary(Collections.emptyList());

        // Assert
        assertNotNull(report, "Summary payload must not be null even when processing empty arguments.");
        assertEquals(0, report.totalCount());
        assertEquals(new BigDecimal("0.00"), report.totalIncome());
        assertEquals(new BigDecimal("0.00"), report.totalExpenses());
        assertEquals(new BigDecimal("0.00"), report.netBalance());
        assertTrue(report.largestIncome().isEmpty(), "Largest income target container must remain empty.");
        assertTrue(report.largestExpense().isEmpty(), "Largest expense target container must remain empty.");
    }

    @Test
    void testNullCollectionsHandledGracefully() {
        SummaryReport report = service.calculateSummary(null);
        assertNotNull(report);
        assertEquals(0, report.totalCount());

        Map<String, BigDecimal> categories = service.aggregateByCategory(null);
        assertNotNull(categories);
        assertTrue(categories.isEmpty());

        Map<YearMonth, Map<TransactionType, BigDecimal>> monthly = service.aggregateMonthly(null);
        assertNotNull(monthly);
        assertTrue(monthly.isEmpty());

        List<Transaction> filtered = service.filterAndSort(null, null, null, null);
        assertNotNull(filtered);
        assertTrue(filtered.isEmpty());
    }

    @Test
    void testAggregateByCategoryCalculatesNet() {
        List<Transaction> dataset = List.of(
                new Transaction("T1", LocalDate.of(2026, 1, 1), "D1", TransactionType.INCOME, "Salary", new BigDecimal("3000.00")),
                new Transaction("T2", LocalDate.of(2026, 1, 2), "D2", TransactionType.EXPENSE, "Food", new BigDecimal("50.00")),
                new Transaction("T3", LocalDate.of(2026, 1, 3), "D3", TransactionType.EXPENSE, "Food", new BigDecimal("30.00")),
                new Transaction("T4", LocalDate.of(2026, 1, 4), "D4", TransactionType.INCOME, "Food", new BigDecimal("10.00")) // Cash back
        );

        Map<String, BigDecimal> result = service.aggregateByCategory(dataset);

        assertEquals(new BigDecimal("3000.00"), result.get("Salary"));
        // Food: -50.00 - 30.00 + 10.00 = -70.00
        assertEquals(new BigDecimal("-70.00"), result.get("Food"));
    }

    @Test
    void testAggregateMonthlyGrouping() {
        List<Transaction> dataset = List.of(
                new Transaction("T1", LocalDate.of(2026, 1, 15), "D1", TransactionType.INCOME, "Salary", new BigDecimal("4000.00")),
                new Transaction("T2", LocalDate.of(2026, 1, 20), "D2", TransactionType.EXPENSE, "Rent", new BigDecimal("1200.00")),
                new Transaction("T3", LocalDate.of(2026, 2, 10), "D3", TransactionType.EXPENSE, "Travel", new BigDecimal("500.00"))
        );

        Map<YearMonth, Map<TransactionType, BigDecimal>> monthly = service.aggregateMonthly(dataset);

        assertEquals(2, monthly.size());

        YearMonth jan = YearMonth.of(2026, 1);
        assertEquals(new BigDecimal("4000.00"), monthly.get(jan).get(TransactionType.INCOME));
        assertEquals(new BigDecimal("1200.00"), monthly.get(jan).get(TransactionType.EXPENSE));

        YearMonth feb = YearMonth.of(2026, 2);
        assertEquals(new BigDecimal("500.00"), monthly.get(feb).get(TransactionType.EXPENSE));
    }

    @Test
    void testFilterAndSortOperations() {
        List<Transaction> dataset = List.of(
                new Transaction("T1", LocalDate.of(2026, 1, 10), "D1", TransactionType.INCOME, "Salary", new BigDecimal("5000.00")),
                new Transaction("T2", LocalDate.of(2026, 1, 5), "D2", TransactionType.EXPENSE, "Food", new BigDecimal("100.00")),
                new Transaction("T3", LocalDate.of(2026, 1, 20), "D3", TransactionType.EXPENSE, "Entertainment", new BigDecimal("200.00")),
                new Transaction("T4", LocalDate.of(2026, 1, 1), "D4", TransactionType.EXPENSE, "Food", new BigDecimal("50.00"))
        );

        // Filter by type EXPENSE
        List<Transaction> expenses = service.filterAndSort(dataset, TransactionType.EXPENSE, null, null);
        assertEquals(3, expenses.size());
        // Default sort is by date ascending: T4 (Jan 1), T2 (Jan 5), T3 (Jan 20)
        assertEquals("T4", expenses.get(0).id());
        assertEquals("T2", expenses.get(1).id());
        assertEquals("T3", expenses.get(2).id());

        // Filter by category Food (case-insensitive)
        List<Transaction> food = service.filterAndSort(dataset, null, "food", null);
        assertEquals(2, food.size());
        assertEquals("T4", food.get(0).id());
        assertEquals("T2", food.get(1).id());

        // Custom comparator: amount descending
        List<Transaction> sortedByAmountDesc = service.filterAndSort(dataset, null, null,
                Comparator.comparing(Transaction::amount).reversed());
        assertEquals("T1", sortedByAmountDesc.get(0).id()); // 5000
        assertEquals("T3", sortedByAmountDesc.get(1).id()); // 200
        assertEquals("T2", sortedByAmountDesc.get(2).id()); // 100
        assertEquals("T4", sortedByAmountDesc.get(3).id()); // 50
    }
}
