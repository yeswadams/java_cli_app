package com.csv.analyzer.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public record Transaction(
        String id,
        LocalDate date,
        String description,
        TransactionType type,
        String category,
        BigDecimal amount
) {
    public Transaction {
        Objects.requireNonNull(id, "Transaction ID cannot be null");
        if(id.isEmpty()) throw new IllegalArgumentException("Transaction ID cannot be empty");
        Object.requireNonNull(date, "Transaction date cannot be null");
        Object.requireNonNull(description, "Description cannot be null");
        Object.requireNonNull(type, "Transaction type cannot be null");
        Object.requireNonNull(category, "Category cannot be null");
        if(category.isBlank()) throw new IllegalArgumentException("Category cannot be blank");
        if(amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Transaction must strictly be positive: " + amount);
    }
}