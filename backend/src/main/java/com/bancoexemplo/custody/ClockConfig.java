package com.bancoexemplo.custody;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class ClockConfig {

    // Business dates follow Brasília; the instant a clock gives is the same in any zone, so stored times stay UTC.
    @Bean
    Clock clock() {
        return Clock.system(ZoneId.of("America/Sao_Paulo"));
    }
}
