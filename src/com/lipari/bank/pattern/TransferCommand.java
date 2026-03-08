package com.lipari.bank.pattern;

import com.lipari.bank.model.Account;

import java.math.BigDecimal;

/**
 * Comando di bonifico tra due conti. Immutabile (record Java 21).
 *
 * <p>{@code execute(source)} preleva dal conto sorgente e deposita sul target.
 */
public record TransferCommand(BigDecimal amount, Account target) implements BankCommand {

    public TransferCommand {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Importo bonifico deve essere positivo");
        }
        if (target == null) {
            throw new IllegalArgumentException("Conto destinatario obbligatorio");
        }
    }

    @Override
    public void execute(Account source) {
        if (source.getIban().equals(target.getIban())) {
            throw new IllegalArgumentException("Sorgente e destinatario non possono coincidere");
        }
        source.withdraw(amount);
        target.deposit(amount);
    }

    @Override
    public String describe() {
        return String.format("Bonifico %.2f€ → %s", amount, target.getIban());
    }
}
