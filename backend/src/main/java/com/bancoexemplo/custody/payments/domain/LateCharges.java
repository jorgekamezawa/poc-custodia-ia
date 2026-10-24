package com.bancoexemplo.custody.payments.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * What an installment costs on a payment date: late fee and default interest when paid after the due date.
 *
 * @param total installment amount plus the late fee and the default interest
 */
public record LateCharges(int daysLate, BigDecimal lateFee, BigDecimal defaultInterest, BigDecimal total) {

    private static final BigDecimal LATE_FEE_RATE = new BigDecimal("0.02");
    private static final BigDecimal MONTHLY_DEFAULT_RATE = new BigDecimal("0.01");
    private static final BigDecimal DAYS_IN_MONTH = new BigDecimal("30");
    private static final int CENTS = 2;

    public static LateCharges of(BigDecimal installmentAmount, LocalDate dueDate, LocalDate paidOn) {
        long days = Math.max(0, ChronoUnit.DAYS.between(dueDate, paidOn));
        if (days == 0) {
            BigDecimal zero = BigDecimal.ZERO.setScale(CENTS);
            return new LateCharges(0, zero, zero, installmentAmount);
        }
        BigDecimal lateFee = installmentAmount.multiply(LATE_FEE_RATE).setScale(CENTS, RoundingMode.HALF_EVEN);
        BigDecimal defaultInterest = installmentAmount
                .multiply(MONTHLY_DEFAULT_RATE)
                .multiply(BigDecimal.valueOf(days))
                .divide(DAYS_IN_MONTH, CENTS, RoundingMode.HALF_EVEN);
        return new LateCharges(
                Math.toIntExact(days),
                lateFee,
                defaultInterest,
                installmentAmount.add(lateFee).add(defaultInterest));
    }

    public boolean late() {
        return daysLate > 0;
    }
}
