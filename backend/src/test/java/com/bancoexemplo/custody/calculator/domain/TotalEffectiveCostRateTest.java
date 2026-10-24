package com.bancoexemplo.custody.calculator.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.bancoexemplo.custody.calculator.api.InstallmentSchedule;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TotalEffectiveCostRateTest {

    private static final LocalDate RELEASE = LocalDate.of(2027, 1, 10);
    private static final LocalDate FIRST_DUE_DATE = LocalDate.of(2027, 2, 10);
    private static final BigDecimal PRINCIPAL = new BigDecimal("10000.00");

    private static InstallmentSchedule price() {
        return PriceSchedule.calculate(PRINCIPAL, new BigDecimal("2"), 12, FIRST_DUE_DATE);
    }

    @Test
    @DisplayName("CAL-05 0003/CT-1: CET do Price sem custos antecipados")
    void priceWithoutUpfrontCosts() {
        assertThat(TotalEffectiveCostRate.annualPercent(PRINCIPAL, price(), RELEASE))
                .isEqualByComparingTo("26.95");
    }

    @Test
    @DisplayName("CAL-05 0003/CT-2: custos pagos antecipadamente reduzem o valor liberado e elevam o CET")
    void upfrontCostsRaiseTheRate() {
        BigDecimal released = PRINCIPAL.subtract(new BigDecimal("300.00"));

        assertThat(TotalEffectiveCostRate.annualPercent(released, price(), RELEASE))
                .isEqualByComparingTo("34.65");
    }

    @Test
    @DisplayName("CAL-05 0003/CT-3: CET do SAC")
    void sac() {
        InstallmentSchedule sac = SacSchedule.calculate(PRINCIPAL, new BigDecimal("2"), 12, FIRST_DUE_DATE);

        assertThat(TotalEffectiveCostRate.annualPercent(PRINCIPAL, sac, RELEASE))
                .isEqualByComparingTo("26.96");
    }

    @Test
    @DisplayName("CAL-05 0003/CT-4: CET de parcela única")
    void singleInstallment() {
        BigDecimal principal = new BigDecimal("1000.00");
        InstallmentSchedule schedule = PriceSchedule.calculate(principal, new BigDecimal("1.5"), 1, FIRST_DUE_DATE);

        assertThat(TotalEffectiveCostRate.annualPercent(principal, schedule, RELEASE))
                .isEqualByComparingTo("19.16");
    }

    @Test
    @DisplayName("CAL-05 0003/CT-5: CET com 2 casas decimais")
    void twoDecimals() {
        assertThat(TotalEffectiveCostRate.annualPercent(PRINCIPAL, price(), RELEASE)
                        .scale())
                .isEqualTo(2);
    }

    @Test
    void rateAboveOneHundredPercentAYear() {
        BigDecimal released = PRINCIPAL.subtract(new BigDecimal("6000.00"));

        assertThat(TotalEffectiveCostRate.annualPercent(released, price(), RELEASE))
                .isEqualByComparingTo("931.54");
    }
}
