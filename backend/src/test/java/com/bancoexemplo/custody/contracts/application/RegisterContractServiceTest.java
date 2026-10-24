package com.bancoexemplo.custody.contracts.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bancoexemplo.custody.contracts.domain.AmortizationSystem;
import com.bancoexemplo.custody.contracts.domain.Contract;
import com.bancoexemplo.custody.contracts.domain.ContractStatus;
import com.bancoexemplo.custody.contracts.domain.DuplicateContractNumberException;
import com.bancoexemplo.custody.contracts.domain.InvalidContractException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RegisterContractServiceTest {

    private static final Instant NOW = Instant.parse("2027-01-10T13:00:00Z");

    private final InMemoryContractRepository repository = new InMemoryContractRepository();
    private final FixedFinancingCalculator calculator = new FixedFinancingCalculator();
    private final RegisterContractService service =
            new RegisterContractService(repository, calculator, Clock.fixed(NOW, ZoneOffset.UTC));

    static RegisterContractCommand command(String number) {
        return new RegisterContractCommand(
                number,
                "Cliente Exemplo",
                "12345678909",
                "9BWZZZ377VT004251",
                "Hatch 1.0 2026",
                AmortizationSystem.PRICE,
                new BigDecimal("10000.00"),
                new BigDecimal("2"),
                12,
                LocalDate.of(2027, 1, 10),
                LocalDate.of(2027, 2, 10),
                BigDecimal.ZERO,
                "operador");
    }

    @Test
    @DisplayName("CON-03 0005/CT-1: contrato cadastrado ativo com cronograma e CET guardados")
    void registersActiveContractWithScheduleAndCet() {
        Contract contract = service.register(command("CTR-0001"));

        assertThat(repository.saved).containsExactly(contract);
        assertThat(contract.status()).isEqualTo(ContractStatus.ACTIVE);
        assertThat(contract.number().value()).isEqualTo("CTR-0001");
        assertThat(contract.installments()).hasSize(12);
        assertThat(contract.installments().get(11).amount()).isEqualByComparingTo("945.55");
        assertThat(contract.annualCetPercent()).isEqualByComparingTo("26.95");
        assertThat(contract.customerCpf().digits()).isEqualTo("12345678909");
        assertThat(contract.chassisNumber().value()).isEqualTo("9BWZZZ377VT004251");
        assertThat(contract.customerName()).isEqualTo("Cliente Exemplo");
        assertThat(contract.vehicleDescription()).isEqualTo("Hatch 1.0 2026");
    }

    @Test
    void financingTermsReachTheCalculator() {
        service.register(command("CTR-0002"));

        assertThat(calculator.lastRequest.system()).isEqualTo(AmortizationSystem.PRICE);
        assertThat(calculator.lastRequest.principal()).isEqualByComparingTo("10000.00");
        assertThat(calculator.lastRequest.monthlyRatePercent()).isEqualByComparingTo("2");
        assertThat(calculator.lastRequest.termMonths()).isEqualTo(12);
        assertThat(calculator.lastRequest.releaseDate()).isEqualTo(LocalDate.of(2027, 1, 10));
        assertThat(calculator.lastRequest.upfrontCosts()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("CON-01 0005/CT-2: número já cadastrado é recusado e nada é guardado")
    void duplicateNumberIsRefused() {
        service.register(command("CTR-0001"));

        assertThatThrownBy(() -> service.register(command("CTR-0001")))
                .isInstanceOf(DuplicateContractNumberException.class)
                .hasMessage("Número do contrato já cadastrado");
        assertThat(repository.saved).hasSize(1);
    }

    @Test
    void invalidFieldIsRefusedBeforeCalculating() {
        RegisterContractCommand invalid = new RegisterContractCommand(
                "CTR-0003",
                "Cliente Exemplo",
                "12345678900",
                "9BWZZZ377VT004251",
                "Hatch 1.0 2026",
                AmortizationSystem.PRICE,
                new BigDecimal("10000.00"),
                new BigDecimal("2"),
                12,
                LocalDate.of(2027, 1, 10),
                LocalDate.of(2027, 2, 10),
                BigDecimal.ZERO,
                "operador");

        assertThatThrownBy(() -> service.register(invalid))
                .isInstanceOf(InvalidContractException.class)
                .hasMessage("CPF inválido");
        assertThat(calculator.lastRequest).isNull();
        assertThat(repository.saved).isEmpty();
    }

    @Test
    @DisplayName("CON-04 0005/CT-12: o contrato guarda quem cadastrou e quando")
    void recordsWhoAndWhen() {
        Contract contract = service.register(command("CTR-0004"));

        assertThat(contract.registeredBy()).isEqualTo("operador");
        assertThat(contract.registeredAt()).isEqualTo(NOW);
    }
}
