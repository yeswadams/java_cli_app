package com.csv.analyzer.service;

import com.csv.analyzer.domain.Transaction;
import java.math.BigDecimal;
import java.util.Optional;

/* Plan:
* Create an immutable data transfer object(DTO) that holds our global financial metric
* Using a Java record, it ensures that once these calculations are finished, they cannot be altered
* */
public record SummaryReport(
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal netBalance,
        long totalCount,
        Optional<Transaction> largestIncome,
        Optional<Transaction> largestExpense
) {}