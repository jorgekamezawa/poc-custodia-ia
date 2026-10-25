package com.bancoexemplo.custody.contracts.adapter.in.web;

import com.bancoexemplo.custody.contracts.domain.contract.AmortizationSystem;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Body of {@code POST /api/contracts}; a missing financing term is refused here, before the use case. The text fields
 * are refused by the contract's own value objects, with their messages.
 */
public record RegisterContractRequest(
        String number,
        String customerName,
        String customerCpf,
        String chassisNumber,
        String vehicleDescription,

        @NotNull(message = "Sistema de amortização é obrigatório")
        AmortizationSystem amortizationSystem,

        @NotNull(message = "Valor financiado é obrigatório") BigDecimal principal,
        @NotNull(message = "Taxa ao mês é obrigatório") BigDecimal monthlyRatePercent,
        @NotNull(message = "Prazo é obrigatório") Integer termMonths,

        @NotNull(message = "Data de liberação é obrigatório")
        LocalDate releaseDate,

        @NotNull(message = "Primeiro vencimento é obrigatório")
        LocalDate firstDueDate,

        @NotNull(message = "Custos pagos antecipadamente é obrigatório")
        BigDecimal upfrontCosts) {}
