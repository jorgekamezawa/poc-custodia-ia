package com.bancoexemplo.custody.contracts.adapter.in.web;

import com.bancoexemplo.custody.contracts.application.ContractSummary;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** A line of {@code GET /api/contracts}. */
public record ContractSummaryResponse(
        UUID id, String number, String customerName, String customerCpf, BigDecimal principal, Instant registeredAt) {

    static ContractSummaryResponse of(ContractSummary summary) {
        return new ContractSummaryResponse(
                summary.id(),
                summary.number(),
                summary.customerName(),
                summary.customerCpf(),
                summary.principal(),
                summary.registeredAt());
    }
}
