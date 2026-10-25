package com.bancoexemplo.custody.payments.domain.payment.exception;

import com.bancoexemplo.custody.shared.domain.exception.ConflictException;

/** The installment already has a payment. */
public class InstallmentAlreadyPaidException extends ConflictException {

    private static final long serialVersionUID = 1L;

    public InstallmentAlreadyPaidException() {
        super("Parcela já paga");
    }
}
