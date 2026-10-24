package com.bancoexemplo.custody.contracts.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Release date and installments of a contract, as other modules see them. */
public record ContractSchedule(LocalDate releaseDate, List<ScheduledInstallment> installments) {

    public ContractSchedule {
        installments = List.copyOf(installments);
    }

    /** One installment: number, due date and amount. */
    public record ScheduledInstallment(int number, LocalDate dueDate, BigDecimal amount) {}
}
