package com.lipari.bank.pattern;

import com.lipari.bank.model.Account;

/**
 * Interfaccia sealed per il pattern Command applicato alle operazioni bancarie.
 *
 * Le sole implementazioni ammesse sono {@link DepositCommand},
 * {@link WithdrawCommand} e {@link TransferCommand}.
 */
public sealed interface BankCommand
        permits DepositCommand, WithdrawCommand, TransferCommand {

    /**
     * Esegue il comando sull'account specificato.
     *
     * @param account conto su cui operare (sorgente per i transfer)
     */
    void execute(Account account);

    /** Descrizione leggibile del comando per log e audit. */
    String describe();
}
