package com.bancoexemplo.custody.contracts;

import com.bancoexemplo.custody.contracts.domain.AmortizationSystem;
import com.bancoexemplo.custody.contracts.domain.ChassisNumber;
import com.bancoexemplo.custody.contracts.domain.Contract;
import com.bancoexemplo.custody.contracts.domain.ContractInstallment;
import com.bancoexemplo.custody.contracts.domain.ContractNumber;
import com.bancoexemplo.custody.contracts.domain.ContractStatus;
import com.bancoexemplo.custody.contracts.domain.Cpf;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** The spec example contract: R$ 10,000.00 at 2% a month for 12 months, first due on the given date. */
public final class ContractFixture {

    private ContractFixture() {}

    public static Contract contract(String number, String cpf, LocalDate firstDueDate, Instant registeredAt) {
        List<ContractInstallment> installments = new ArrayList<>();
        for (int n = 1; n <= 12; n++) {
            installments.add(new ContractInstallment(
                    n,
                    firstDueDate.plusMonths(n - 1L),
                    new BigDecimal(n == 12 ? "945.55" : "945.60"),
                    new BigDecimal("10.00"),
                    new BigDecimal("935.60"),
                    new BigDecimal("100.00")));
        }
        return new Contract(
                UUID.randomUUID(),
                new ContractNumber(number),
                "Cliente Exemplo",
                new Cpf(cpf),
                new ChassisNumber("9BWZZZ377VT004251"),
                "Hatch 1.0 2026",
                AmortizationSystem.PRICE,
                new BigDecimal("10000.00"),
                new BigDecimal("2.0000"),
                12,
                firstDueDate.minusMonths(1),
                firstDueDate,
                new BigDecimal("0.00"),
                new BigDecimal("26.95"),
                ContractStatus.ACTIVE,
                "operador",
                registeredAt,
                installments);
    }

    public static Contract contract(String number) {
        return contract(number, "12345678909", LocalDate.of(2027, 2, 10), Instant.parse("2027-01-10T13:00:00Z"));
    }
}
