package com.bancoexemplo.custody.calculator.api;

/** Calculates installment schedules for other modules. */
public interface ScheduleCalculator {

    /**
     * Calculates the schedule for the request.
     *
     * @throws InvalidScheduleRequestException when a field is outside the limits
     */
    InstallmentSchedule calculate(ScheduleRequest request);
}
