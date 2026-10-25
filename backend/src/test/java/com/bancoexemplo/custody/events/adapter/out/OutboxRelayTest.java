package com.bancoexemplo.custody.events.adapter.out;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.bancoexemplo.custody.TestcontainersConfiguration;
import com.bancoexemplo.custody.events.api.EventPublisher;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
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
import tools.jackson.databind.json.JsonMapper;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class OutboxRelayTest {

    @Autowired
    private OutboxRelay relay;

    @Autowired
    private EventPublisher publisher;

    @Autowired
    private TransactionTemplate transaction;

    @Autowired
    private JdbcClient jdbc;

    @Autowired
    private KafkaConnectionDetails kafka;

    @Autowired
    private JsonMapper json;

    record Sample(String contractId, int installmentNumber) {}

    @Test
    @DisplayName("0007/CT-14: o publicador leva o evento ao Kafka e marca como publicado")
    void relayPublishesToKafka() throws InterruptedException {
        String topic = "test." + UUID.randomUUID();
        transaction.executeWithoutResult(status -> publisher.publish(topic, "contract-9", new Sample("c-9", 3)));

        // The scheduled run may get there first; the event is then already locked or published and this call skips it.
        relay.publishPending();

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
        String stored = jdbc.sql("SELECT payload::text FROM outbox_event WHERE topic = :topic")
                .param("topic", topic)
                .query(String.class)
                .single();
        assertThat(json.readTree(received.get(0).value())).isEqualTo(json.readTree(stored));
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
}
