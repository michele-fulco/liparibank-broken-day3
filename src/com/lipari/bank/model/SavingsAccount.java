package com.lipari.bank.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class SavingsAccount extends Account {

    private double interestRate;   // tasso % annuo, es: 3.0 → 3%

    public SavingsAccount(String iban, BigDecimal initialBalance,
                          Customer owner, double interestRate) {
        super(iban, initialBalance, owner);
        this.interestRate = interestRate;
    }

    public double getInterestRate()          { return interestRate; }
    public void setInterestRate(double rate) { this.interestRate = rate; }

    public void applyInterest() {
        BigDecimal interest = getBalance()
                .multiply(BigDecimal.valueOf(interestRate / 100.0))
                .setScale(2, RoundingMode.HALF_UP);
        if (interest.compareTo(BigDecimal.ZERO) > 0) {
            deposit(interest);
        }
    }

    @Override
    public String toString() {
        return super.toString()
                + String.format(" | [RISPARMIO] tasso: %.2f%%", interestRate);
    }
}
