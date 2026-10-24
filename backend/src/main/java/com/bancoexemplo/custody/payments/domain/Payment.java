package com.bancoexemplo.custody.payments.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** A registered payment of one installment. */
public record Payment(
        UUID id,
        UUID contractId,
        int installmentNumber,
        LocalDate paidOn,
        BigDecimal amount,
        String registeredBy,
        Instant registeredAt) {}
