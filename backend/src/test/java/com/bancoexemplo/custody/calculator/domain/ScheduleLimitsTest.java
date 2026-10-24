package com.bancoexemplo.custody.calculator.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bancoexemplo.custody.calculator.api.AmortizationSystem;
import com.bancoexemplo.custody.calculator.api.InvalidScheduleRequestException;
import com.bancoexemplo.custody.calculator.api.ScheduleRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ScheduleLimitsTest {

    private static ScheduleRequest request(String principal, String rate, int term) {
        return new ScheduleRequest(
                AmortizationSystem.PRICE,
                new BigDecimal(principal),
                new BigDecimal(rate),
                term,
                LocalDate.of(2027, 1, 10));
    }

    @ParameterizedTest(name = "{displayName} [{arguments}]")
    @ValueSource(strings = {"999.99", "1000000.01"})
    @DisplayName("CAL-03 0001/CT-8: valor financiado fora dos limites é recusado")
    void principalOutOfLimits(String principal) {
        assertThatThrownBy(() -> ScheduleLimits.validate(request(principal, "2", 12)))
                .isInstanceOf(InvalidScheduleRequestException.class)
                .hasMessage("Valor financiado deve estar entre R$ 1.000,00 e R$ 1.000.000,00");
    }

    @ParameterizedTest(name = "{displayName} [{arguments}]")
    @ValueSource(strings = {"1000.00", "1000000.00"})
    @DisplayName("CAL-03 0001/CT-9: valor financiado nos limites é aceito")
    void principalAtLimits(String principal) {
        assertThatCode(() -> ScheduleLimits.validate(request(principal, "2", 12)))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest(name = "{displayName} [{arguments}]")
    @ValueSource(strings = {"-0.01", "10.01", "1.23456"})
    @DisplayName("CAL-03 0001/CT-10: taxa fora dos limites ou com mais de 4 casas é recusada")
    void rateOutOfLimits(String rate) {
        assertThatThrownBy(() -> ScheduleLimits.validate(request("10000.00", rate, 12)))
                .isInstanceOf(InvalidScheduleRequestException.class)
                .hasMessage("Taxa deve estar entre 0% e 10% ao mês, com até 4 casas decimais");
    }

    @ParameterizedTest(name = "{displayName} [{arguments}]")
    @ValueSource(strings = {"0", "10", "1.2345", "2.50000"})
    @DisplayName("CAL-03 0001/CT-10: taxa nos limites e com até 4 casas é aceita")
    void rateAtLimits(String rate) {
        assertThatCode(() -> ScheduleLimits.validate(request("10000.00", rate, 12)))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest(name = "{displayName} [{arguments}]")
    @ValueSource(ints = {0, 73})
    @DisplayName("CAL-03 0001/CT-11: prazo fora dos limites é recusado")
    void termOutOfLimits(int term) {
        assertThatThrownBy(() -> ScheduleLimits.validate(request("10000.00", "2", term)))
                .isInstanceOf(InvalidScheduleRequestException.class)
                .hasMessage("Prazo deve estar entre 1 e 72 meses");
    }

    @ParameterizedTest(name = "{displayName} [{arguments}]")
    @ValueSource(ints = {1, 72})
    @DisplayName("CAL-03 0001/CT-11: prazo nos limites é aceito")
    void termAtLimits(int term) {
        assertThatCode(() -> ScheduleLimits.validate(request("10000.00", "2", term)))
                .doesNotThrowAnyException();
    }
}
