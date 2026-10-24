package com.bancoexemplo.custody.archfixture;

import com.bancoexemplo.custody.calculator.domain.PriceSchedule;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Breaks the module boundary on purpose, so the architecture test can prove its rule fails. */
public final class ReachesIntoCalculatorDomain {

    private ReachesIntoCalculatorDomain() {}

    public static int installments() {
        return PriceSchedule.calculate(BigDecimal.TEN, BigDecimal.ONE, 1, LocalDate.of(2027, 1, 1))
                .installments()
                .size();
    }
}
