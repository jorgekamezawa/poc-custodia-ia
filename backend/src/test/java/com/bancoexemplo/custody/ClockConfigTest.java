package com.bancoexemplo.custody;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClockConfigTest {

    private final Clock projectClock = new ClockConfig().clock();

    @Test
    @DisplayName("CON-05 0006/CT-4: hoje é a data de Brasília, não a do UTC")
    void todayIsTheBrasiliaDate() {
        Clock lateNightInBrasilia = Clock.fixed(Instant.parse("2026-10-25T02:00:00Z"), projectClock.getZone());

        assertThat(LocalDate.now(lateNightInBrasilia)).isEqualTo(LocalDate.of(2026, 10, 24));
    }

    @Test
    void theInstantStaysUniversal() {
        Instant before = Instant.now();

        assertThat(projectClock.instant()).isBetween(before, Instant.now());
    }
}
