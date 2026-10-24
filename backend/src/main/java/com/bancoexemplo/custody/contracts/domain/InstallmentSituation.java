package com.bancoexemplo.custody.contracts.domain;

import java.time.LocalDate;

/** Where an unpaid installment stands against today. */
public enum InstallmentSituation {
    OPEN,
    OVERDUE;

    public static InstallmentSituation of(LocalDate dueDate, LocalDate today) {
        return dueDate.isBefore(today) ? OVERDUE : OPEN;
    }
}
