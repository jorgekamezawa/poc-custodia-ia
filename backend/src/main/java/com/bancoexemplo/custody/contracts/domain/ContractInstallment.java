package com.bancoexemplo.custody.contracts.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

/** One installment of the schedule stored with the contract. */
public record ContractInstallment(
        int number,
        LocalDate dueDate,
        BigDecimal amount,
        BigDecimal interest,
        BigDecimal amortization,
        BigDecimal balanceAfter) {}
