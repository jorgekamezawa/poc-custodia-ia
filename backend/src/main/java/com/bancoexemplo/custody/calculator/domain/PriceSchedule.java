package com.bancoexemplo.custody.calculator.domain;

import com.bancoexemplo.custody.calculator.api.Installment;
import com.bancoexemplo.custody.calculator.api.InstallmentSchedule;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Price schedule: equal installments, interest on the previous balance, rounding residue in the last one. */
public final class PriceSchedule {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private PriceSchedule() {}

    public static InstallmentSchedule calculate(
            BigDecimal principal, BigDecimal monthlyRatePercent, int termMonths, LocalDate firstDueDate) {
        BigDecimal rate = monthlyRatePercent.divide(HUNDRED, MathContext.DECIMAL128);
        BigDecimal fixedAmount = fixedAmount(principal, rate, termMonths);
        List<Installment> installments = new ArrayList<>(termMonths);
        BigDecimal balance = principal;
        for (int number = 1; number <= termMonths; number++) {
            BigDecimal interest = Money.of(balance.multiply(rate));
            BigDecimal amortization = number == termMonths ? balance : fixedAmount.subtract(interest);
            balance = balance.subtract(amortization);
            installments.add(new Installment(
                    number,
                    DueDates.of(firstDueDate, number),
                    interest.add(amortization),
                    interest,
                    amortization,
                    Money.of(balance)));
        }
        return new InstallmentSchedule(installments);
    }

    private static BigDecimal fixedAmount(BigDecimal principal, BigDecimal rate, int termMonths) {
        if (rate.signum() == 0) {
            return principal.divide(BigDecimal.valueOf(termMonths), Money.CENTS, RoundingMode.HALF_EVEN);
        }
        BigDecimal discount =
                BigDecimal.ONE.subtract(BigDecimal.ONE.add(rate).pow(-termMonths, MathContext.DECIMAL128));
        return Money.of(principal.multiply(rate).divide(discount, MathContext.DECIMAL128));
    }
}
