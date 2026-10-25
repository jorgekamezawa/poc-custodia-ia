package com.bancoexemplo.custody.payments.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bancoexemplo.custody.TestcontainersConfiguration;
import com.bancoexemplo.custody.payments.application.port.PaymentRepository;
import com.bancoexemplo.custody.payments.domain.payment.ContractTerms;
import com.bancoexemplo.custody.payments.domain.payment.InstallmentAlreadyPaidException;
import com.bancoexemplo.custody.payments.domain.payment.InstallmentTerms;
import com.bancoexemplo.custody.payments.domain.payment.Payment;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 24);

    /** Released on 10/09/2026, 12 installments of R$ 945.60 from 10/10/2026: the first is 14 days late today. */
    private static final ContractTerms TERMS = new ContractTerms(
            LocalDate.of(2026, 9, 10),
            IntStream.rangeClosed(1, 12)
                    .mapToObj(n -> new InstallmentTerms(
                            n, LocalDate.of(2026, 10, 10).plusMonths(n - 1L), new BigDecimal("945.60")))
                    .toList());

    /** The installment paid today, with the ones before it already paid; only the first carries charges. */
    private static Payment payment(UUID contractId, int installmentNumber) {
        return Payment.create(
                contractId,
                TERMS,
                IntStream.range(1, installmentNumber).boxed().collect(Collectors.toSet()),
                installmentNumber,
                TODAY,
                new BigDecimal(installmentNumber == 1 ? "968.92" : "945.60"),
                "operador",
                TODAY,
                Instant.parse("2026-10-24T15:00:00Z"));
    }

    @Test
    void savedPaymentKeepsEveryField() {
        Payment payment = payment(UUID.randomUUID(), 1);

        repository.save(payment);

        Map<String, Object> row = jdbc.sql("SELECT * FROM payment WHERE id = :id")
                .param("id", payment.getId())
                .query()
                .singleRow();
        assertThat(row.get("contract_id")).isEqualTo(payment.getContractId());
        assertThat(row.get("installment_number")).isEqualTo(1);
        assertThat(row.get("paid_on").toString()).isEqualTo("2026-10-24");
        assertThat((BigDecimal) row.get("amount")).isEqualByComparingTo("968.92");
        assertThat((BigDecimal) row.get("late_fee")).isEqualByComparingTo("18.91");
        assertThat((BigDecimal) row.get("default_interest")).isEqualByComparingTo("4.41");
        assertThat(row.get("registered_by")).isEqualTo("operador");
        assertThat(((Timestamp) row.get("registered_at")).toInstant()).isEqualTo(payment.getRegisteredAt());
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
