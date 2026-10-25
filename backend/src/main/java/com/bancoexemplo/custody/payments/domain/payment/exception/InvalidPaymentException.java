package com.bancoexemplo.custody.payments.domain.payment.exception;

import com.bancoexemplo.custody.shared.domain.exception.BusinessRuleException;

/** A payment outside the rules; the message is shown to the operator. */
public class InvalidPaymentException extends BusinessRuleException {

    private static final long serialVersionUID = 1L;

    public InvalidPaymentException(String message) {
        super(message);
    }
}
