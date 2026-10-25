package com.bancoexemplo.custody.contracts.application.port;

import com.bancoexemplo.custody.contracts.application.ContractSummary;
import com.bancoexemplo.custody.contracts.domain.Contract;
import com.bancoexemplo.custody.contracts.domain.ContractNumber;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Where contracts are kept. */
public interface ContractRepository {

    boolean existsByNumber(ContractNumber number);

    /**
     * Stores a new contract with its installments.
     *
     * @throws com.bancoexemplo.custody.contracts.domain.DuplicateContractNumberException when the number is taken
     */
    void save(Contract contract);

    Optional<Contract> findById(UUID id);

    /** The most recently registered contracts, newest first. */
    List<ContractSummary> findLatest(int limit);

    /** Contracts of the customer with this CPF, newest first. */
    List<ContractSummary> findByCpf(String digits, int limit);

    /** Contracts whose number starts with the prefix, ignoring case, newest first. */
    List<ContractSummary> findByNumberPrefix(String prefix, int limit);

    /**
     * Records the payment date of an installment still open; the contract decides first, this only stores it.
     *
     * @throws com.bancoexemplo.custody.contracts.domain.InstallmentNotPayableException when no open installment matched
     */
    void markInstallmentPaid(UUID contractId, int installmentNumber, LocalDate paidOn);
}
