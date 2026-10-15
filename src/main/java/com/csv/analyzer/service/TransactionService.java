package com.csv.analyzer.service;

import com.csv.analyzer.domain.Transaction;
import com.csv.analyzer.domain.TransactionType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

public class TransactionService {

    /*
     * SERVICE ARCHITECTURE:
     * 1. Execute exact-decimal mathematical summations to prevent rounding errors.
     * 2. Implement protective Optional containers for metrics that could be empty.
     * 3. Group and sort financial data natively using pure Java Stream APIs.
     * 4. Defensively handle null dataset references.
     */

    public SummaryReport calculateSummary(List<Transaction> txns) {
        if (txns == null) {
            txns = Collections.emptyList();
        }

        BigDecimal income = txns.stream()
                .filter(t -> t.type() == TransactionType.INCOME)
                .map(Transaction::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal expenses = txns.stream()
                .filter(t -> t.type() == TransactionType.EXPENSE)
                .map(Transaction::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal net = income.subtract(expenses).setScale(2, RoundingMode.HALF_UP);

        Optional<Transaction> maxInc = txns.stream()
                .filter(t -> t.type() == TransactionType.INCOME)
                .max(Comparator.comparing(Transaction::amount));

        Optional<Transaction> maxExp = txns.stream()
                .filter(t -> t.type() == TransactionType.EXPENSE)
                .max(Comparator.comparing(Transaction::amount));

        return new SummaryReport(income, expenses, net, txns.size(), maxInc, maxExp);
    }

    public Map<String, BigDecimal> aggregateByCategory(List<Transaction> txns) {
        if (txns == null) {
            return Collections.emptyMap();
        }

        Map<String, BigDecimal> report = new HashMap<>();
        for (Transaction t : txns) {
            BigDecimal modifier = t.type() == TransactionType.INCOME ? t.amount() : t.amount().negate();
            report.merge(t.category(), modifier, BigDecimal::add);
        }
        return report;
    }

    public Map<YearMonth, Map<TransactionType, BigDecimal>> aggregateMonthly(List<Transaction> txns) {
        if (txns == null) {
            return Collections.emptyMap();
        }

        return txns.stream().collect(Collectors.groupingBy(
                t -> YearMonth.from(t.date()),
                TreeMap::new,
                Collectors.groupingBy(
                        Transaction::type,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::amount, BigDecimal::add)
                )
        ));
    }

    public List<Transaction> filterAndSort(List<Transaction> txns, TransactionType type, String category, Comparator<Transaction> sorter) {
        if (txns == null) {
            return Collections.emptyList();
        }

        return txns.stream()
                .filter(t -> type == null || t.type() == type)
                .filter(t -> category == null || t.category().equalsIgnoreCase(category))
                .sorted(sorter != null ? sorter : Comparator.comparing(Transaction::date))
                .toList();
    }
}