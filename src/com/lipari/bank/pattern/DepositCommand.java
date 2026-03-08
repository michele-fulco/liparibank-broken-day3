package com.lipari.bank.pattern;

import com.lipari.bank.model.Account;

import java.math.BigDecimal;

/**
 * Comando di deposito. Immutabile (record Java 21).
 */
public record DepositCommand(BigDecimal amount, String memo) implements BankCommand {

    public DepositCommand {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Importo deposito deve essere positivo");
        }
    }

    @Override
    public void execute(Account account) {
        account.deposit(amount);
    }

    @Override
    public String describe() {
        return String.format("Deposito %.2f€ [%s]", amount, memo);
    }
}
