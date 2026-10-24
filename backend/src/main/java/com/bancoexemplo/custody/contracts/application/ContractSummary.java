package com.bancoexemplo.custody.contracts.application;

import com.bancoexemplo.custody.contracts.domain.Contract;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** A contract as a line of the search result. */
public record ContractSummary(
        UUID id, String number, String customerName, String customerCpf, BigDecimal principal, Instant registeredAt) {

    public static ContractSummary of(Contract contract) {
        return new ContractSummary(
                contract.id(),
                contract.number().value(),
                contract.customerName(),
                contract.customerCpf().digits(),
                contract.principal(),
                contract.registeredAt());
    }
}
