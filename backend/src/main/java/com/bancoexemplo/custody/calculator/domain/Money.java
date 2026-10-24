package com.bancoexemplo.custody.calculator.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Where a calculated value becomes an amount in reais. */
final class Money {

    static final int CENTS = 2;

    private Money() {}

    static BigDecimal of(BigDecimal value) {
        return value.setScale(CENTS, RoundingMode.HALF_EVEN);
    }
}
