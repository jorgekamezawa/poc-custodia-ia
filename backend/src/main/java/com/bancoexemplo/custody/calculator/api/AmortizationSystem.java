package com.bancoexemplo.custody.calculator.api;

/** How the principal is paid back over the installments. */
public enum AmortizationSystem {
    /** Every installment has the same amount. */
    PRICE,
    /** Every installment amortizes the same amount, so installments decrease. */
    SAC
}
