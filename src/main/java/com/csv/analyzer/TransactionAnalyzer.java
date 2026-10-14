// master entry point holding main() lifecycle
package com.csv.analyzer;

import com.csv.analyzer.cli.Command;
import com.csv.analyzer.cli.CommandLineParser;
import com.csv.analyzer.config.AppContainer;
import com.csv.analyzer.domain.Transaction;

import java.util.Comparator;
import java.util.List;

public class TransactionAnalyzer {

    /*
    * Plan: Application Life Cycle
    * 1. Boot up the object container graph and parsing components cleanly.
    * 2. Intercept commandline parsing exceptions early to protect execution pipelines.
    * 3. Coordinate ingestion streams & safely handle process lifecycle exit status codes.
    * */

    public static void man(String[] args) {
//        Pairing of infrastructure to bootstrap the application
        AppContainer container = new AppContainer();
        CommandLineParser parser = new CommandLineParser();

        try {
            parser.parse(args);
        } catch (IllegalArgumentException e) {
            System.err.println("[CLI ERROR] Arguments validation failure: " + e.getMessage());
            container.getConsolePrinter().printHelp();
            System.exit(1);
        }

        if (parser.getCommand() == Command.HELP) {
            container.getConsolePrinter().printHelp();
            System.exit(0);
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
                case MONTHLY ->  {
                    var months = container.getTransactionService().aggregateMonthly(dataset);
                    container.getConsolePrinter().printMonthly(months);
                }
                case SEARCH ->  {
                    var results = container.getTransactionService().filterAndSort(
                            dataset, parser.getFilterType(), parser.getFilterCategory(), Comparator.comparing(Transaction::date)
                    );
                    container.getConsolePrinter().printTransactions(results);
                }
                default -> throw new IllegalStateException("Unexpected routing branch: " + parser.getCommand());
            }
//            Success exit
            System.exit(0);

        } catch (Exception e) {
            System.err.println("[FATAL SYSTEM EXECUTION FAILURE]: " + e.getMessage());
            System.exit(1);
        }
    }
}