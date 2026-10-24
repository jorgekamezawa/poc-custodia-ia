package com.bancoexemplo.custody.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import com.bancoexemplo.custody.TestcontainersConfiguration;
import com.bancoexemplo.custody.events.api.EventPublisher;
import com.bancoexemplo.custody.events.api.ProcessedEvents;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.kafka.autoconfigure.KafkaConnectionDetails;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class EventsTest {

    @Autowired
    private EventPublisher publisher;

    @Autowired
    private ProcessedEvents processedEvents;

    @Autowired
    private TransactionTemplate transaction;

    @Autowired
    private JdbcClient jdbc;

    @Autowired
    private KafkaConnectionDetails kafka;

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

    @Test
    @DisplayName("0007/CT-14: o publicador leva o evento ao Kafka e marca como publicado")
    void relayPublishesToKafka() {
        String topic = topic();
        transaction.executeWithoutResult(status -> publisher.publish(topic, "contract-9", new Sample("c-9", 3)));

        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, String.join(",", kafka.getBootstrapServers()));
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "test-" + UUID.randomUUID());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        List<ConsumerRecord<String, String>> received = new ArrayList<>();
        try (KafkaConsumer<String, String> consumer =
                new KafkaConsumer<>(properties, new StringDeserializer(), new StringDeserializer())) {
            consumer.subscribe(List.of(topic));
            await().atMost(Duration.ofSeconds(30)).until(() -> {
                consumer.poll(Duration.ofMillis(500)).forEach(received::add);
                return !received.isEmpty();
            });
        }

        assertThat(received.get(0).key()).isEqualTo("contract-9");
        assertThat(json.readTree(received.get(0).value()).get("contractId").asString())
                .isEqualTo("c-9");
        await().atMost(Duration.ofSeconds(10))
                .until(() ->
                        jdbc.sql("SELECT count(*) FROM outbox_event WHERE topic = :topic AND published_at IS NOT NULL")
                                        .param("topic", topic)
                                        .query(Long.class)
                                        .single()
                                == 1L);
    }

    @Test
    void eachConsumerAppliesAnEventOnce() {
        UUID eventId = UUID.randomUUID();

        Boolean first = transaction.execute(status -> processedEvents.markProcessed(eventId, "contracts"));
        Boolean again = transaction.execute(status -> processedEvents.markProcessed(eventId, "contracts"));
        Boolean otherConsumer = transaction.execute(status -> processedEvents.markProcessed(eventId, "history"));

        assertThat(first).isTrue();
        assertThat(again).isFalse();
        assertThat(otherConsumer).isTrue();
    }
}
