package com.bancoexemplo.custody.calculator.domain;

import java.time.LocalDate;

/** Monthly due dates counted from the first one. */
final class DueDates {

    private DueDates() {}

    static LocalDate of(LocalDate firstDueDate, int installmentNumber) {
        // plusMonths from the first date keeps the original day after a short month.
        return firstDueDate.plusMonths(installmentNumber - 1L);
    }
}
