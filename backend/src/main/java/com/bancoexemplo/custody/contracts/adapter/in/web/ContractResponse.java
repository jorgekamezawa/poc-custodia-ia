package com.bancoexemplo.custody.contracts.adapter.in.web;

import com.bancoexemplo.custody.contracts.application.ContractView;
import com.bancoexemplo.custody.contracts.domain.Contract;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** A contract as the portal sees it, with today's outstanding balance and installment situations. */
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
        BigDecimal outstandingBalance,
        List<InstallmentResponse> installments) {

    /** One installment and its situation today. */
    public record InstallmentResponse(
            int number,
            LocalDate dueDate,
            BigDecimal amount,
            BigDecimal interest,
            BigDecimal amortization,
            BigDecimal balanceAfter,
            String situation) {}

    static ContractResponse of(ContractView view) {
        Contract contract = view.contract();
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
                view.outstandingBalance(),
                view.installments().stream()
                        .map(item -> new InstallmentResponse(
                                item.installment().number(),
                                item.installment().dueDate(),
                                item.installment().amount(),
                                item.installment().interest(),
                                item.installment().amortization(),
                                item.installment().balanceAfter(),
                                item.situation().name()))
                        .toList());
    }
}
