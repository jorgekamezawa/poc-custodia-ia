package com.bancoexemplo.custody.shared.domain.exception;

/** A business rule refuses the operation as asked. */
public abstract class BusinessRuleException extends DomainException {

    private static final long serialVersionUID = 1L;

    protected BusinessRuleException(String message) {
        super(message);
    }
}
