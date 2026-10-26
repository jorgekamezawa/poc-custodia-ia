package com.bancoexemplo.custody.contracts.adapter.out.persistence;

import com.bancoexemplo.custody.contracts.application.ContractSummary;
import com.bancoexemplo.custody.contracts.application.port.ContractRepository;
import com.bancoexemplo.custody.contracts.domain.contract.Contract;
import com.bancoexemplo.custody.contracts.domain.contract.ContractNumber;
import com.bancoexemplo.custody.contracts.domain.contract.exception.DuplicateContractNumberException;
import com.bancoexemplo.custody.contracts.domain.contract.exception.InstallmentNotPayableException;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
class ContractRepositoryJpaImpl implements ContractRepository {

    private static final String NUMBER_KEY = "contract_number_key";
    private static final Pattern LIKE_SPECIAL = Pattern.compile("[\\\\%_]");

    private final EntityManager entityManager;
    private final ContractJpaRepository contracts;
    private final ContractInstallmentJpaRepository installments;

    ContractRepositoryJpaImpl(
            EntityManager entityManager,
            ContractJpaRepository contracts,
            ContractInstallmentJpaRepository installments) {
        this.entityManager = entityManager;
        this.contracts = contracts;
        this.installments = installments;
    }

    @Override
    public boolean existsByNumber(ContractNumber number) {
        return contracts.existsByNumber(number.value());
    }

    @Override
    @Transactional
    public void save(Contract contract) {
        ContractJpaEntity entity = ContractJpaMapper.toEntity(contract);
        try {
            // Persist rather than save: the id comes from the domain, and save would look for an existing row first.
            entityManager.persist(entity);
            // Flush here so a taken number surfaces in this method, where it becomes the domain's refusal.
            entityManager.flush();
        } catch (ConstraintViolationException e) {
            if (NUMBER_KEY.equals(e.getConstraintName())) {
                throw new DuplicateContractNumberException();
            }
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Contract> findById(UUID id) {
        return contracts.findWithInstallments(id).map(ContractJpaMapper::toDomain);
    }

    @Override
    public List<ContractSummary> findLatest(int limit) {
        return contracts.findLatest(Limit.of(limit));
    }

    @Override
    public List<ContractSummary> findByCpf(String digits, int limit) {
        return contracts.findByCpf(digits, Limit.of(limit));
    }

    @Override
    public List<ContractSummary> findByNumberPrefix(String prefix, int limit) {
        String pattern = LIKE_SPECIAL.matcher(prefix.toUpperCase(Locale.ROOT)).replaceAll("\\\\$0") + "%";
        return contracts.findByNumberPattern(pattern, Limit.of(limit));
    }

    @Override
    @Transactional
    public void markInstallmentPaid(UUID contractId, int installmentNumber, LocalDate paidOn) {
        if (installments.markPaidIfOpen(contractId, installmentNumber, paidOn) != 1) {
            throw new InstallmentNotPayableException("Parcela " + installmentNumber + " não está em aberto");
        }
    }
}
