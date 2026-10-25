package com.bancoexemplo.custody.contracts.adapter.in.web;

import com.bancoexemplo.custody.contracts.application.ContractView;
import com.bancoexemplo.custody.contracts.domain.contract.Contract;
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
                contract.getId(),
                contract.getNumber().value(),
                contract.getCustomerName().value(),
                contract.getCustomerCpf().digits(),
                contract.getChassisNumber().value(),
                contract.getVehicleDescription().value(),
                contract.getTerms().amortizationSystem().name(),
                contract.getTerms().principal(),
                contract.getTerms().monthlyRatePercent(),
                contract.getTerms().termMonths(),
                contract.getTerms().releaseDate(),
                contract.getTerms().firstDueDate(),
                contract.getTerms().upfrontCosts(),
                contract.getAnnualCetPercent(),
                contract.getStatus().name(),
                contract.getRegisteredBy(),
                contract.getRegisteredAt(),
                view.outstandingBalance(),
                view.installments().stream()
                        .map(item -> new InstallmentResponse(
                                item.installment().getNumber(),
                                item.installment().getDueDate(),
                                item.installment().getAmount(),
                                item.installment().getInterest(),
                                item.installment().getAmortization(),
                                item.installment().getBalanceAfter(),
                                item.situation().name()))
                        .toList());
    }
}
