package com.bancoexemplo.custody.contracts.application.exception;

import com.bancoexemplo.custody.shared.domain.exception.NotFoundException;

/** No contract with the requested id. */
public class ContractNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 1L;

    public ContractNotFoundException() {
        super("Contrato não encontrado");
    }
}
