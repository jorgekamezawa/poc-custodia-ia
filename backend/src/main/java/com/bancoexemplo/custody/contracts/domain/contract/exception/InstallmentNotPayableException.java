package com.bancoexemplo.custody.contracts.domain.contract.exception;

import com.bancoexemplo.custody.shared.domain.exception.BusinessRuleException;

/** The installment cannot be recorded as paid: it is not in the schedule or it is already paid. */
public class InstallmentNotPayableException extends BusinessRuleException {

    private static final long serialVersionUID = 1L;

    public InstallmentNotPayableException(String message) {
        super(message);
    }
}
