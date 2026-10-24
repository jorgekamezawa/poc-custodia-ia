package com.bancoexemplo.custody.calculator.api;

/** Calculates the total effective cost of a contract for other modules. */
public interface EffectiveCostCalculator {

    /**
     * Calculates the total effective cost for the request.
     *
     * @throws InvalidScheduleRequestException when a field is outside the limits
     */
    TotalEffectiveCost calculate(EffectiveCostRequest request);
}
