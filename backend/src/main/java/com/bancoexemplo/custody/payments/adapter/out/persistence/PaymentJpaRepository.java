package com.bancoexemplo.custody.payments.adapter.out.persistence;

import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface PaymentJpaRepository extends JpaRepository<PaymentJpaEntity, UUID> {

    @Query("SELECT p.installmentNumber FROM PaymentJpaEntity p WHERE p.contractId = :contractId")
    Set<Integer> findInstallmentNumbers(@Param("contractId") UUID contractId);
}
