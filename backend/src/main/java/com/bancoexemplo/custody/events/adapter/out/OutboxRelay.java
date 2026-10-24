package com.bancoexemplo.custody.events.adapter.out;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Sends to Kafka the outbox events not yet published and marks them, waiting for Kafka to acknowledge each one. */
@Component
class OutboxRelay {

    private static final int BATCH = 100;
    private static final long SEND_TIMEOUT_SECONDS = 10;

    private final JdbcClient jdbc;
    private final KafkaTemplate<String, String> kafka;
    private final Clock clock;

    OutboxRelay(JdbcClient jdbc, KafkaTemplate<String, String> kafka, Clock clock) {
        this.jdbc = jdbc;
        this.kafka = kafka;
        this.clock = clock;
    }

    private record Pending(UUID id, String topic, String key, String payload) {}

    @Scheduled(fixedDelayString = "${custody.events.relay-delay-ms:500}")
    @Transactional
    public void publishPending() throws InterruptedException {
        List<Pending> pending = jdbc.sql("""
                        SELECT id, topic, event_key, payload::text AS payload FROM outbox_event
                        WHERE published_at IS NULL ORDER BY created_at LIMIT :batch
                        FOR UPDATE SKIP LOCKED
                        """)
                .param("batch", BATCH)
                .query((row, index) -> new Pending(
                        row.getObject("id", UUID.class),
                        row.getString("topic"),
                        row.getString("event_key"),
                        row.getString("payload")))
                .list();
        for (Pending event : pending) {
            try {
                kafka.send(event.topic(), event.key(), event.payload()).get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (ExecutionException | TimeoutException e) {
                // Stop here so later events keep their order; the next run retries from this one.
                return;
            }
            jdbc.sql("UPDATE outbox_event SET published_at = :now WHERE id = :id")
                    .param("now", Timestamp.from(Instant.now(clock)))
                    .param("id", event.id())
                    .update();
        }
    }
}
