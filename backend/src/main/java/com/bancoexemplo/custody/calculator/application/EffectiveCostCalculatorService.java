package com.bancoexemplo.custody.calculator.application;

import com.bancoexemplo.custody.calculator.api.EffectiveCostCalculator;
import com.bancoexemplo.custody.calculator.api.EffectiveCostRequest;
import com.bancoexemplo.custody.calculator.api.InstallmentSchedule;
import com.bancoexemplo.custody.calculator.api.ScheduleCalculator;
import com.bancoexemplo.custody.calculator.api.TotalEffectiveCost;
import com.bancoexemplo.custody.calculator.domain.EffectiveCostLimits;
import com.bancoexemplo.custody.calculator.domain.TotalEffectiveCostRate;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class EffectiveCostCalculatorService implements EffectiveCostCalculator {

    private final ScheduleCalculator scheduleCalculator;

    public EffectiveCostCalculatorService(ScheduleCalculator scheduleCalculator) {
        this.scheduleCalculator = scheduleCalculator;
    }

    @Override
    public TotalEffectiveCost calculate(EffectiveCostRequest request) {
        InstallmentSchedule schedule = scheduleCalculator.calculate(request.schedule());
        EffectiveCostLimits.validate(request);
        BigDecimal released = request.schedule().principal().subtract(request.upfrontCosts());
        return new TotalEffectiveCost(TotalEffectiveCostRate.annualPercent(released, schedule, request.releaseDate()));
    }
}
