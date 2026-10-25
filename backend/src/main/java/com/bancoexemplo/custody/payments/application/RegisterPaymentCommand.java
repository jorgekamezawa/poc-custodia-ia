package com.bancoexemplo.custody.payments.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** A payment as the operator typed it, and who is registering it; any typed field may be missing. */
public record RegisterPaymentCommand(
        UUID contractId, Integer installmentNumber, LocalDate paidOn, BigDecimal amount, String registeredBy) {}
