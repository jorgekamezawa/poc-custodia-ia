package com.bancoexemplo.custody.contracts.domain;

/** The installment cannot be recorded as paid: it is not in the schedule or it is already paid. */
public class InstallmentNotPayableException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InstallmentNotPayableException(String message) {
        super(message);
    }
}
