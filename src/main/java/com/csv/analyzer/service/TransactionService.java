// main service class
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
    * Plan: SERVICE ARCHITECTURE:
    * 1. Execute exact-decimal mathematical summations to prevent rounding errors.
    * 2. Implement protective Optional containers for metrics that could be empty.
    * 3. Group and sort financial data natively using pure Java Stream APIs.
    * */

    public SummaryReport calculateSummary(List<Transaction> txns) {
//        Use BigDecimal and enforce a scale of 2

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

        BigDecimal net = income.subtract(expenses);

//        Optional helps us handle empty states safely
        Optional<Transaction> maxInc = txns.stream()
                .filter(t -> t.type() == TransactionType.INCOME)
                .max(Comparator.comparing(Transaction::amount));

        Optional<Transaction> maxExp = txns.stream()
                .filter(t -> t.type() == TransactionType.EXPENSE)
                .max(Comparator.comparing(Transaction::amount));

        return new SummaryReport(income, expenses, net, txns.size(), maxInc,maxExp);
    }

//    Aggregate financial totals categorized into positive / negative impacts.

    public Map<String, BigDecimal> aggregate(List<Transaction> txns) {
        Map<String, BigDecimal> report = new HashMap<>();

        for (Transaction t : txns) {
            /* Income increases our budget pool, while expenses reduces it */
            BigDecimal modifier = t.type() == TransactionType.INCOME ? t.amount() : t.amount().negate();
//        Check if the category key already exists if not intialize it with our modifier else combine them via BigDecimal::add
            report.merge(t.category(), modifier, BigDecimal::add);
        }
        return report;
    }

    public Map<YearMonth, Map<TransactionType, BigDecimal>> aggregateMonthly(List<Transaction> txns) {
        /*
        * We use a TreeMap instance provider here instead of a default HashMap.
        A TreeMap automatically keeps its keys sorted in their natural sorting order.
        This ensures our console output chronologically reads (Jan -> Feb -> Mar) without manual sorting arrays.
        * */
        return txns.stream().collect(Collectors.groupingBy(
                t -> YearMonth.from(t.date()),
                TreeMap::new,
                Collectors.groupingBy(
                        Transaction::type,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::amount, BigDecimal::add)
                )
        ));
    }

// Filters down datasets against flexible query constraints

    public List<Transaction> filterAndSort(List<Transaction> txns, TransactionType type, String category, Comparator<Transaction> sorter) {
        return txns.stream()
                .filter(t -> type == null || t.type() == type)
                .filter(t -> category == null || t.category().equalsIgnoreCase(category))
                .sorted(sorter != null ? sorter : Comparator.comparing(Transaction::date))
                .toList();
    }
}