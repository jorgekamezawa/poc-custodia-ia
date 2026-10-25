package com.bancoexemplo.custody.contracts.adapter.out.calculator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bancoexemplo.custody.TestcontainersConfiguration;
import com.bancoexemplo.custody.contracts.application.port.FinancingCalculator;
import com.bancoexemplo.custody.contracts.domain.contract.AmortizationSystem;
import com.bancoexemplo.custody.contracts.domain.contract.CalculatedInstallment;
import com.bancoexemplo.custody.contracts.domain.contract.Financing;
import com.bancoexemplo.custody.contracts.domain.contract.FinancingTerms;
import com.bancoexemplo.custody.contracts.domain.contract.InvalidContractException;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class CalculatorFinancingAdapterTest {

    @Autowired
    private FinancingCalculator calculator;

    private static FinancingTerms request(AmortizationSystem system, int termMonths) {
        return new FinancingTerms(
                system,
                new BigDecimal("10000.00"),
                new BigDecimal("2"),
                termMonths,
                LocalDate.of(2027, 1, 10),
                LocalDate.of(2027, 2, 10),
                BigDecimal.ZERO);
    }

    @Test
    void priceTermsComeBackAsTheContractScheduleAndCet() {
        Financing financing = calculator.calculate(request(AmortizationSystem.PRICE, 12));

        assertThat(financing.annualCetPercent()).isEqualByComparingTo("26.95");
        assertThat(financing.installments()).hasSize(12);
        CalculatedInstallment first = financing.installments().get(0);
        assertThat(first.number()).isEqualTo(1);
        assertThat(first.dueDate()).isEqualTo(LocalDate.of(2027, 2, 10));
        assertThat(first.amount()).isEqualByComparingTo("945.60");
        assertThat(first.interest()).isEqualByComparingTo("200.00");
        assertThat(first.amortization()).isEqualByComparingTo("745.60");
        assertThat(first.balanceAfter()).isEqualByComparingTo("9254.40");
        assertThat(financing.installments().get(11).amount()).isEqualByComparingTo("945.55");
    }

    @Test
    void sacReachesTheCalculatorAsSac() {
        Financing financing = calculator.calculate(request(AmortizationSystem.SAC, 12));

        assertThat(financing.installments().get(0).amortization()).isEqualByComparingTo("833.33");
        assertThat(financing.installments().get(0).amount()).isEqualByComparingTo("1033.33");
    }

    @Test
    void calculatorRefusalBecomesTheContractRefusal() {
        assertThatThrownBy(() -> calculator.calculate(request(AmortizationSystem.PRICE, 0)))
                .isInstanceOf(InvalidContractException.class)
                .hasMessage("Prazo deve estar entre 1 e 72 meses");
    }
}
