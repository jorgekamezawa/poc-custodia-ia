package com.bancoexemplo.custody.shared.domain.exception;

/** What the operation refers to does not exist. */
public abstract class NotFoundException extends DomainException {

    private static final long serialVersionUID = 1L;

    protected NotFoundException(String message) {
        super(message);
    }
}
