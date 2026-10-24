package com.bancoexemplo.custody.payments.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LateChargesTest {

    private static final BigDecimal AMOUNT = new BigDecimal("945.60");
    private static final LocalDate DUE = LocalDate.of(2026, 10, 10);

    @Test
    @DisplayName("PAG-05 0008/CT-1: 14 dias de atraso têm multa de 2% e juros de 1% ao mês pelos dias")
    void fourteenDaysLate() {
        LateCharges charges = LateCharges.of(AMOUNT, DUE, LocalDate.of(2026, 10, 24));

        assertThat(charges.daysLate()).isEqualTo(14);
        assertThat(charges.lateFee()).isEqualByComparingTo("18.91");
        assertThat(charges.defaultInterest()).isEqualByComparingTo("4.41");
        assertThat(charges.total()).isEqualByComparingTo("968.92");
    }

    @Test
    @DisplayName("PAG-06 0008/CT-2: 1 dia de atraso")
    void oneDayLate() {
        LateCharges charges = LateCharges.of(AMOUNT, DUE, LocalDate.of(2026, 10, 11));

        assertThat(charges.lateFee()).isEqualByComparingTo("18.91");
        assertThat(charges.defaultInterest()).isEqualByComparingTo("0.32");
        assertThat(charges.total()).isEqualByComparingTo("964.83");
    }

    @Test
    @DisplayName("PAG-05 0008/CT-3: no dia do vencimento não há multa nem juros")
    void onTheDueDate() {
        LateCharges charges = LateCharges.of(AMOUNT, DUE, DUE);

        assertThat(charges.daysLate()).isZero();
        assertThat(charges.lateFee()).isEqualByComparingTo("0.00");
        assertThat(charges.defaultInterest()).isEqualByComparingTo("0.00");
        assertThat(charges.total()).isEqualByComparingTo("945.60");
        assertThat(charges.late()).isFalse();
    }

    @Test
    @DisplayName("PAG-06 0008/CT-4: 60 dias de atraso")
    void sixtyDaysLate() {
        LateCharges charges = LateCharges.of(AMOUNT, DUE, LocalDate.of(2026, 12, 9));

        assertThat(charges.daysLate()).isEqualTo(60);
        assertThat(charges.defaultInterest()).isEqualByComparingTo("18.91");
        assertThat(charges.total()).isEqualByComparingTo("983.42");
        assertThat(charges.late()).isTrue();
    }

    @Test
    void earlyPaymentHasNoCharges() {
        LateCharges charges = LateCharges.of(AMOUNT, DUE, DUE.minusDays(5));

        assertThat(charges.daysLate()).isZero();
        assertThat(charges.total()).isEqualByComparingTo("945.60");
    }
}
