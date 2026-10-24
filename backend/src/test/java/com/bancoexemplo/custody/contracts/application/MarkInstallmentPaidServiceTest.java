package com.bancoexemplo.custody.contracts.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.bancoexemplo.custody.contracts.domain.Contract;
import com.bancoexemplo.custody.contracts.domain.ContractInstallment;
import com.bancoexemplo.custody.contracts.domain.ContractNumber;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MarkInstallmentPaidServiceTest {

    private final InMemoryContractRepository repository = new InMemoryContractRepository();
    private final MarkInstallmentPaidService service = new MarkInstallmentPaidService(repository);

    @Test
    @DisplayName("CON-06 0007/CT-11: o contrato marca a parcela paga e o saldo cai para o da parcela")
    void marksTheInstallmentPaid() {
        RegisterContractService register = new RegisterContractService(
                repository,
                new FixedFinancingCalculator(),
                Clock.fixed(Instant.parse("2026-09-10T13:00:00Z"), ZoneOffset.UTC));
        Contract contract = register.register(RegisterContractServiceTest.command("CTR-PAG-1"));
        ContractInstallment first = contract.installments().get(0);
        assertThat(contract.outstandingBalance()).isEqualByComparingTo("10000.00");

        service.markPaid(contract.id(), 1, LocalDate.of(2026, 10, 24));

        Contract updated = repository.findById(contract.id()).orElseThrow();
        assertThat(updated.number()).isEqualTo(new ContractNumber("CTR-PAG-1"));
        assertThat(updated.installments().get(0).paidOn()).isEqualTo(LocalDate.of(2026, 10, 24));
        assertThat(updated.installments().get(1).paidOn()).isNull();
        assertThat(updated.outstandingBalance()).isEqualByComparingTo("9254.40");
        assertThat(first.amount()).isEqualByComparingTo(new BigDecimal("945.60"));
    }
}
