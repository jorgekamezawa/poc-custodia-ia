package com.bancoexemplo.custody.calculator.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bancoexemplo.custody.calculator.api.AmortizationSystem;
import com.bancoexemplo.custody.calculator.api.EffectiveCostRequest;
import com.bancoexemplo.custody.calculator.api.InvalidScheduleRequestException;
import com.bancoexemplo.custody.calculator.api.ScheduleRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EffectiveCostCalculatorServiceTest {

    private final EffectiveCostCalculatorService service =
            new EffectiveCostCalculatorService(new ScheduleCalculatorService());

    private static EffectiveCostRequest request(int termMonths, String upfrontCosts) {
        return new EffectiveCostRequest(
                new ScheduleRequest(
                        AmortizationSystem.PRICE,
                        new BigDecimal("10000.00"),
                        new BigDecimal("2"),
                        termMonths,
                        LocalDate.of(2027, 2, 10)),
                LocalDate.of(2027, 1, 10),
                new BigDecimal(upfrontCosts));
    }

    @Test
    void validRequestReturnsTheRate() {
        assertThat(service.calculate(request(12, "0")).annualRatePercent()).isEqualByComparingTo("26.95");
    }

    @Test
    void upfrontCostsAreDeductedFromTheReleasedAmount() {
        assertThat(service.calculate(request(12, "300.00")).annualRatePercent()).isEqualByComparingTo("34.65");
    }

    @Test
    void invalidCostsAreRefused() {
        assertThatThrownBy(() -> service.calculate(request(12, "-1")))
                .isInstanceOf(InvalidScheduleRequestException.class);
    }

    @Test
    @DisplayName("CAL-03 0003/CT-11: cronograma fora dos limites recusa o CET com a mesma mensagem")
    void scheduleOutOfLimits() {
        assertThatThrownBy(() -> service.calculate(request(73, "0")))
                .isInstanceOf(InvalidScheduleRequestException.class)
                .hasMessage("Prazo deve estar entre 1 e 72 meses");
    }
}
