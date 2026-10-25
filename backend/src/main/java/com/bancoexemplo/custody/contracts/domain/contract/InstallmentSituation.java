package com.bancoexemplo.custody.contracts.domain.contract;

import java.time.LocalDate;

/** Where an installment stands against today. */
public enum InstallmentSituation {
    OPEN,
    OVERDUE,
    PAID;

    public static InstallmentSituation of(ContractInstallment installment, LocalDate today) {
        return installment.isPaid() ? PAID : of(installment.getDueDate(), today);
    }

    /** Situation of an unpaid installment. */
    public static InstallmentSituation of(LocalDate dueDate, LocalDate today) {
        return dueDate.isBefore(today) ? OVERDUE : OPEN;
    }
}
