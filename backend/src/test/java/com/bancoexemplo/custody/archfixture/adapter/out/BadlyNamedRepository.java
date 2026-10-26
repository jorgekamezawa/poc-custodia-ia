package com.bancoexemplo.custody.archfixture.adapter.out;

import com.bancoexemplo.custody.contracts.api.ContractInstallments;
import com.bancoexemplo.custody.contracts.api.ContractSchedule;
import java.util.Optional;
import java.util.UUID;

/** An outbound adapter not named after its port, so the architecture test can prove its rule fails. */
public final class BadlyNamedRepository implements ContractInstallments {

    @Override
    public Optional<ContractSchedule> find(UUID contractId) {
        return Optional.empty();
    }
}
