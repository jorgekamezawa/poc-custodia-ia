package com.bancoexemplo.custody.contracts.domain;

/** Another contract already has this number. */
public class DuplicateContractNumberException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DuplicateContractNumberException() {
        super("Número do contrato já cadastrado");
    }
}
