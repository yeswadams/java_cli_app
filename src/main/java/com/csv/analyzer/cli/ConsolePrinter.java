package com.csv.analyzer.cli;

import com.csv.analyzer.domain.Transaction;
import com.csv.analyzer.domain.TransactionType;
import com.csv.analyzer.service.SummaryReport;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public class ConsolePrinter {

    /*
    * Plan: Architecture of the Printer
    * 1. Output clean visual breaks for easy scanning
    * 2. Format numerical data
    * 3. Safely open Optional domain types without triggering raw unboxing crashes.
    * */

    public void printHelp() {
        System.out.println("""
            Transaction Analyzer CLI - Usage Matrix:
            =========================================
            Usage: java -jar transaction-analyzer.jar <command> <file-path> [options]
            
            Commands:
              summary     Calculates global income, expenses, and current system balances.
              categories  Aggregates transactional entries grouped by categorical values.
              monthly     Renders chronological summaries grouped by calendar month.
              search      Queries transactions with optional filters and lists matches.
              help        Exposes command parameter guides.
              
            Options:
              --type <INCOME|EXPENSE>  Filters search output to a single transaction type.
              --category <Name>        Filters search output to a single target business category.
            """);
    }

    // Renders a formated block detailing summary report fields.
    public void printSummary(SummaryReport report) {
        System.out.println("""
            =============== FINANCIAL SUMMARY REPORT ===============
        """);
        System.out.printf("Processed Count : %d entries%n", report.totalCount());

        System.out.printf("Total Income : %,.2f%n", report.totalIncome());
        System.out.printf("Total Expenses : %,.2f%n", report.totalExpenses());
        System.out.printf("Net Account Bal : %,.2f%n", report.netBalance());

        report.largestIncome().ifPresent(t -> System.out.printf("Peak Income Record: %s (%,.2f)%n", t.description(), t.amount()));
        report.largestExpense().ifPresent(t -> System.out.printf("Peak Expense Record: %s (%,.2f)%n", t.description(), t.amount()));
    }

    public void printCategories(Map<String, BigDecimal> data) {
        System.out.println("====== NET TRACKING BY CATEGORY ===========");

        data.forEach((cat, amt) -> System.out.printf(" * %-15s : %,.2f%n", cat, amt));
    }

//    Renders chronological summaries
    public void printMonthly(Map<YearMonth, Map<TransactionType, BigDecimal>> data) {
        System.out.println("====== CHRONOLOGICAL MONTHLY LOGS ======");
        data.forEach((month, map) -> {
            BigDecimal inc = map.getOrDefault(TransactionType.INCOME, BigDecimal.ZERO);
            BigDecimal exp = map.getOrDefault(TransactionType.EXPENSE, BigDecimal.ZERO);

            System.out.printf(" [%s] -> Incoming: %,.2f | Spent: %,.2f%n", month, inc, exp);
        });
    }

    public void printTransactions(List<Transaction> list) {
        System.out.println("======== MATCHING TRACKED RECORDS =========");
        if (list.isEmpty()) {
            System.out.println("No matching records found for the specified criteria.");
            return;
        }

        for (Transaction t : list) {
            System.out.printf("%s | %s | %-20s | %-7s | %,.2f%n", t.id(), t.date(), t.description(), t.type(), t.amount());
        }
    }
}