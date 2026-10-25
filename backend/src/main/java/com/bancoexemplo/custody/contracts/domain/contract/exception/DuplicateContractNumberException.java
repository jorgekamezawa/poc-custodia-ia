package com.bancoexemplo.custody.contracts.domain.contract.exception;

import com.bancoexemplo.custody.shared.domain.exception.ConflictException;

/** Another contract already has this number. */
public class DuplicateContractNumberException extends ConflictException {

    private static final long serialVersionUID = 1L;

    public DuplicateContractNumberException() {
        super("Número do contrato já cadastrado");
    }
}
