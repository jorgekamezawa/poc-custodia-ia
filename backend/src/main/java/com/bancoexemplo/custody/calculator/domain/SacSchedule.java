package com.bancoexemplo.custody.calculator.domain;

import com.bancoexemplo.custody.calculator.api.Installment;
import com.bancoexemplo.custody.calculator.api.InstallmentSchedule;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** SAC schedule: equal amortization, interest on the previous balance, rounding residue in the last one. */
public final class SacSchedule {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private SacSchedule() {}

    public static InstallmentSchedule calculate(
            BigDecimal principal, BigDecimal monthlyRatePercent, int termMonths, LocalDate firstDueDate) {
        BigDecimal rate = monthlyRatePercent.divide(HUNDRED, MathContext.DECIMAL128);
        BigDecimal constantAmortization =
                principal.divide(BigDecimal.valueOf(termMonths), Money.CENTS, RoundingMode.HALF_EVEN);
        List<Installment> installments = new ArrayList<>(termMonths);
        BigDecimal balance = principal;
        for (int number = 1; number <= termMonths; number++) {
            BigDecimal interest = Money.of(balance.multiply(rate));
            BigDecimal amortization = number == termMonths ? balance : constantAmortization;
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
}
