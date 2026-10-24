package com.bancoexemplo.custody.contracts.api;

import java.util.Optional;
import java.util.UUID;

/** What other modules may read of a contract's schedule. */
public interface ContractInstallments {

    Optional<ContractSchedule> find(UUID contractId);
}
