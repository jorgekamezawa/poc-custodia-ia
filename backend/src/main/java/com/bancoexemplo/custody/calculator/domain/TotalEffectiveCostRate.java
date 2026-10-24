package com.bancoexemplo.custody.calculator.domain;

import com.bancoexemplo.custody.calculator.api.Installment;
import com.bancoexemplo.custody.calculator.api.InstallmentSchedule;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Annual rate that discounts the installments, by calendar days over 365, to the released amount.
 *
 * <p>The rate is searched in {@code double}: the exponent is fractional, which {@code BigDecimal} cannot raise to,
 * and the result is rounded to 2 decimals, far above the error of a {@code double}.
 */
public final class TotalEffectiveCostRate {

    private static final double DAYS_IN_YEAR = 365.0;
    private static final int ITERATIONS = 200;
    private static final int PERCENT_DECIMALS = 2;

    private TotalEffectiveCostRate() {}

    public static BigDecimal annualPercent(BigDecimal released, InstallmentSchedule schedule, LocalDate releaseDate) {
        List<Flow> flows = schedule.installments().stream()
                .map(installment -> Flow.of(installment, releaseDate))
                .toList();
        double target = released.doubleValue();
        double low = 0.0;
        double high = 1.0;
        while (presentValue(flows, high) > target) {
            low = high;
            high *= 2;
        }
        for (int i = 0; i < ITERATIONS; i++) {
            double middle = (low + high) / 2;
            if (presentValue(flows, middle) > target) {
                low = middle;
            } else {
                high = middle;
            }
        }
        return BigDecimal.valueOf(low).movePointRight(2).setScale(PERCENT_DECIMALS, RoundingMode.HALF_EVEN);
    }

    private static double presentValue(List<Flow> flows, double annualRate) {
        double total = 0.0;
        for (Flow flow : flows) {
            total += flow.amount() / Math.pow(1 + annualRate, flow.years());
        }
        return total;
    }

    private record Flow(double amount, double years) {

        static Flow of(Installment installment, LocalDate releaseDate) {
            long days = ChronoUnit.DAYS.between(releaseDate, installment.dueDate());
            return new Flow(installment.amount().doubleValue(), days / DAYS_IN_YEAR);
        }
    }
}
