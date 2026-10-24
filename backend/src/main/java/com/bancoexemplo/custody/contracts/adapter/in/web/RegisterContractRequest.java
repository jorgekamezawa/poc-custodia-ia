package com.bancoexemplo.custody.contracts.adapter.in.web;

import com.bancoexemplo.custody.contracts.domain.AmortizationSystem;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Body of {@code POST /api/contracts}; missing fields arrive as null and are refused by name. */
public record RegisterContractRequest(
        String number,
        String customerName,
        String customerCpf,
        String chassisNumber,
        String vehicleDescription,
        AmortizationSystem amortizationSystem,
        BigDecimal principal,
        BigDecimal monthlyRatePercent,
        Integer termMonths,
        LocalDate releaseDate,
        LocalDate firstDueDate,
        BigDecimal upfrontCosts) {}
