package com.bancoexemplo.custody.contracts.adapter.in.web;

import com.bancoexemplo.custody.contracts.domain.Contract;
import com.bancoexemplo.custody.contracts.domain.ContractInstallment;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** A contract as the portal sees it. */
public record ContractResponse(
        UUID id,
        String number,
        String customerName,
        String customerCpf,
        String chassisNumber,
        String vehicleDescription,
        String amortizationSystem,
        BigDecimal principal,
        BigDecimal monthlyRatePercent,
        int termMonths,
        LocalDate releaseDate,
        LocalDate firstDueDate,
        BigDecimal upfrontCosts,
        BigDecimal annualCetPercent,
        String status,
        String registeredBy,
        Instant registeredAt,
        List<ContractInstallment> installments) {

    static ContractResponse of(Contract contract) {
        return new ContractResponse(
                contract.id(),
                contract.number().value(),
                contract.customerName(),
                contract.customerCpf().digits(),
                contract.chassisNumber().value(),
                contract.vehicleDescription(),
                contract.amortizationSystem().name(),
                contract.principal(),
                contract.monthlyRatePercent(),
                contract.termMonths(),
                contract.releaseDate(),
                contract.firstDueDate(),
                contract.upfrontCosts(),
                contract.annualCetPercent(),
                contract.status().name(),
                contract.registeredBy(),
                contract.registeredAt(),
                contract.installments());
    }
}
