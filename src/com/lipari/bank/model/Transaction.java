package com.lipari.bank.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Movimento contabile immutabile (record Java 21).
 */
public record Transaction(
        TransactionType type,
        BigDecimal amount,
        String description,
        LocalDateTime timestamp) {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public Transaction {
        Objects.requireNonNull(type,        "tipo obbligatorio");
        Objects.requireNonNull(amount,      "importo obbligatorio");
        Objects.requireNonNull(description, "descrizione obbligatoria");
        Objects.requireNonNull(timestamp,   "timestamp obbligatorio");
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Importo non può essere negativo");
        }
    }

    @Override
    public String toString() {
        return String.format("[%s] %-10s %10.2f€  %s",
                timestamp.format(FMT), type.getLabel(), amount, description);
    }
}
