package com.lipari.bank.pattern;

import com.lipari.bank.model.Account;

import java.math.BigDecimal;

/**
 * Comando di prelievo. Immutabile (record Java 21).
 */
public record WithdrawCommand(BigDecimal amount) implements BankCommand {

    public WithdrawCommand {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Importo prelievo deve essere positivo");
        }
    }

    @Override
    public void execute(Account account) {
        account.withdraw(amount);
    }

    @Override
    public String describe() {
        return String.format("Prelievo %.2f€", amount);
    }
}
