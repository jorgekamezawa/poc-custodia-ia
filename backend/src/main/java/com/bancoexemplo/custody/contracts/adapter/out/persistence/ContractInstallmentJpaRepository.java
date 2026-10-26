package com.bancoexemplo.custody.contracts.adapter.out.persistence;

import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ContractInstallmentJpaRepository
        extends JpaRepository<ContractInstallmentJpaEntity, ContractInstallmentJpaEntity.Key> {

    /** Records the payment date only while the installment is open; the count tells whether it was. */
    @Modifying
    @Query("UPDATE ContractInstallmentJpaEntity i SET i.paidOn = :paidOn"
            + " WHERE i.contractId = :contractId AND i.number = :number AND i.paidOn IS NULL")
    int markPaidIfOpen(
            @Param("contractId") UUID contractId, @Param("number") int number, @Param("paidOn") LocalDate paidOn);
}
