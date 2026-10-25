package com.bancoexemplo.custody.contracts.adapter.in.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bancoexemplo.custody.TestcontainersConfiguration;
import com.bancoexemplo.custody.contracts.ContractFixture;
import com.bancoexemplo.custody.contracts.application.port.ContractRepository;
import com.bancoexemplo.custody.contracts.domain.contract.Contract;
import com.bancoexemplo.custody.contracts.domain.contract.ContractInstallment;
import com.bancoexemplo.custody.contracts.domain.contract.exception.InstallmentNotPayableException;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class PaymentRegisteredListenerTest {

    @Autowired
    private PaymentRegisteredListener listener;

    @Autowired
    private ContractRepository contracts;

    @Autowired
    private JdbcClient jdbc;

    @Test
    @DisplayName("0007/CT-12: o mesmo evento aplicado duas vezes marca a parcela uma vez")
    void sameEventTwiceAppliesOnce() {
        Contract contract = ContractFixture.contract(
                "CTR-IDEM-" + UUID.randomUUID().toString().substring(0, 8));
        contracts.save(contract);
        UUID eventId = UUID.randomUUID();
        String message = """
                {"eventId": "%s", "occurredAt": "2027-02-01T15:00:00Z", "contractId": "%s",
                 "installmentNumber": 1, "paidOn": "2027-02-01", "amount": 945.60}
                """.formatted(eventId, contract.getId());

        listener.on(message);
        listener.on(message);

        assertThat(jdbc.sql("SELECT count(*) FROM processed_event WHERE event_id = :id")
                        .param("id", eventId)
                        .query(Long.class)
                        .single())
                .isEqualTo(1L);
        Contract updated = contracts.findById(contract.getId()).orElseThrow();
        assertThat(updated.getInstallments().get(0).getPaidOn()).isEqualTo(LocalDate.of(2027, 2, 1));
        assertThat(updated.getInstallments().stream().filter(ContractInstallment::isPaid))
                .hasSize(1);
    }

    @Test
    void eventTheContractRefusesFailsAndStaysUnprocessed() {
        Contract contract = ContractFixture.contract(
                "CTR-RECUSA-" + UUID.randomUUID().toString().substring(0, 8));
        contracts.save(contract);
        UUID eventId = UUID.randomUUID();
        String message = """
                {"eventId": "%s", "occurredAt": "2027-02-01T15:00:00Z", "contractId": "%s",
                 "installmentNumber": 13, "paidOn": "2027-02-01", "amount": 945.60}
                """.formatted(eventId, contract.getId());

        assertThatThrownBy(() -> listener.on(message))
                .isInstanceOf(InstallmentNotPayableException.class)
                .hasMessage("Parcela 13 não existe no contrato");
        assertThat(jdbc.sql("SELECT count(*) FROM processed_event WHERE event_id = :id")
                        .param("id", eventId)
                        .query(Long.class)
                        .single())
                .isZero();
    }
}
