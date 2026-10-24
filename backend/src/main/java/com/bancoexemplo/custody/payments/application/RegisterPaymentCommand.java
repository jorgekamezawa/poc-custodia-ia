package com.bancoexemplo.custody.payments.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** A payment as the operator typed it, and who is registering it. */
public record RegisterPaymentCommand(
        UUID contractId, int installmentNumber, LocalDate paidOn, BigDecimal amount, String registeredBy) {}
