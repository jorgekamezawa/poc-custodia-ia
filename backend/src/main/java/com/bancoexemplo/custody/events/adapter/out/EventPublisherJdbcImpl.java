package com.bancoexemplo.custody.events.adapter.out;

import com.bancoexemplo.custody.events.api.EventPublisher;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

@Component
class EventPublisherJdbcImpl implements EventPublisher {

    private final JdbcClient jdbc;
    private final JsonMapper json;
    private final Clock clock;

    EventPublisherJdbcImpl(JdbcClient jdbc, JsonMapper json, Clock clock) {
        this.jdbc = jdbc;
        this.json = json;
        this.clock = clock;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void publish(String topic, String key, Object payload) {
        UUID eventId = UUID.randomUUID();
        Instant now = Instant.now(clock);
        ObjectNode body = json.createObjectNode();
        body.put("eventId", eventId.toString());
        body.put("occurredAt", now.toString());
        body.setAll((ObjectNode) json.valueToTree(payload));
        jdbc.sql("""
                        INSERT INTO outbox_event (id, topic, event_key, payload, created_at)
                        VALUES (:id, :topic, :key, CAST(:payload AS jsonb), :createdAt)
                        """)
                .param("id", eventId)
                .param("topic", topic)
                .param("key", key)
                .param("payload", json.writeValueAsString(body))
                .param("createdAt", Timestamp.from(now))
                .update();
    }
}
