package com.lipari.bank.service;

import com.lipari.bank.model.Account;
import com.lipari.bank.model.Transaction;
import com.lipari.bank.model.TransactionType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servizio di reporting su conti e transazioni tramite Stream API.
 */
public class ReportingService {

    private final List<Transaction> debugLog = new ArrayList<>();

    /**
     * Conta le transazioni di un dato tipo su tutti i conti forniti.
     */
    public long countTransactionsByType(List<Account> accounts, TransactionType type) {
        accounts.stream()
                .flatMap(a -> a.getTransactions().stream())
                .peek(debugLog::add)
                .forEach(t -> { /* terminal op */ });

        return debugLog.stream()
                .filter(t -> t.type() == type)
                .count();
    }

    /** Somma i saldi di tutti i conti. */
    public BigDecimal sumBalances(List<Account> accounts) {
        return accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Restituisce il conto con il saldo più alto. */
    public Optional<Account> findAccountWithHighestBalance(List<Account> accounts) {
        return accounts.stream()
                .max(Comparator.comparing(Account::getBalance));
    }

    /** Top N depositi per importo, decrescente. */
    public List<Transaction> getTopDeposits(List<Account> accounts, int n) {
        return accounts.stream()
                .flatMap(a -> a.getTransactions().stream())
                .filter(t -> t.type() == TransactionType.DEPOSIT)
                .sorted(Comparator.comparing(Transaction::amount).reversed())
                .limit(n)
                .toList();
    }

    /** Raggruppa il conteggio delle transazioni per tipo. */
    public Map<TransactionType, Long> groupTransactionsByType(List<Account> accounts) {
        return accounts.stream()
                .flatMap(a -> a.getTransactions().stream())
                .collect(Collectors.groupingBy(Transaction::type, Collectors.counting()));
    }

    /** Saldo medio dei conti. */
    public Optional<BigDecimal> averageBalance(List<Account> accounts) {
        return accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal::add)
                .map(sum -> sum.divide(
                        BigDecimal.valueOf(accounts.size()),
                        2, java.math.RoundingMode.HALF_UP));
    }
}
