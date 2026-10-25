package com.bancoexemplo.custody.contracts.domain.contract.exception;

import com.bancoexemplo.custody.shared.domain.exception.BusinessRuleException;

/** A field of the contract outside the rules; the message names the field and is shown to the operator. */
public class InvalidContractException extends BusinessRuleException {

    private static final long serialVersionUID = 1L;

    public InvalidContractException(String message) {
        super(message);
    }
}
