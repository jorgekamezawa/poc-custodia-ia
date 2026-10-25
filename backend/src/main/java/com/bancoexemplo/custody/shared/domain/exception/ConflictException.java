package com.bancoexemplo.custody.shared.domain.exception;

/** The operation clashes with what is already recorded, such as a number taken or an installment paid. */
public abstract class ConflictException extends DomainException {

    private static final long serialVersionUID = 1L;

    protected ConflictException(String message) {
        super(message);
    }
}
