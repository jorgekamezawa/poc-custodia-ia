package com.bancoexemplo.custody.events.adapter.out;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bancoexemplo.custody.TestcontainersConfiguration;
import com.bancoexemplo.custody.events.api.EventPublisher;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class EventPublisherJdbcImplTest {

    @Autowired
    private EventPublisher publisher;

    @Autowired
    private TransactionTemplate transaction;

    @Autowired
    private JdbcClient jdbc;

    @Autowired
    private JsonMapper json;

    record Sample(String contractId, int installmentNumber) {}

    private static String topic() {
        return "test." + UUID.randomUUID();
    }

    private long rows(String topic) {
        return jdbc.sql("SELECT count(*) FROM outbox_event WHERE topic = :topic")
                .param("topic", topic)
                .query(Long.class)
                .single();
    }

    @Test
    void publishedEventIsStoredWithTopicKeyAndBody() {
        String topic = topic();
        transaction.executeWithoutResult(status -> publisher.publish(topic, "key-1", new Sample("c-1", 1)));

        Map<String, Object> row = jdbc.sql(
                        "SELECT id, event_key, payload::text AS payload FROM outbox_event WHERE topic = :topic")
                .param("topic", topic)
                .query()
                .singleRow();
        JsonNode body = json.readTree((String) row.get("payload"));
        assertThat(row.get("event_key")).isEqualTo("key-1");
        assertThat(body.get("eventId").asString()).isEqualTo(row.get("id").toString());
        assertThat(body.get("occurredAt").asString()).isNotBlank();
        assertThat(body.get("contractId").asString()).isEqualTo("c-1");
        assertThat(body.get("installmentNumber").asInt()).isEqualTo(1);
    }

    @Test
    @DisplayName("0007/CT-15: transação que falha depois de gravar o evento não deixa evento")
    void failedTransactionLeavesNoEvent() {
        String topic = topic();

        assertThatThrownBy(() -> transaction.executeWithoutResult(status -> {
                    publisher.publish(topic, "key-1", new Sample("c-1", 1));
                    throw new IllegalStateException("falha depois do evento");
                }))
                .isInstanceOf(IllegalStateException.class);
        assertThat(rows(topic)).isZero();
    }
}
