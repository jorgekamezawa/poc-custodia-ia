package com.bancoexemplo.custody.calculator.api;

import java.util.List;

/** The installments of a contract, in payment order. */
public record InstallmentSchedule(List<Installment> installments) {

    public InstallmentSchedule {
        installments = List.copyOf(installments);
    }
}
