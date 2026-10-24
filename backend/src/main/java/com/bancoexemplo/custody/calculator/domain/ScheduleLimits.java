package com.bancoexemplo.custody.calculator.domain;

import com.bancoexemplo.custody.calculator.api.InvalidScheduleRequestException;
import com.bancoexemplo.custody.calculator.api.ScheduleRequest;
import java.math.BigDecimal;

/** The range of principal, rate and term the custody accepts. */
public final class ScheduleLimits {

    private static final BigDecimal MIN_PRINCIPAL = new BigDecimal("1000.00");
    private static final BigDecimal MAX_PRINCIPAL = new BigDecimal("1000000.00");
    private static final BigDecimal MAX_RATE_PERCENT = new BigDecimal("10");
    private static final int MAX_RATE_DECIMALS = 4;
    private static final int MIN_TERM = 1;
    private static final int MAX_TERM = 72;

    private ScheduleLimits() {}

    // The "% e" in "0% e 10%" is Portuguese text, not a format specifier.
    @SuppressWarnings("OrphanedFormatString")
    public static void validate(ScheduleRequest request) {
        if (outside(request.principal(), MIN_PRINCIPAL, MAX_PRINCIPAL)) {
            throw new InvalidScheduleRequestException(
                    "Valor financiado deve estar entre R$ 1.000,00 e R$ 1.000.000,00");
        }
        BigDecimal rate = request.monthlyRatePercent();
        if (outside(rate, BigDecimal.ZERO, MAX_RATE_PERCENT)
                || rate.stripTrailingZeros().scale() > MAX_RATE_DECIMALS) {
            throw new InvalidScheduleRequestException(
                    "Taxa deve estar entre 0% e 10% ao mês, com até 4 casas decimais");
        }
        if (request.termMonths() < MIN_TERM || request.termMonths() > MAX_TERM) {
            throw new InvalidScheduleRequestException("Prazo deve estar entre 1 e 72 meses");
        }
    }

    private static boolean outside(BigDecimal value, BigDecimal min, BigDecimal max) {
        return value.compareTo(min) < 0 || value.compareTo(max) > 0;
    }
}
