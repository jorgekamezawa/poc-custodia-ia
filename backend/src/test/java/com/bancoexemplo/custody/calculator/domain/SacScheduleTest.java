package com.bancoexemplo.custody.calculator.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.bancoexemplo.custody.calculator.api.Installment;
import com.bancoexemplo.custody.calculator.api.InstallmentSchedule;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SacScheduleTest {

    private static final LocalDate FIRST_DUE_DATE = LocalDate.of(2027, 1, 10);

    private static List<Installment> tenThousandAtTwoPercentFor12Months() {
        return SacSchedule.calculate(new BigDecimal("10000.00"), new BigDecimal("2"), 12, FIRST_DUE_DATE)
                .installments();
    }

    @Test
    @DisplayName("CAL-04 0002/CT-1: amortização constante com a diferença de arredondamento na última")
    void constantAmortizationWithResidueInTheLast() {
        List<Installment> installments = tenThousandAtTwoPercentFor12Months();

        assertThat(installments).hasSize(12);
        assertThat(installments.subList(0, 11))
                .allSatisfy(
                        installment -> assertThat(installment.amortization()).isEqualByComparingTo("833.33"));
        assertThat(installments.get(11).amortization()).isEqualByComparingTo("833.37");
        assertThat(installments.get(11).balanceAfter()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("CAL-04 0002/CT-2: juros sobre o saldo anterior somados à amortização")
    void interestOnPreviousBalance() {
        List<Installment> installments = tenThousandAtTwoPercentFor12Months();

        assertThat(installments.get(0).amount()).isEqualByComparingTo("1033.33");
        assertThat(installments.get(0).interest()).isEqualByComparingTo("200.00");
        assertThat(installments.get(0).balanceAfter()).isEqualByComparingTo("9166.67");
        assertThat(installments.get(1).amount()).isEqualByComparingTo("1016.66");
        assertThat(installments.get(1).interest()).isEqualByComparingTo("183.33");
        assertThat(installments.get(11).amount()).isEqualByComparingTo("850.04");
        assertThat(installments.get(11).interest()).isEqualByComparingTo("16.67");
        assertThat(installments.get(11).number()).isEqualTo(12);
    }

    @Test
    @DisplayName("CAL-04 0002/CT-3: parcela decrescente, juros mais amortização e amortizações somando o valor")
    void decreasingInstallments() {
        List<Installment> installments = tenThousandAtTwoPercentFor12Months();

        assertThat(installments)
                .allSatisfy(installment -> assertThat(installment.interest().add(installment.amortization()))
                        .isEqualByComparingTo(installment.amount()));
        for (int i = 1; i < installments.size(); i++) {
            assertThat(installments.get(i).amount())
                    .isLessThan(installments.get(i - 1).amount());
        }
        BigDecimal amortized =
                installments.stream().map(Installment::amortization).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(amortized).isEqualByComparingTo("10000.00");
    }

    @Test
    @DisplayName("CAL-04 0002/CT-4: taxa zero sem juros e resíduo na última parcela")
    void zeroRate() {
        InstallmentSchedule schedule =
                SacSchedule.calculate(new BigDecimal("10000.00"), BigDecimal.ZERO, 3, FIRST_DUE_DATE);

        assertThat(schedule.installments())
                .extracting(Installment::amount)
                .usingElementComparator(BigDecimal::compareTo)
                .containsExactly(new BigDecimal("3333.33"), new BigDecimal("3333.33"), new BigDecimal("3333.34"));
        assertThat(schedule.installments())
                .allSatisfy(installment -> assertThat(installment.interest()).isEqualByComparingTo("0.00"));
    }

    @Test
    @DisplayName("CAL-02 0002/CT-5: vencimentos do SAC seguem o último dia do mês curto")
    void dueDates() {
        InstallmentSchedule schedule =
                SacSchedule.calculate(new BigDecimal("10000.00"), new BigDecimal("2"), 3, LocalDate.of(2027, 1, 31));

        assertThat(schedule.installments())
                .extracting(Installment::dueDate)
                .containsExactly(LocalDate.of(2027, 1, 31), LocalDate.of(2027, 2, 28), LocalDate.of(2027, 3, 31));
    }
}
