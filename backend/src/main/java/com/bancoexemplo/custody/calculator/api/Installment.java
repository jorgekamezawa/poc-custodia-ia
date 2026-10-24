package com.bancoexemplo.custody.calculator.api;

import java.math.BigDecimal;
import java.time.LocalDate;

/** One installment of a schedule; interest plus amortization is the amount. */
public record Installment(
        int number,
        LocalDate dueDate,
        BigDecimal amount,
        BigDecimal interest,
        BigDecimal amortization,
        BigDecimal balanceAfter) {}
