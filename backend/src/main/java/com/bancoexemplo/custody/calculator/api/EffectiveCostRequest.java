package com.bancoexemplo.custody.calculator.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * What the total effective cost is calculated from.
 *
 * @param upfrontCosts fees, taxes and insurance paid before the first installment, deducted from the released amount
 */
public record EffectiveCostRequest(ScheduleRequest schedule, LocalDate releaseDate, BigDecimal upfrontCosts) {

    public EffectiveCostRequest {
        Objects.requireNonNull(schedule, "schedule");
        Objects.requireNonNull(releaseDate, "releaseDate");
        Objects.requireNonNull(upfrontCosts, "upfrontCosts");
    }
}
