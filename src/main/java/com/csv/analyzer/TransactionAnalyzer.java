package com.csv.analyzer;

/**
 * Entry point facade forwarding to {@link TransactionAnalyzerApp}.
 */
public class TransactionAnalyzer {

    public static void main(String[] args) {
        TransactionAnalyzerApp.main(args);
    }

    @Deprecated
    public static void man(String[] args) {
        main(args);
    }
}