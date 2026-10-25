package com.bancoexemplo.custody.contracts.adapter.out.persistence;

import com.bancoexemplo.custody.contracts.domain.contract.ChassisNumber;
import com.bancoexemplo.custody.contracts.domain.contract.Contract;
import com.bancoexemplo.custody.contracts.domain.contract.ContractInstallment;
import com.bancoexemplo.custody.contracts.domain.contract.ContractNumber;
import com.bancoexemplo.custody.contracts.domain.contract.Cpf;
import com.bancoexemplo.custody.contracts.domain.contract.CustomerName;
import com.bancoexemplo.custody.contracts.domain.contract.FinancingTerms;
import com.bancoexemplo.custody.contracts.domain.contract.VehicleDescription;

/** Copies a contract between the domain and its rows; reading goes through the domain's reconstruct. */
final class ContractJpaMapper {

    private ContractJpaMapper() {}

    static ContractJpaEntity toEntity(Contract contract) {
        ContractJpaEntity entity = new ContractJpaEntity();
        entity.id = contract.getId();
        entity.number = contract.getNumber().value();
        entity.customerName = contract.getCustomerName().value();
        entity.customerCpf = contract.getCustomerCpf().digits();
        entity.chassisNumber = contract.getChassisNumber().value();
        entity.vehicleDescription = contract.getVehicleDescription().value();
        FinancingTerms terms = contract.getTerms();
        entity.amortizationSystem = terms.amortizationSystem();
        entity.principal = terms.principal();
        entity.monthlyRatePercent = terms.monthlyRatePercent();
        entity.termMonths = terms.termMonths();
        entity.releaseDate = terms.releaseDate();
        entity.firstDueDate = terms.firstDueDate();
        entity.upfrontCosts = terms.upfrontCosts();
        entity.annualCetPercent = contract.getAnnualCetPercent();
        entity.status = contract.getStatus();
        entity.registeredBy = contract.getRegisteredBy();
        entity.registeredAt = contract.getRegisteredAt();
        for (ContractInstallment installment : contract.getInstallments()) {
            ContractInstallmentJpaEntity row = new ContractInstallmentJpaEntity();
            row.contractId = contract.getId();
            row.number = installment.getNumber();
            row.dueDate = installment.getDueDate();
            row.amount = installment.getAmount();
            row.interest = installment.getInterest();
            row.amortization = installment.getAmortization();
            row.balanceAfter = installment.getBalanceAfter();
            row.paidOn = installment.getPaidOn();
            entity.installments.add(row);
        }
        return entity;
    }

    static Contract toDomain(ContractJpaEntity entity) {
        return Contract.reconstruct(
                entity.id,
                new ContractNumber(entity.number),
                new CustomerName(entity.customerName),
                new Cpf(entity.customerCpf),
                new ChassisNumber(entity.chassisNumber),
                new VehicleDescription(entity.vehicleDescription),
                new FinancingTerms(
                        entity.amortizationSystem,
                        entity.principal,
                        entity.monthlyRatePercent,
                        entity.termMonths,
                        entity.releaseDate,
                        entity.firstDueDate,
                        entity.upfrontCosts),
                entity.annualCetPercent,
                entity.status,
                entity.registeredBy,
                entity.registeredAt,
                entity.installments.stream()
                        .map(row -> ContractInstallment.reconstruct(
                                row.number,
                                row.dueDate,
                                row.amount,
                                row.interest,
                                row.amortization,
                                row.balanceAfter,
                                row.paidOn))
                        .toList());
    }
}
