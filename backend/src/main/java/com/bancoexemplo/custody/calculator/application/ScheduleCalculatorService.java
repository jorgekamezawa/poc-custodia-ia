package com.bancoexemplo.custody.calculator.application;

import com.bancoexemplo.custody.calculator.api.InstallmentSchedule;
import com.bancoexemplo.custody.calculator.api.ScheduleCalculator;
import com.bancoexemplo.custody.calculator.api.ScheduleRequest;
import com.bancoexemplo.custody.calculator.domain.PriceSchedule;
import com.bancoexemplo.custody.calculator.domain.ScheduleLimits;
import org.springframework.stereotype.Service;

@Service
public class ScheduleCalculatorService implements ScheduleCalculator {

    @Override
    public InstallmentSchedule calculate(ScheduleRequest request) {
        ScheduleLimits.validate(request);
        return switch (request.system()) {
            case PRICE ->
                PriceSchedule.calculate(
                        request.principal(),
                        request.monthlyRatePercent(),
                        request.termMonths(),
                        request.firstDueDate());
        };
    }
}
