package com.bancoexemplo.custody.payments.adapter.in.web;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Body of {@code POST /api/contracts/{contractId}/payments}; missing fields arrive as null. */
public record RegisterPaymentRequest(Integer installmentNumber, LocalDate paidOn, BigDecimal amount) {}
