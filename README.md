# Transaction Analyzer CLI

A command-line application for analyzing financial transactions from CSV files, built with Java and the standard library.

Transaction Analyzer reads transaction records, validates input data, calculates financial summaries, and produces reports directly in the terminal. The project emphasizes correctness, explicit design decisions, comprehensive automated testing, and maintainable Java code without application frameworks.

## Project Goals

- Build a complete Java application without relying on tutorials or generated implementations.
- Apply object-oriented programming, collections, generics, exception handling, streams, and the Java type system to a practical problem.
- Process structured data while handling malformed input and invalid financial records.
- Perform accurate financial calculations using decimal arithmetic.
- Design clear boundaries between the domain model, data ingestion, business logic, and command-line interface.
- Establish confidence through comprehensive automated tests, including edge cases and failure scenarios.

## Features

> Implementation status: In development.

### CSV Ingestion

- Read transaction data from CSV files.
- Interpret and validate CSV headers and records.
- Handle quoted fields, embedded commas, and escaped quotation marks.
- Validate transaction identifiers, dates, types, categories, descriptions, and amounts.
- Report malformed records with useful error messages and line numbers where possible.
- Handle missing files, empty files, and invalid input.

### Financial Analysis

- Calculate total income and total expenses.
- Calculate net balance.
- Count transactions.
- Identify the largest income and expense transactions.
- Aggregate financial activity by category.
- Generate monthly financial summaries.
- Filter transactions by type, category, and date range.
- Sort transactions by date or amount.

### Command-Line Interface

- Execute analysis commands from the terminal.
- Display readable and consistent financial reports.
- Provide command usage information.
- Handle invalid arguments and input failures gracefully.
- Separate diagnostic messages from normal report output.
- Return appropriate process exit codes.

### Automated Testing

- Unit tests for domain behavior and financial calculations.
- CSV parsing and validation tests.
- Edge-case tests for empty datasets and malformed input.
- Integration tests for file processing.
- Command-line tests for output, errors, and exit behavior.

Features will be marked complete as their implementations and tests are verified.

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 25 | Application language and runtime |
| Maven | Build automation and dependency management |
| JUnit 5 | Automated testing |
| Java Standard Library | File I/O, collections, date/time handling, decimal arithmetic, and application logic |
| Git | Version control |

No application framework is required. The initial implementation does not depend on Spring, an ORM, Lombok, or a third-party CSV parsing library.

JUnit 5 is used exclusively for testing. Application dependencies will remain limited to what the project genuinely needs.

## Requirements

Before running the project, ensure you have:

- JDK 25 or a compatible configured Java 25 development environment.
- Apache Maven.
- Git.
- A terminal capable of running Java applications.

Verify your environment:

```bash
java -version
javac -version
mvn -version
git --version