package com.lipari.bank.pattern;

import com.lipari.bank.model.Account;
import com.lipari.bank.model.CheckingAccount;
import com.lipari.bank.model.Customer;
import com.lipari.bank.model.SavingsAccount;

import java.math.BigDecimal;

/**
 * Factory Method per la creazione di conti bancari.
 * Usa switch expression (Java 21) per selezionare il tipo.
 */
public class AccountFactory {

    public enum AccountType { CHECKING, SAVINGS }

    /**
     * Crea un conto del tipo specificato con i parametri forniti.
     *
     * @param type           tipo di conto
     * @param iban           IBAN del conto
     * @param initialBalance saldo iniziale
     * @param owner          titolare
     * @return nuova istanza del conto appropriato
     */
    public static Account create(AccountType type,
                                 String iban,
                                 BigDecimal initialBalance,
                                 Customer owner) {
        return switch (type) {
            case CHECKING -> new CheckingAccount(
                    iban, initialBalance, owner, BigDecimal.ZERO);
            case SAVINGS  -> new SavingsAccount(
                    iban, initialBalance, owner, 2.5);
        };
    }

    /**
     * Overload con overdraftLimit esplicito (solo per CHECKING).
     */
    public static Account createChecking(String iban,
                                         BigDecimal initialBalance,
                                         Customer owner,
                                         BigDecimal overdraftLimit) {
        return new CheckingAccount(iban, initialBalance, owner, overdraftLimit);
    }

    private AccountFactory() { /* utility class */ }
}
