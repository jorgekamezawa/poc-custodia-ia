package com.bancoexemplo.custody.contracts.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.bancoexemplo.custody.contracts.ContractFixture;
import com.bancoexemplo.custody.contracts.application.port.ContractRepository;
import com.bancoexemplo.custody.contracts.domain.contract.Contract;
import com.bancoexemplo.custody.contracts.domain.contract.InstallmentSituation;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContractQueryServiceTest {

    private static final int LIMIT = 50;

    @Mock
    private ContractRepository repository;

    private ContractQueryService service;

    @BeforeEach
    void setUp() {
        service = new ContractQueryService(
                repository, Clock.fixed(Instant.parse("2026-10-24T15:00:00Z"), ZoneId.of("America/Sao_Paulo")));
    }

    private static List<ContractSummary> found(String number) {
        return List.of(new ContractSummary(
                UUID.randomUUID(),
                number,
                "Cliente Exemplo",
                "12345678909",
                new BigDecimal("10000.00"),
                Instant.parse("2027-01-10T13:00:00Z")));
    }

    @Test
    void viewHasEachInstallmentSituationAndTheBalance() {
        Contract contract = ContractFixture.contract(
                "CTR-0001", "12345678909", LocalDate.of(2026, 10, 10), Instant.parse("2026-09-10T13:00:00Z"));
        when(repository.findById(contract.getId())).thenReturn(Optional.of(contract));

        ContractView view = service.view(contract.getId());

        assertThat(view.outstandingBalance()).isEqualByComparingTo("10000.00");
        assertThat(view.installments().get(0).situation()).isEqualTo(InstallmentSituation.OVERDUE);
        assertThat(view.installments().get(1).situation()).isEqualTo(InstallmentSituation.OPEN);
        assertThat(view.installments().get(0).installment().getNumber()).isEqualTo(1);
    }

    @Test
    void unknownContractIsNotFound() {
        UUID unknown = UUID.randomUUID();
        when(repository.findById(unknown)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.view(unknown))
                .isInstanceOf(ContractNotFoundException.class)
                .hasMessage("Contrato não encontrado");
    }

    @Test
    void elevenDigitsWithPunctuationSearchTheCpf() {
        List<ContractSummary> byCpf = found("CTR-0001");
        when(repository.findByCpf("52998224725", LIMIT)).thenReturn(byCpf);

        assertThat(service.search("529.982.247-25")).isEqualTo(byCpf);
    }

    @Test
    void otherTextSearchesTheNumberPrefix() {
        List<ContractSummary> byNumber = found("CTR-0001");
        when(repository.findByNumberPrefix("ctr-00", LIMIT)).thenReturn(byNumber);

        assertThat(service.search(" ctr-00 ")).isEqualTo(byNumber);
    }

    @Test
    void elevenCharactersWithLettersSearchTheNumber() {
        List<ContractSummary> byNumber = found("CTR12345678");
        when(repository.findByNumberPrefix("CTR12345678", LIMIT)).thenReturn(byNumber);

        assertThat(service.search("CTR12345678")).isEqualTo(byNumber);
    }

    @Test
    void noTextListsTheLatest() {
        List<ContractSummary> latest = found("CTR-0001");
        when(repository.findLatest(LIMIT)).thenReturn(latest);

        assertThat(service.search(null)).isEqualTo(latest);
        assertThat(service.search("   ")).isEqualTo(latest);
    }
}
