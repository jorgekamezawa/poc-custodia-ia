package com.bancoexemplo.custody.contracts.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** A signed vehicle financing contract under custody, with the schedule and CET calculated at registration. */
public record Contract(
        UUID id,
        ContractNumber number,
        String customerName,
        Cpf customerCpf,
        ChassisNumber chassisNumber,
        String vehicleDescription,
        AmortizationSystem amortizationSystem,
        BigDecimal principal,
        BigDecimal monthlyRatePercent,
        int termMonths,
        LocalDate releaseDate,
        LocalDate firstDueDate,
        BigDecimal upfrontCosts,
        BigDecimal annualCetPercent,
        ContractStatus status,
        String registeredBy,
        Instant registeredAt,
        List<ContractInstallment> installments) {

    public Contract {
        installments = List.copyOf(installments);
    }

    /** Balance after the last paid installment; with none paid yet, the principal. */
    public BigDecimal outstandingBalance() {
        BigDecimal balance = principal;
        for (ContractInstallment installment : installments) {
            if (installment.paid()) {
                balance = installment.balanceAfter();
            }
        }
        return balance;
    }

    public Contract withInstallmentPaid(int installmentNumber, LocalDate paidOn) {
        return new Contract(
                id,
                number,
                customerName,
                customerCpf,
                chassisNumber,
                vehicleDescription,
                amortizationSystem,
                principal,
                monthlyRatePercent,
                termMonths,
                releaseDate,
                firstDueDate,
                upfrontCosts,
                annualCetPercent,
                status,
                registeredBy,
                registeredAt,
                installments.stream()
                        .map(installment ->
                                installment.number() == installmentNumber ? installment.paidOn(paidOn) : installment)
                        .toList());
    }
}
