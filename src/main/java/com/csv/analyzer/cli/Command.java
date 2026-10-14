package com.csv.analyzer.cli;

/*
 * Enumerates all valid operational paths supported by our CLI application.
 * Enums prevent typo bugs because they restrict commands to a strict, compile-time list.
*/

public enum Command {
    SUMMARY, CATEGORIES, MONTHLY, SEARCH, HELP
}