package com.bancoexemplo.custody.contracts.domain;

/** A field of the contract outside the rules; the message names the field and is shown to the operator. */
public class InvalidContractException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidContractException(String message) {
        super(message);
    }
}
