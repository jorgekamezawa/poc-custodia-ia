package com.bancoexemplo.custody.payments.domain.payment;

/** The installment already has a payment. */
public class InstallmentAlreadyPaidException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InstallmentAlreadyPaidException() {
        super("Parcela já paga");
    }
}
