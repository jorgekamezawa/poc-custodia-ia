package com.bancoexemplo.custody.contracts.application;

import com.bancoexemplo.custody.contracts.domain.contract.AmortizationSystem;
import java.math.BigDecimal;
import java.time.LocalDate;

/** What the operator typed from the signed contract, and who is registering it; any field may be missing. */
public record RegisterContractCommand(
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
        BigDecimal upfrontCosts,
        String registeredBy) {}
