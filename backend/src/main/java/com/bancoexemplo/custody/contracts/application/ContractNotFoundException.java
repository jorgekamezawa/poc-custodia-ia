package com.bancoexemplo.custody.contracts.application;

/** No contract with the requested id. */
public class ContractNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ContractNotFoundException() {
        super("Contrato não encontrado");
    }
}
