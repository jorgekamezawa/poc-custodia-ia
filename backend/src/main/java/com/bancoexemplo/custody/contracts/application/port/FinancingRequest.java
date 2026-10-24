package com.bancoexemplo.custody.contracts.application.port;

import com.bancoexemplo.custody.contracts.domain.AmortizationSystem;
import java.math.BigDecimal;
import java.time.LocalDate;

/** The financing terms of a contract, as the calculation needs them. */
public record FinancingRequest(
        AmortizationSystem system,
        BigDecimal principal,
        BigDecimal monthlyRatePercent,
        int termMonths,
        LocalDate releaseDate,
        LocalDate firstDueDate,
        BigDecimal upfrontCosts) {}
