package com.bancoexemplo.custody.contracts.domain.contract;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** A signed vehicle financing contract under custody, with the schedule and CET calculated at registration. */
public final class Contract {

    private final UUID id;
    private final ContractNumber number;
    private final CustomerName customerName;
    private final Cpf customerCpf;
    private final ChassisNumber chassisNumber;
    private final VehicleDescription vehicleDescription;
    private final FinancingTerms terms;
    private final BigDecimal annualCetPercent;
    private final ContractStatus status;
    private final String registeredBy;
    private final Instant registeredAt;
    private final List<ContractInstallment> installments;

    private Contract(
            UUID id,
            ContractNumber number,
            CustomerName customerName,
            Cpf customerCpf,
            ChassisNumber chassisNumber,
            VehicleDescription vehicleDescription,
            FinancingTerms terms,
            BigDecimal annualCetPercent,
            ContractStatus status,
            String registeredBy,
            Instant registeredAt,
            List<ContractInstallment> installments) {
        this.id = id;
        this.number = number;
        this.customerName = customerName;
        this.customerCpf = customerCpf;
        this.chassisNumber = chassisNumber;
        this.vehicleDescription = vehicleDescription;
        this.terms = terms;
        this.annualCetPercent = annualCetPercent;
        this.status = status;
        this.registeredBy = registeredBy;
        this.registeredAt = registeredAt;
        this.installments = List.copyOf(installments);
    }

    /** Registers a signed contract with the schedule and CET calculated for its terms; it starts active. */
    public static Contract create(
            ContractNumber number,
            CustomerName customerName,
            Cpf customerCpf,
            ChassisNumber chassisNumber,
            VehicleDescription vehicleDescription,
            FinancingTerms terms,
            Financing financing,
            String registeredBy,
            Instant registeredAt) {
        return new Contract(
                UUID.randomUUID(),
                Objects.requireNonNull(number),
                Objects.requireNonNull(customerName),
                Objects.requireNonNull(customerCpf),
                Objects.requireNonNull(chassisNumber),
                Objects.requireNonNull(vehicleDescription),
                Objects.requireNonNull(terms),
                financing.annualCetPercent(),
                ContractStatus.ACTIVE,
                Objects.requireNonNull(registeredBy),
                Objects.requireNonNull(registeredAt),
                financing.installments().stream()
                        .map(ContractInstallment::create)
                        .toList());
    }

    /** A contract as stored; nothing is checked again, so a rule that changed later never blocks reading it. */
    public static Contract reconstruct(
            UUID id,
            ContractNumber number,
            CustomerName customerName,
            Cpf customerCpf,
            ChassisNumber chassisNumber,
            VehicleDescription vehicleDescription,
            FinancingTerms terms,
            BigDecimal annualCetPercent,
            ContractStatus status,
            String registeredBy,
            Instant registeredAt,
            List<ContractInstallment> installments) {
        return new Contract(
                id,
                number,
                customerName,
                customerCpf,
                chassisNumber,
                vehicleDescription,
                terms,
                annualCetPercent,
                status,
                registeredBy,
                registeredAt,
                installments);
    }

    /** Balance after the last paid installment; with none paid yet, the principal. */
    public BigDecimal outstandingBalance() {
        BigDecimal balance = terms.principal();
        for (ContractInstallment installment : installments) {
            if (installment.isPaid()) {
                balance = installment.getBalanceAfter();
            }
        }
        return balance;
    }

    /** Records the installment as paid on the date; refused when it is not in the schedule or already paid. */
    public void payInstallment(int installmentNumber, LocalDate paidOn) {
        ContractInstallment toPay = installments.stream()
                .filter(candidate -> candidate.getNumber() == installmentNumber)
                .findFirst()
                .orElseThrow(() ->
                        new InstallmentNotPayableException("Parcela " + installmentNumber + " não existe no contrato"));
        if (toPay.isPaid()) {
            throw new InstallmentNotPayableException("Parcela " + installmentNumber + " já está paga");
        }
        toPay.pay(paidOn);
    }

    public UUID getId() {
        return id;
    }

    public ContractNumber getNumber() {
        return number;
    }

    public CustomerName getCustomerName() {
        return customerName;
    }

    public Cpf getCustomerCpf() {
        return customerCpf;
    }

    public ChassisNumber getChassisNumber() {
        return chassisNumber;
    }

    public VehicleDescription getVehicleDescription() {
        return vehicleDescription;
    }

    public FinancingTerms getTerms() {
        return terms;
    }

    public BigDecimal getAnnualCetPercent() {
        return annualCetPercent;
    }

    public ContractStatus getStatus() {
        return status;
    }

    public String getRegisteredBy() {
        return registeredBy;
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }

    public List<ContractInstallment> getInstallments() {
        return installments;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Contract contract && contract.id.equals(id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
