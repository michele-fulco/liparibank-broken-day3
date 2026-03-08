package com.lipari.bank.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Classe base sealed per i conti bancari.
 */
public sealed class Account permits CheckingAccount, SavingsAccount {

    private final String iban;

    private BigDecimal balance;

    private final Customer owner;
    private final List<Transaction> transactions = new CopyOnWriteArrayList<>();

    public Account(String iban, BigDecimal initialBalance, Customer owner) {
        if (iban == null || iban.isBlank()) {
            throw new IllegalArgumentException("IBAN non valido");
        }
        if (initialBalance == null || initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Saldo iniziale non può essere negativo");
        }
        this.iban    = iban;
        this.balance = initialBalance;
        this.owner   = owner;
    }

    // ─── Getters ────────────────────────────────────────────────────────────

    public String     getIban()         { return iban; }
    public BigDecimal getBalance()      { return balance; }
    public Customer   getOwner()        { return owner; }
    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    // ─── Operazioni ─────────────────────────────────────────────────────────

    public void deposit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Importo deposito deve essere positivo");
        }
        this.balance = this.balance.add(amount);
        transactions.add(new Transaction(
                TransactionType.DEPOSIT, amount, "Deposito", LocalDateTime.now()));
    }

    /**
     * Preleva {@code amount} dal saldo del conto.
     */
    public void withdraw(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Importo prelievo deve essere positivo");
        }
        if (balance.subtract(amount).compareTo(getMinBalance()) < 0) {
            throw new IllegalStateException(String.format(
                    "Fondi insufficienti [%s]: disponibile=%.2f€, richiesto=%.2f€",
                    iban, balance, amount));
        }
        this.balance = this.balance.subtract(amount);
        transactions.add(new Transaction(
                TransactionType.WITHDRAWAL, amount, "Prelievo", LocalDateTime.now()));
    }

    /** Saldo minimo ammesso. Le sottoclassi possono fare override. */
    protected BigDecimal getMinBalance() {
        return BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        return String.format("IBAN: %-26s | Saldo: %12.2f€ | Titolare: %s",
                iban, balance, owner);
    }
}
