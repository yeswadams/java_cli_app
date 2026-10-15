package com.csv.analyzer;

import com.csv.analyzer.cli.Command;
import com.csv.analyzer.cli.CommandLineParser;
import com.csv.analyzer.config.AppContainer;
import com.csv.analyzer.domain.Transaction;

import java.util.Comparator;
import java.util.List;

/**
 * Enterprise entry point and application orchestrator for the Transaction Analyzer CLI.
 */
public class TransactionAnalyzerApp {

    public static void main(String[] args) {
        AppContainer container = new AppContainer();
        CommandLineParser parser = new CommandLineParser();
        int exitCode = run(args, container, parser);
        System.exit(exitCode);
    }

    /**
     * Executes CLI commands with isolated error handling and testable exit codes.
     *
     * @param args CLI arguments passed to the application
     * @param container Inversion of Control container providing service dependencies
     * @param parser Command line parser instance
     * @return 0 on successful execution, 1 on failure
     */
    public static int run(String[] args, AppContainer container, CommandLineParser parser) {
        try {
            parser.parse(args);
        } catch (IllegalArgumentException e) {
            System.err.println("[CLI ERROR] Arguments validation failure: " + e.getMessage());
            container.getConsolePrinter().printHelp();
            return 1;
        }

        if (parser.getCommand() == Command.HELP) {
            container.getConsolePrinter().printHelp();
            return 0;
        }

        try {
            List<Transaction> dataset = container.getCsvReader().loadTransactions(parser.getFilePath());

            switch (parser.getCommand()) {
                case SUMMARY -> {
                    var report = container.getTransactionService().calculateSummary(dataset);
                    container.getConsolePrinter().printSummary(report);
                }
                case CATEGORIES -> {
                    var cats = container.getTransactionService().aggregateByCategory(dataset);
                    container.getConsolePrinter().printCategories(cats);
                }
                case MONTHLY -> {
                    var months = container.getTransactionService().aggregateMonthly(dataset);
                    container.getConsolePrinter().printMonthly(months);
                }
                case SEARCH -> {
                    var results = container.getTransactionService().filterAndSort(
                            dataset, parser.getFilterType(), parser.getFilterCategory(), Comparator.comparing(Transaction::date)
                    );
                    container.getConsolePrinter().printTransactions(results);
                }
                default -> throw new IllegalStateException("Unexpected routing branch: " + parser.getCommand());
            }
            return 0;

        } catch (Exception e) {
            System.err.println("[FATAL SYSTEM EXECUTION FAILURE]: " + e.getMessage());
            return 1;
        }
    }
}
