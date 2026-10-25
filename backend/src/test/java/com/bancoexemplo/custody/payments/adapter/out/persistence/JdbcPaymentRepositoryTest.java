package com.bancoexemplo.custody.payments.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bancoexemplo.custody.TestcontainersConfiguration;
import com.bancoexemplo.custody.payments.application.port.PaymentRepository;
import com.bancoexemplo.custody.payments.domain.InstallmentAlreadyPaidException;
import com.bancoexemplo.custody.payments.domain.Payment;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class JdbcPaymentRepositoryTest {

    @Autowired
    private PaymentRepository repository;

    @Autowired
    private JdbcClient jdbc;

    private static Payment payment(UUID contractId, int installmentNumber) {
        return new Payment(
                UUID.randomUUID(),
                contractId,
                installmentNumber,
                LocalDate.of(2026, 10, 24),
                new BigDecimal("968.92"),
                new BigDecimal("18.91"),
                new BigDecimal("4.41"),
                "operador",
                Instant.parse("2026-10-24T15:00:00Z"));
    }

    @Test
    void savedPaymentKeepsEveryField() {
        Payment payment = payment(UUID.randomUUID(), 1);

        repository.save(payment);

        Map<String, Object> row = jdbc.sql("SELECT * FROM payment WHERE id = :id")
                .param("id", payment.id())
                .query()
                .singleRow();
        assertThat(row.get("contract_id")).isEqualTo(payment.contractId());
        assertThat(row.get("installment_number")).isEqualTo(1);
        assertThat(row.get("paid_on").toString()).isEqualTo("2026-10-24");
        assertThat((BigDecimal) row.get("amount")).isEqualByComparingTo("968.92");
        assertThat((BigDecimal) row.get("late_fee")).isEqualByComparingTo("18.91");
        assertThat((BigDecimal) row.get("default_interest")).isEqualByComparingTo("4.41");
        assertThat(row.get("registered_by")).isEqualTo("operador");
        assertThat(((Timestamp) row.get("registered_at")).toInstant()).isEqualTo(payment.registeredAt());
    }

    @Test
    void paidInstallmentsAreOnlyThoseOfTheContract() {
        UUID contractId = UUID.randomUUID();
        repository.save(payment(contractId, 1));
        repository.save(payment(contractId, 2));
        repository.save(payment(UUID.randomUUID(), 3));

        assertThat(repository.paidInstallments(contractId)).containsExactlyInAnyOrder(1, 2);
        assertThat(repository.paidInstallments(UUID.randomUUID())).isEmpty();
    }

    @Test
    void databaseRefusesSecondPaymentOfTheSameInstallment() {
        UUID contractId = UUID.randomUUID();
        repository.save(payment(contractId, 1));

        assertThatThrownBy(() -> repository.save(payment(contractId, 1)))
                .isInstanceOf(InstallmentAlreadyPaidException.class)
                .hasMessage("Parcela já paga");
        assertThat(repository.paidInstallments(contractId)).containsExactly(1);
    }
}
