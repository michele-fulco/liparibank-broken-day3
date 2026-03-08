package com.lipari.bank.model;

import java.math.BigDecimal;

public final class CheckingAccount extends Account {

    private BigDecimal overdraftLimit;

    public CheckingAccount(String iban, BigDecimal initialBalance,
                           Customer owner, BigDecimal overdraftLimit) {
        super(iban, initialBalance, owner);
        if (overdraftLimit == null || overdraftLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Limite scoperto non può essere negativo");
        }
        this.overdraftLimit = overdraftLimit;
    }

    public BigDecimal getOverdraftLimit()           { return overdraftLimit; }
    public void setOverdraftLimit(BigDecimal limit) { this.overdraftLimit = limit; }

    @Override
    protected BigDecimal getMinBalance() {
        return overdraftLimit.negate();
    }

    @Override
    public String toString() {
        return super.toString()
                + String.format(" | [CORRENTE] scoperto: %.2f€", overdraftLimit);
    }
}
