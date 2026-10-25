package com.bancoexemplo.custody.contracts.adapter.out.persistence;

import com.bancoexemplo.custody.contracts.application.ContractSummary;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ContractJpaRepository extends JpaRepository<ContractJpaEntity, UUID> {

    String SUMMARY = "SELECT new com.bancoexemplo.custody.contracts.application.ContractSummary("
            + "c.id, c.number, c.customerName, c.customerCpf, c.principal, c.registeredAt) FROM ContractJpaEntity c";

    boolean existsByNumber(String number);

    // Installments come in the same query, so reading a contract never runs one query per installment.
    @Query("SELECT c FROM ContractJpaEntity c LEFT JOIN FETCH c.installments WHERE c.id = :id")
    Optional<ContractJpaEntity> findWithInstallments(@Param("id") UUID id);

    @Query(SUMMARY + " ORDER BY c.registeredAt DESC")
    List<ContractSummary> findLatest(Limit limit);

    @Query(SUMMARY + " WHERE c.customerCpf = :cpf ORDER BY c.registeredAt DESC")
    List<ContractSummary> findByCpf(@Param("cpf") String cpf, Limit limit);

    @Query(SUMMARY + " WHERE UPPER(c.number) LIKE :pattern ESCAPE '\\' ORDER BY c.registeredAt DESC")
    List<ContractSummary> findByNumberPattern(@Param("pattern") String pattern, Limit limit);
}
