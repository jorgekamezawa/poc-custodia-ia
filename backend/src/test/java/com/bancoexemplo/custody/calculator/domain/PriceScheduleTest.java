package com.bancoexemplo.custody.calculator.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.bancoexemplo.custody.calculator.api.Installment;
import com.bancoexemplo.custody.calculator.api.InstallmentSchedule;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PriceScheduleTest {

    private static final LocalDate FIRST_DUE_DATE = LocalDate.of(2027, 1, 10);

    private static InstallmentSchedule tenThousandAtTwoPercentFor12Months() {
        return PriceSchedule.calculate(new BigDecimal("10000.00"), new BigDecimal("2"), 12, FIRST_DUE_DATE);
    }

    private static List<BigDecimal> amounts(InstallmentSchedule schedule) {
        return schedule.installments().stream().map(Installment::amount).toList();
    }

    @Test
    @DisplayName("CAL-01 0001/CT-1: parcelas fixas com a diferença de arredondamento na última")
    void fixedInstallmentsWithRoundingDifferenceInTheLast() {
        InstallmentSchedule schedule = tenThousandAtTwoPercentFor12Months();

        assertThat(schedule.installments()).hasSize(12);
        assertThat(amounts(schedule).subList(0, 11))
                .allSatisfy(amount -> assertThat(amount).isEqualByComparingTo("945.60"));
        assertThat(schedule.installments().get(11).amount()).isEqualByComparingTo("945.55");
        assertThat(schedule.installments().get(11).balanceAfter()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("CAL-01 0001/CT-2: juros sobre o saldo anterior e amortização pela diferença")
    void interestOnPreviousBalanceAndAmortizationByDifference() {
        List<Installment> installments = tenThousandAtTwoPercentFor12Months().installments();

        assertThat(installments.get(0))
                .extracting(Installment::number, Installment::dueDate)
                .containsExactly(1, FIRST_DUE_DATE);
        assertThat(installments.get(0).interest()).isEqualByComparingTo("200.00");
        assertThat(installments.get(0).amortization()).isEqualByComparingTo("745.60");
        assertThat(installments.get(0).balanceAfter()).isEqualByComparingTo("9254.40");
        assertThat(installments.get(1).interest()).isEqualByComparingTo("185.09");
        assertThat(installments.get(1).amortization()).isEqualByComparingTo("760.51");
        assertThat(installments.get(1).balanceAfter()).isEqualByComparingTo("8493.89");
    }

    @Test
    @DisplayName("CAL-01 0001/CT-3: juros mais amortização é a parcela e as amortizações somam o valor financiado")
    void interestPlusAmortizationIsTheInstallment() {
        List<Installment> installments = tenThousandAtTwoPercentFor12Months().installments();

        assertThat(installments)
                .allSatisfy(installment -> assertThat(installment.interest().add(installment.amortization()))
                        .isEqualByComparingTo(installment.amount()));
        BigDecimal amortized =
                installments.stream().map(Installment::amortization).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(amortized).isEqualByComparingTo("10000.00");
        assertThat(installments)
                .allSatisfy(
                        installment -> assertThat(installment.amount().scale()).isEqualTo(2));
    }

    @Test
    @DisplayName("CAL-01 0001/CT-4: prazo de um mês tem parcela única com todos os juros")
    void singleInstallment() {
        Installment only = PriceSchedule.calculate(new BigDecimal("1000.00"), new BigDecimal("1.5"), 1, FIRST_DUE_DATE)
                .installments()
                .get(0);

        assertThat(only.amount()).isEqualByComparingTo("1015.00");
        assertThat(only.interest()).isEqualByComparingTo("15.00");
        assertThat(only.amortization()).isEqualByComparingTo("1000.00");
        assertThat(only.balanceAfter()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("CAL-01 0001/CT-5: taxa zero divide o valor e deixa o resíduo na última parcela")
    void zeroRate() {
        InstallmentSchedule schedule =
                PriceSchedule.calculate(new BigDecimal("10000.00"), BigDecimal.ZERO, 3, FIRST_DUE_DATE);

        assertThat(amounts(schedule))
                .usingElementComparator(BigDecimal::compareTo)
                .containsExactly(new BigDecimal("3333.33"), new BigDecimal("3333.33"), new BigDecimal("3333.34"));
        assertThat(schedule.installments())
                .allSatisfy(installment -> assertThat(installment.interest()).isEqualByComparingTo("0.00"));
    }

    @Test
    @DisplayName("CAL-02 0001/CT-6: vencimento no dia 31 cai no último dia de fevereiro e volta ao 31")
    void dueDateFallsOnLastDayOfShortMonth() {
        InstallmentSchedule schedule =
                PriceSchedule.calculate(new BigDecimal("10000.00"), new BigDecimal("2"), 3, LocalDate.of(2027, 1, 31));

        assertThat(schedule.installments())
                .extracting(Installment::dueDate)
                .containsExactly(LocalDate.of(2027, 1, 31), LocalDate.of(2027, 2, 28), LocalDate.of(2027, 3, 31));
    }

    @Test
    @DisplayName("CAL-02 0001/CT-7: em ano bissexto o vencimento cai em 29 de fevereiro")
    void dueDateInLeapYear() {
        InstallmentSchedule schedule =
                PriceSchedule.calculate(new BigDecimal("10000.00"), new BigDecimal("2"), 2, LocalDate.of(2028, 1, 31));

        assertThat(schedule.installments())
                .extracting(Installment::dueDate)
                .containsExactly(LocalDate.of(2028, 1, 31), LocalDate.of(2028, 2, 29));
    }
}
