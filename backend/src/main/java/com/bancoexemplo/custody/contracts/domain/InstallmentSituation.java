package com.bancoexemplo.custody.contracts.domain;

import java.time.LocalDate;

/** Where an installment stands against today. */
public enum InstallmentSituation {
    OPEN,
    OVERDUE,
    PAID;

    public static InstallmentSituation of(ContractInstallment installment, LocalDate today) {
        return installment.paid() ? PAID : of(installment.dueDate(), today);
    }

    /** Situation of an unpaid installment. */
    public static InstallmentSituation of(LocalDate dueDate, LocalDate today) {
        return dueDate.isBefore(today) ? OVERDUE : OPEN;
    }
}
