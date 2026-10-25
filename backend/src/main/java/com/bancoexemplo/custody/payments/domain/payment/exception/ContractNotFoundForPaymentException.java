package com.bancoexemplo.custody.payments.domain.payment.exception;

import com.bancoexemplo.custody.shared.domain.exception.NotFoundException;

/** No contract with the id the payment refers to. */
public class ContractNotFoundForPaymentException extends NotFoundException {

    private static final long serialVersionUID = 1L;

    public ContractNotFoundForPaymentException() {
        super("Contrato não encontrado");
    }
}
