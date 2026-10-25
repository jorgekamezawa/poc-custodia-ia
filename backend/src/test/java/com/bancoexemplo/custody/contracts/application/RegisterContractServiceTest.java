package com.bancoexemplo.custody.contracts.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.bancoexemplo.custody.contracts.application.port.ContractRepository;
import com.bancoexemplo.custody.contracts.application.port.Financing;
import com.bancoexemplo.custody.contracts.application.port.FinancingCalculator;
import com.bancoexemplo.custody.contracts.application.port.FinancingRequest;
import com.bancoexemplo.custody.contracts.domain.AmortizationSystem;
import com.bancoexemplo.custody.contracts.domain.Contract;
import com.bancoexemplo.custody.contracts.domain.ContractInstallment;
import com.bancoexemplo.custody.contracts.domain.ContractNumber;
import com.bancoexemplo.custody.contracts.domain.ContractStatus;
import com.bancoexemplo.custody.contracts.domain.DuplicateContractNumberException;
import com.bancoexemplo.custody.contracts.domain.InvalidContractException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisterContractServiceTest {

    private static final Instant NOW = Instant.parse("2027-01-10T13:00:00Z");

    /** The terms of {@link #command}, exactly as the calculator must receive them. */
    private static final FinancingRequest TERMS = new FinancingRequest(
            AmortizationSystem.PRICE,
            new BigDecimal("10000.00"),
            new BigDecimal("2"),
            12,
            LocalDate.of(2027, 1, 10),
            LocalDate.of(2027, 2, 10),
            BigDecimal.ZERO);

    @Mock
    private ContractRepository repository;

    @Mock
    private FinancingCalculator calculator;

    private RegisterContractService service;

    @BeforeEach
    void setUp() {
        service = new RegisterContractService(repository, calculator, Clock.fixed(NOW, ZoneOffset.UTC));
    }

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

    /** 12 installments of the spec example (the last of R$ 945.55) with CET 26.95. */
    private static Financing financing() {
        List<ContractInstallment> installments = new ArrayList<>();
        for (int number = 1; number <= 12; number++) {
            installments.add(new ContractInstallment(
                    number,
                    LocalDate.of(2027, 2, 10).plusMonths(number - 1L),
                    new BigDecimal(number == 12 ? "945.55" : "945.60"),
                    new BigDecimal("200.00"),
                    new BigDecimal("745.60"),
                    new BigDecimal("9254.40")));
        }
        return new Financing(installments, new BigDecimal("26.95"));
    }

    @Test
    @DisplayName("CON-03 0005/CT-1: contrato cadastrado ativo com cronograma e CET guardados")
    void registersActiveContractWithScheduleAndCet() {
        when(calculator.calculate(TERMS)).thenReturn(financing());

        Contract contract = service.register(command("CTR-0001"));

        verify(repository).save(contract);
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
    @DisplayName("CON-01 0005/CT-2: número já cadastrado é recusado e nada é guardado")
    void duplicateNumberIsRefused() {
        when(repository.existsByNumber(new ContractNumber("CTR-0001"))).thenReturn(true);

        assertThatThrownBy(() -> service.register(command("CTR-0001")))
                .isInstanceOf(DuplicateContractNumberException.class)
                .hasMessage("Número do contrato já cadastrado");
        verify(repository, never()).save(any());
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
        verifyNoInteractions(calculator, repository);
    }

    @Test
    @DisplayName("CON-04 0005/CT-12: o contrato guarda quem cadastrou e quando")
    void recordsWhoAndWhen() {
        when(calculator.calculate(TERMS)).thenReturn(financing());

        Contract contract = service.register(command("CTR-0004"));

        assertThat(contract.registeredBy()).isEqualTo("operador");
        assertThat(contract.registeredAt()).isEqualTo(NOW);
    }
}
