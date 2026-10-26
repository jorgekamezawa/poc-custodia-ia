package com.bancoexemplo.custody.payments.adapter.out.events;

import static org.assertj.core.api.Assertions.assertThat;

import com.bancoexemplo.custody.TestcontainersConfiguration;
import com.bancoexemplo.custody.payments.application.port.PaymentEvents;
import com.bancoexemplo.custody.payments.domain.payment.ContractTerms;
import com.bancoexemplo.custody.payments.domain.payment.InstallmentTerms;
import com.bancoexemplo.custody.payments.domain.payment.Payment;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
class PaymentEventsOutboxImplTest {

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
        ContractTerms terms = new ContractTerms(
                LocalDate.of(2026, 9, 10),
                List.of(new InstallmentTerms(1, LocalDate.of(2026, 11, 10), new BigDecimal("945.60"))));
        Payment payment = Payment.create(
                contractId,
                terms,
                Set.of(),
                1,
                LocalDate.of(2026, 10, 24),
                new BigDecimal("945.60"),
                "operador",
                LocalDate.of(2026, 10, 24),
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
