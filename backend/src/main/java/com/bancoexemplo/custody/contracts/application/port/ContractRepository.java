package com.bancoexemplo.custody.contracts.application.port;

import com.bancoexemplo.custody.contracts.domain.Contract;
import com.bancoexemplo.custody.contracts.domain.ContractNumber;
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
}
