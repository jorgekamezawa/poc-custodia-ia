package com.bancoexemplo.custody.payments.domain.payment;

/** No contract with the id the payment refers to. */
public class ContractNotFoundForPaymentException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ContractNotFoundForPaymentException() {
        super("Contrato não encontrado");
    }
}
