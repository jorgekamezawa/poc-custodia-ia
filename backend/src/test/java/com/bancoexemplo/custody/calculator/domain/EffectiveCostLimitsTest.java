package com.bancoexemplo.custody.calculator.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bancoexemplo.custody.calculator.api.AmortizationSystem;
import com.bancoexemplo.custody.calculator.api.EffectiveCostRequest;
import com.bancoexemplo.custody.calculator.api.InvalidScheduleRequestException;
import com.bancoexemplo.custody.calculator.api.ScheduleRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EffectiveCostLimitsTest {

    private static final String RELEASE_MESSAGE = "Data de liberação deve ser anterior ao primeiro vencimento";
    private static final String COSTS_MESSAGE =
            "Custos antecipados devem ficar entre R$ 0,00 e menos que o valor financiado";

    private static EffectiveCostRequest request(LocalDate release, String upfrontCosts) {
        ScheduleRequest schedule = new ScheduleRequest(
                AmortizationSystem.PRICE,
                new BigDecimal("10000.00"),
                new BigDecimal("2"),
                12,
                LocalDate.of(2027, 2, 10));
        return new EffectiveCostRequest(schedule, release, new BigDecimal(upfrontCosts));
    }

    @Test
    @DisplayName("CAL-06 0003/CT-6: liberação no dia do primeiro vencimento é recusada")
    void releaseOnFirstDueDate() {
        assertThatThrownBy(() -> EffectiveCostLimits.validate(request(LocalDate.of(2027, 2, 10), "0")))
                .isInstanceOf(InvalidScheduleRequestException.class)
                .hasMessage(RELEASE_MESSAGE);
    }

    @Test
    @DisplayName("CAL-06 0003/CT-7: liberação depois do primeiro vencimento é recusada")
    void releaseAfterFirstDueDate() {
        assertThatThrownBy(() -> EffectiveCostLimits.validate(request(LocalDate.of(2027, 2, 11), "0")))
                .isInstanceOf(InvalidScheduleRequestException.class)
                .hasMessage(RELEASE_MESSAGE);
    }

    @Test
    @DisplayName("CAL-06 0003/CT-8: custos antecipados negativos são recusados")
    void negativeCosts() {
        assertThatThrownBy(() -> EffectiveCostLimits.validate(request(LocalDate.of(2027, 1, 10), "-0.01")))
                .isInstanceOf(InvalidScheduleRequestException.class)
                .hasMessage(COSTS_MESSAGE);
    }

    @Test
    @DisplayName("CAL-06 0003/CT-9: custos antecipados iguais ao valor financiado são recusados")
    void costsEqualToPrincipal() {
        assertThatThrownBy(() -> EffectiveCostLimits.validate(request(LocalDate.of(2027, 1, 10), "10000.00")))
                .isInstanceOf(InvalidScheduleRequestException.class)
                .hasMessage(COSTS_MESSAGE);
    }

    @Test
    @DisplayName("CAL-06 0003/CT-10: custos antecipados logo abaixo do valor financiado são aceitos")
    void costsJustBelowPrincipal() {
        assertThatCode(() -> EffectiveCostLimits.validate(request(LocalDate.of(2027, 2, 9), "9999.99")))
                .doesNotThrowAnyException();
    }
}
