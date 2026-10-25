package com.bancoexemplo.custody.payments.domain.payment;

import java.math.BigDecimal;
import java.time.LocalDate;

/** An installment as far as paying it goes: number, due date and amount. */
public record InstallmentTerms(int number, LocalDate dueDate, BigDecimal amount) {}
