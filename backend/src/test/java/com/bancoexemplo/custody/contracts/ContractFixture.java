package com.bancoexemplo.custody.contracts;

import com.bancoexemplo.custody.contracts.domain.contract.AmortizationSystem;
import com.bancoexemplo.custody.contracts.domain.contract.ChassisNumber;
import com.bancoexemplo.custody.contracts.domain.contract.Contract;
import com.bancoexemplo.custody.contracts.domain.contract.ContractInstallment;
import com.bancoexemplo.custody.contracts.domain.contract.ContractNumber;
import com.bancoexemplo.custody.contracts.domain.contract.ContractStatus;
import com.bancoexemplo.custody.contracts.domain.contract.Cpf;
import com.bancoexemplo.custody.contracts.domain.contract.CustomerName;
import com.bancoexemplo.custody.contracts.domain.contract.FinancingTerms;
import com.bancoexemplo.custody.contracts.domain.contract.VehicleDescription;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** The spec example contract, as stored: R$ 10,000.00 at 2% a month for 12 months, first due on the given date. */
public final class ContractFixture {

    private ContractFixture() {}

    public static Contract contract(String number, String cpf, LocalDate firstDueDate, Instant registeredAt) {
        List<ContractInstallment> installments = new ArrayList<>();
        for (int n = 1; n <= 12; n++) {
            installments.add(ContractInstallment.reconstruct(
                    n,
                    firstDueDate.plusMonths(n - 1L),
                    new BigDecimal(n == 12 ? "945.55" : "945.60"),
                    new BigDecimal("10.00"),
                    new BigDecimal("935.60"),
                    new BigDecimal("100.00"),
                    null));
        }
        return Contract.reconstruct(
                UUID.randomUUID(),
                new ContractNumber(number),
                new CustomerName("Cliente Exemplo"),
                new Cpf(cpf),
                new ChassisNumber("9BWZZZ377VT004251"),
                new VehicleDescription("Hatch 1.0 2026"),
                terms(firstDueDate.minusMonths(1), firstDueDate),
                new BigDecimal("26.95"),
                ContractStatus.ACTIVE,
                "operador",
                registeredAt,
                installments);
    }

    public static Contract contract(String number) {
        return contract(number, "12345678909", LocalDate.of(2027, 2, 10), Instant.parse("2027-01-10T13:00:00Z"));
    }

    public static FinancingTerms terms(LocalDate releaseDate, LocalDate firstDueDate) {
        return new FinancingTerms(
                AmortizationSystem.PRICE,
                new BigDecimal("10000.00"),
                new BigDecimal("2.0000"),
                12,
                releaseDate,
                firstDueDate,
                new BigDecimal("0.00"));
    }
}
