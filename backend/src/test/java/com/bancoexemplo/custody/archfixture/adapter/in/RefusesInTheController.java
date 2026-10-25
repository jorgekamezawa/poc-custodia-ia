package com.bancoexemplo.custody.archfixture.adapter.in;

import com.bancoexemplo.custody.contracts.domain.contract.InvalidContractException;

/** An inbound adapter that builds a domain refusal, so the architecture test can prove its rule fails. */
public final class RefusesInTheController {

    private RefusesInTheController() {}

    public static RuntimeException refusal() {
        return new InvalidContractException("Prazo é obrigatório");
    }
}
