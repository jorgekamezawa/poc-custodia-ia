package com.bancoexemplo.custody.payments.application;

import java.math.BigDecimal;

/** The amount due for an installment on a payment date. */
public record PaymentQuote(
        int installmentNumber,
        BigDecimal installmentAmount,
        int daysLate,
        BigDecimal lateFee,
        BigDecimal defaultInterest,
        BigDecimal total) {}
