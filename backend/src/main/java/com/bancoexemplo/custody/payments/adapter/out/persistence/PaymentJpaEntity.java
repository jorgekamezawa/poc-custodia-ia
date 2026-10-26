package com.bancoexemplo.custody.payments.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** A row of {@code payment}; the domain never sees this class. */
@Entity
@Table(name = "payment")
class PaymentJpaEntity {

    @Id
    UUID id;

    @Column(name = "contract_id")
    UUID contractId;

    @Column(name = "installment_number")
    int installmentNumber;

    @Column(name = "paid_on")
    LocalDate paidOn;

    BigDecimal amount;

    @Column(name = "late_fee")
    BigDecimal lateFee;

    @Column(name = "default_interest")
    BigDecimal defaultInterest;

    @Column(name = "registered_by")
    String registeredBy;

    @Column(name = "registered_at")
    Instant registeredAt;
}
