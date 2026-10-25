package com.bancoexemplo.custody.contracts.adapter.out.calculator;

import com.bancoexemplo.custody.calculator.api.EffectiveCostCalculator;
import com.bancoexemplo.custody.calculator.api.EffectiveCostRequest;
import com.bancoexemplo.custody.calculator.api.InstallmentSchedule;
import com.bancoexemplo.custody.calculator.api.InvalidScheduleRequestException;
import com.bancoexemplo.custody.calculator.api.ScheduleCalculator;
import com.bancoexemplo.custody.calculator.api.ScheduleRequest;
import com.bancoexemplo.custody.contracts.application.port.FinancingCalculator;
import com.bancoexemplo.custody.contracts.domain.contract.CalculatedInstallment;
import com.bancoexemplo.custody.contracts.domain.contract.Financing;
import com.bancoexemplo.custody.contracts.domain.contract.FinancingTerms;
import com.bancoexemplo.custody.contracts.domain.contract.exception.InvalidContractException;
import org.springframework.stereotype.Component;

/** Translates the contract's financing terms to the calculator module and its refusal back to the contract's. */
@Component
class CalculatorFinancingAdapter implements FinancingCalculator {

    private final ScheduleCalculator scheduleCalculator;
    private final EffectiveCostCalculator effectiveCostCalculator;

    CalculatorFinancingAdapter(ScheduleCalculator scheduleCalculator, EffectiveCostCalculator effectiveCostCalculator) {
        this.scheduleCalculator = scheduleCalculator;
        this.effectiveCostCalculator = effectiveCostCalculator;
    }

    @Override
    public Financing calculate(FinancingTerms terms) {
        ScheduleRequest scheduleRequest = new ScheduleRequest(
                com.bancoexemplo.custody.calculator.api.AmortizationSystem.valueOf(
                        terms.amortizationSystem().name()),
                terms.principal(),
                terms.monthlyRatePercent(),
                terms.termMonths(),
                terms.firstDueDate());
        try {
            InstallmentSchedule schedule = scheduleCalculator.calculate(scheduleRequest);
            var cet = effectiveCostCalculator.calculate(
                    new EffectiveCostRequest(scheduleRequest, terms.releaseDate(), terms.upfrontCosts()));
            return new Financing(
                    schedule.installments().stream()
                            .map(installment -> new CalculatedInstallment(
                                    installment.number(),
                                    installment.dueDate(),
                                    installment.amount(),
                                    installment.interest(),
                                    installment.amortization(),
                                    installment.balanceAfter()))
                            .toList(),
                    cet.annualRatePercent());
        } catch (InvalidScheduleRequestException e) {
            throw new InvalidContractException(e.getMessage());
        }
    }
}
