package com.bancoexemplo.custody.contracts.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One installment of the schedule stored with the contract.
 *
 * @param paidOn when it was paid; null while unpaid
 */
public record ContractInstallment(
        int number,
        LocalDate dueDate,
        BigDecimal amount,
        BigDecimal interest,
        BigDecimal amortization,
        BigDecimal balanceAfter,
        LocalDate paidOn) {

    /** An installment not paid yet. */
    public ContractInstallment(
            int number,
            LocalDate dueDate,
            BigDecimal amount,
            BigDecimal interest,
            BigDecimal amortization,
            BigDecimal balanceAfter) {
        this(number, dueDate, amount, interest, amortization, balanceAfter, null);
    }

    public boolean paid() {
        return paidOn != null;
    }

    public ContractInstallment paidOn(LocalDate date) {
        return new ContractInstallment(number, dueDate, amount, interest, amortization, balanceAfter, date);
    }
}
