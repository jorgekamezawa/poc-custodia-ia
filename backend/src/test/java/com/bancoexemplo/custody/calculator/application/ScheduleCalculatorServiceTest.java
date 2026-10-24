package com.bancoexemplo.custody.calculator.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bancoexemplo.custody.calculator.api.AmortizationSystem;
import com.bancoexemplo.custody.calculator.api.InstallmentSchedule;
import com.bancoexemplo.custody.calculator.api.InvalidScheduleRequestException;
import com.bancoexemplo.custody.calculator.api.ScheduleRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ScheduleCalculatorServiceTest {

    private final ScheduleCalculatorService service = new ScheduleCalculatorService();

    @Test
    void validRequestReturnsThePriceSchedule() {
        InstallmentSchedule schedule = service.calculate(new ScheduleRequest(
                AmortizationSystem.PRICE,
                new BigDecimal("10000.00"),
                new BigDecimal("2"),
                12,
                LocalDate.of(2027, 1, 10)));

        assertThat(schedule.installments()).hasSize(12);
        assertThat(schedule.installments().get(0).amount()).isEqualByComparingTo("945.60");
    }

    @Test
    void invalidRequestIsRefusedBeforeCalculating() {
        ScheduleRequest request = new ScheduleRequest(
                AmortizationSystem.PRICE,
                new BigDecimal("10000.00"),
                new BigDecimal("2"),
                0,
                LocalDate.of(2027, 1, 10));

        assertThatThrownBy(() -> service.calculate(request))
                .isInstanceOf(InvalidScheduleRequestException.class)
                .hasMessage("Prazo deve estar entre 1 e 72 meses");
    }

    @Test
    void sacRequestReturnsTheSacSchedule() {
        InstallmentSchedule schedule = service.calculate(new ScheduleRequest(
                AmortizationSystem.SAC,
                new BigDecimal("10000.00"),
                new BigDecimal("2"),
                12,
                LocalDate.of(2027, 1, 10)));

        assertThat(schedule.installments().get(0).amount()).isEqualByComparingTo("1033.33");
    }

    @Test
    @DisplayName("CAL-03 0002/CT-6: pedido SAC fora dos limites é recusado")
    void sacRequestOutOfLimitsIsRefused() {
        ScheduleRequest request = new ScheduleRequest(
                AmortizationSystem.SAC, new BigDecimal("10000.00"), new BigDecimal("2"), 73, LocalDate.of(2027, 1, 10));

        assertThatThrownBy(() -> service.calculate(request))
                .isInstanceOf(InvalidScheduleRequestException.class)
                .hasMessage("Prazo deve estar entre 1 e 72 meses");
    }
}
