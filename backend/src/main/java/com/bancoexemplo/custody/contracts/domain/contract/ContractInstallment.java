package com.bancoexemplo.custody.contracts.domain.contract;

import java.math.BigDecimal;
import java.time.LocalDate;

/** One installment of the schedule stored with the contract; its number identifies it within the contract. */
public final class ContractInstallment {

    private final int number;
    private final LocalDate dueDate;
    private final BigDecimal amount;
    private final BigDecimal interest;
    private final BigDecimal amortization;
    private final BigDecimal balanceAfter;
    private LocalDate paidOn;

    private ContractInstallment(
            int number,
            LocalDate dueDate,
            BigDecimal amount,
            BigDecimal interest,
            BigDecimal amortization,
            BigDecimal balanceAfter,
            LocalDate paidOn) {
        this.number = number;
        this.dueDate = dueDate;
        this.amount = amount;
        this.interest = interest;
        this.amortization = amortization;
        this.balanceAfter = balanceAfter;
        this.paidOn = paidOn;
    }

    /** An installment of a contract being registered, not paid yet. */
    static ContractInstallment create(CalculatedInstallment calculated) {
        return new ContractInstallment(
                calculated.number(),
                calculated.dueDate(),
                calculated.amount(),
                calculated.interest(),
                calculated.amortization(),
                calculated.balanceAfter(),
                null);
    }

    /** An installment as stored; nothing is checked again. */
    public static ContractInstallment reconstruct(
            int number,
            LocalDate dueDate,
            BigDecimal amount,
            BigDecimal interest,
            BigDecimal amortization,
            BigDecimal balanceAfter,
            LocalDate paidOn) {
        return new ContractInstallment(number, dueDate, amount, interest, amortization, balanceAfter, paidOn);
    }

    void pay(LocalDate date) {
        this.paidOn = date;
    }

    public int getNumber() {
        return number;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getInterest() {
        return interest;
    }

    public BigDecimal getAmortization() {
        return amortization;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    /** When it was paid; null while unpaid. */
    public LocalDate getPaidOn() {
        return paidOn;
    }

    public boolean isPaid() {
        return paidOn != null;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ContractInstallment installment && installment.number == number;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(number);
    }
}
