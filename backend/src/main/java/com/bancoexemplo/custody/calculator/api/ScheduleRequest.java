package com.bancoexemplo.custody.calculator.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * What a schedule is calculated from.
 *
 * @param monthlyRatePercent effective monthly rate as a percentage, so {@code 2} means 2% a month
 */
public record ScheduleRequest(
        AmortizationSystem system,
        BigDecimal principal,
        BigDecimal monthlyRatePercent,
        int termMonths,
        LocalDate firstDueDate) {

    public ScheduleRequest {
        Objects.requireNonNull(system, "system");
        Objects.requireNonNull(principal, "principal");
        Objects.requireNonNull(monthlyRatePercent, "monthlyRatePercent");
        Objects.requireNonNull(firstDueDate, "firstDueDate");
    }
}
