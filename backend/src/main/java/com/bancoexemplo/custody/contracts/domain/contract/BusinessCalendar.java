package com.bancoexemplo.custody.contracts.domain.contract;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

/** Business dates follow Brasília, whatever zone the system clock runs in. */
public final class BusinessCalendar {

    private static final ZoneId BRASILIA = ZoneId.of("America/Sao_Paulo");

    private BusinessCalendar() {}

    public static LocalDate today(Clock clock) {
        return LocalDate.now(clock.withZone(BRASILIA));
    }
}
