package com.bancoexemplo.custody.events.adapter.out;

import com.bancoexemplo.custody.events.api.ProcessedEvents;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
class JdbcProcessedEvents implements ProcessedEvents {

    private final JdbcClient jdbc;
    private final Clock clock;

    JdbcProcessedEvents(JdbcClient jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public boolean markProcessed(UUID eventId, String consumer) {
        return jdbc.sql("""
                                INSERT INTO processed_event (event_id, consumer, processed_at)
                                VALUES (:eventId, :consumer, :processedAt)
                                ON CONFLICT DO NOTHING
                                """)
                        .param("eventId", eventId)
                        .param("consumer", consumer)
                        .param("processedAt", Timestamp.from(Instant.now(clock)))
                        .update()
                == 1;
    }
}
