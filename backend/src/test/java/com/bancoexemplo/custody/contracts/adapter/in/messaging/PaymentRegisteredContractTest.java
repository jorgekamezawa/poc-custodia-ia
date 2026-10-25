package com.bancoexemplo.custody.contracts.adapter.in.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import com.bancoexemplo.custody.TestcontainersConfiguration;
import com.bancoexemplo.custody.contracts.ContractFixture;
import com.bancoexemplo.custody.contracts.application.port.ContractRepository;
import com.bancoexemplo.custody.contracts.domain.contract.Contract;
import com.bancoexemplo.custody.payments.application.port.PaymentEvents;
import com.bancoexemplo.custody.payments.domain.payment.ContractTerms;
import com.bancoexemplo.custody.payments.domain.payment.InstallmentTerms;
import com.bancoexemplo.custody.payments.domain.payment.Payment;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
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

/**
 * Each module keeps its own copy of the payment-registered event, so nothing at compile time ties them; this test
 * sends what payments really publishes to what contracts really reads.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class PaymentRegisteredContractTest {

    private static final LocalDate TODAY = LocalDate.now(ZoneId.of("America/Sao_Paulo"));

    // Not today, so the test fails if contracts took the date from its own clock instead of the event.
    private static final LocalDate PAID_ON = TODAY.minusDays(1);

    @Autowired
    private PaymentEvents paymentEvents;

    @Autowired
    private PaymentRegisteredListener listener;

    @Autowired
    private ContractRepository contracts;

    @Autowired
    private TransactionTemplate transaction;

    @Autowired
    private JdbcClient jdbc;

    @Test
    void whatPaymentsPublishesIsWhatContractsReads() {
        Contract contract = ContractFixture.contract(
                "CTR-EVT-" + UUID.randomUUID().toString().substring(0, 8),
                "12345678909",
                TODAY.plusDays(15),
                Instant.parse("2026-01-10T13:00:00Z"));
        contracts.save(contract);
        var first = contract.getInstallments().get(0);
        Payment payment = Payment.create(
                contract.getId(),
                new ContractTerms(
                        contract.getTerms().releaseDate(),
                        List.of(new InstallmentTerms(1, first.getDueDate(), first.getAmount()))),
                Set.of(),
                1,
                PAID_ON,
                first.getAmount(),
                "operador",
                TODAY,
                Instant.now());

        transaction.executeWithoutResult(status -> paymentEvents.paymentRegistered(payment));
        Map<String, Object> published = jdbc.sql(
                        "SELECT topic, payload::text AS payload FROM outbox_event WHERE event_key = :key")
                .param("key", contract.getId().toString())
                .query()
                .singleRow();
        listener.on((String) published.get("payload"));

        assertThat(published.get("topic")).isEqualTo(PaymentRegisteredListener.TOPIC);
        Contract updated = contracts.findById(contract.getId()).orElseThrow();
        assertThat(updated.getInstallments().get(0).getPaidOn()).isEqualTo(PAID_ON);
    }
}
