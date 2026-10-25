package com.bancoexemplo.custody.contracts.domain.contract;

import java.math.BigDecimal;
import java.time.LocalDate;

/** One installment as the calculation produced it, before it belongs to a contract. */
public record CalculatedInstallment(
        int number,
        LocalDate dueDate,
        BigDecimal amount,
        BigDecimal interest,
        BigDecimal amortization,
        BigDecimal balanceAfter) {}
