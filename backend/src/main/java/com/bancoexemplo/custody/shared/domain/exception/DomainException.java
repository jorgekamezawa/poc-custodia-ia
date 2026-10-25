package com.bancoexemplo.custody.shared.domain.exception;

/** A refusal by the business, with a message the operator reads; each category maps to one response at the edge. */
public abstract class DomainException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    protected DomainException(String message) {
        super(message);
    }
}
