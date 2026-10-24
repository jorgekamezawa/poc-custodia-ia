package com.bancoexemplo.custody.calculator.api;

import java.math.BigDecimal;

/** Total effective cost of a contract as an annual percentage with 2 decimals, so {@code 26.95} means 26.95% a year. */
public record TotalEffectiveCost(BigDecimal annualRatePercent) {}
