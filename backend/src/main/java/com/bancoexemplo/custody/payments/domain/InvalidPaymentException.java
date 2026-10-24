package com.bancoexemplo.custody.payments.domain;

/** A payment outside the rules; the message is shown to the operator. */
public class InvalidPaymentException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidPaymentException(String message) {
        super(message);
    }
}
