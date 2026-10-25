package com.bancoexemplo.custody.shared.archfixture;

import com.bancoexemplo.custody.contracts.domain.contract.ContractNumber;

/** Shared code that knows a module, so the architecture test can prove its rule fails. */
public final class SharedReachingIntoAModule {

    private SharedReachingIntoAModule() {}

    public static ContractNumber number() {
        return new ContractNumber("CTR-0001");
    }
}
