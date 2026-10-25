package com.bancoexemplo.custody.payments.adapter.out.events;

import static org.assertj.core.api.Assertions.assertThat;

import com.bancoexemplo.custody.TestcontainersConfiguration;
import com.bancoexemplo.custody.payments.application.port.PaymentEvents;
import com.bancoexemplo.custody.payments.domain.Payment;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
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
class PaymentEventsAdapterTest {

    @Autowired
    private PaymentEvents events;

    @Autowired
    private TransactionTemplate transaction;

    @Autowired
    private JdbcClient jdbc;

    @Autowired
    private JsonMapper json;

    @Test
    void registeredPaymentGoesToTheOutboxKeyedByContract() {
        UUID contractId = UUID.randomUUID();
        Payment payment = new Payment(
                UUID.randomUUID(),
                contractId,
                1,
                LocalDate.of(2026, 10, 24),
                new BigDecimal("945.60"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                "operador",
                Instant.parse("2026-10-24T15:00:00Z"));

        transaction.executeWithoutResult(status -> events.paymentRegistered(payment));

        Map<String, Object> row = jdbc.sql(
                        "SELECT topic, payload::text AS payload FROM outbox_event WHERE event_key = :key")
                .param("key", contractId.toString())
                .query()
                .singleRow();
        JsonNode body = json.readTree((String) row.get("payload"));
        assertThat(row.get("topic")).isEqualTo("custody.payment-registered");
        assertThat(body.get("contractId").asString()).isEqualTo(contractId.toString());
        assertThat(body.get("installmentNumber").asInt()).isEqualTo(1);
        assertThat(body.get("paidOn").asString()).isEqualTo("2026-10-24");
        assertThat(body.get("amount").decimalValue()).isEqualByComparingTo("945.60");
    }
}
