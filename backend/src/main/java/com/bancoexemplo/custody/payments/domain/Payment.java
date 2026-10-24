package com.bancoexemplo.custody.payments.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A registered payment of one installment.
 *
 * @param amount total paid, including the late fee and the default interest
 */
public record Payment(
        UUID id,
        UUID contractId,
        int installmentNumber,
        LocalDate paidOn,
        BigDecimal amount,
        BigDecimal lateFee,
        BigDecimal defaultInterest,
        String registeredBy,
        Instant registeredAt) {}
