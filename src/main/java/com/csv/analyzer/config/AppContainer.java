// App factory
package com.csv.analyzer.config;

import com.csv.analyzer.cli.ConsolePrinter;
import com.csv.analyzer.csv.CsvReader;
import com.csv.analyzer.service.TransactionService;

public class AppContainer {
    /*
    * Plan
    * 1. Initialize and maintain Singletons(single instances) of core system layers.
    * 2. Provide clean getter endpoints to wire dependencies up clearly.
    * */

    private final CsvReader csvReader;
    private final TransactionService transactionService;
    private final ConsolePrinter consolePrinter;

//    Constructor builds the instance objects exactly once.
    public AppContainer() {
        this.csvReader = new CsvReader();
        this.transactionService = new TransactionService();
        this.consolePrinter = new ConsolePrinter();
    }

//    Securely expose references out to the application orchestrator main thread
    public CsvReader getCsvReader() { return csvReader; }
    public TransactionService getTransactionService() { return transactionService; }
    public ConsolePrinter getConsolePrinter() { return consolePrinter; }
}
