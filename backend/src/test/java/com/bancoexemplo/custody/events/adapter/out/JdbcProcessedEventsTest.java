package com.bancoexemplo.custody.events.adapter.out;

import static org.assertj.core.api.Assertions.assertThat;

import com.bancoexemplo.custody.TestcontainersConfiguration;
import com.bancoexemplo.custody.events.api.ProcessedEvents;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.support.TransactionTemplate;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class JdbcProcessedEventsTest {

    @Autowired
    private ProcessedEvents processedEvents;

    @Autowired
    private TransactionTemplate transaction;

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
