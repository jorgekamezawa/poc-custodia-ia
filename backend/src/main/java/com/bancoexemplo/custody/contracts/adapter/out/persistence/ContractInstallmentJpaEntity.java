package com.bancoexemplo.custody.contracts.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** A row of {@code contract_installment}, keyed by the contract and the installment number. */
@Entity
@Table(name = "contract_installment")
@IdClass(ContractInstallmentJpaEntity.Key.class)
class ContractInstallmentJpaEntity {

    @Id
    @Column(name = "contract_id")
    UUID contractId;

    @Id
    int number;

    @Column(name = "due_date")
    LocalDate dueDate;

    BigDecimal amount;

    BigDecimal interest;

    BigDecimal amortization;

    @Column(name = "balance_after")
    BigDecimal balanceAfter;

    @Column(name = "paid_on")
    LocalDate paidOn;

    /** The composite key the table declares. */
    record Key(UUID contractId, int number) implements Serializable {}
}
